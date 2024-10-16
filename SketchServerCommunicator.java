import java.awt.*;
import java.io.*;
import java.net.Socket;
/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @authors Rebecca Azanaw
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// if a new client connects, tell them the state of the world
			if (server.getSketch().getShapes().size() > 0) {
				for (Integer key : server.getSketch().getShapes().descendingKeySet()) {
					out.println("DRAW " + key + " " + server.getSketch().getShape(key).toString());
				}
			}

			String line;
			while ((line = in.readLine()) != null){		// Keep getting and handling messages from the client
				Message message = new Message(line);		// parses the message into its parts
				Shape shape = null;		//instantiate shape
				if (message.mode.equals("DRAW")) {
					message.ID = server.IDcount;		// if mode is draw, the shape will be new and need an ID
					Color color = new Color(message.rgb);		//creates color from message
					switch (message.shapeType) {				// depending on shape type, different objects are instantiated
						case "ellipse" -> shape = new Ellipse(message.x1, message.y1, message.x2, message.y2, color);
						case "segment" -> shape = new Segment(message.x1, message.y1, message.x2, message.y2, color);
						case "rectangle" -> shape = new Rectangle(message.x1, message.y1, message.x2, message.y2, color);
						case "polyline" -> shape = new Polyline(message.points, color);
					}
					server.getSketch().addShape(message.ID, shape);		// adds shape to master sketch located in server
					server.IDcount += 1;
				}

				if (message.mode.equals("MOVE")){						// if mode is move, move based on ID
					server.getSketch().moveShape(message.ID, message.dx, message.dy);
					shape = server.getSketch().getShape(message.ID);
				}
				if (message.mode.equals("RECOLOR")){// if mode is recolor, recolor based on ID
					server.getSketch().recolorShape(message.ID, new Color(message.rgb));
					shape = server.getSketch().getShape(message.ID);
				}
				if (message.mode.equals("DELETE")) {					// if mode is delete,  delete shape
					shape = server.getSketch().getShape(message.ID);
					if(shape != null) {
						server.getSketch().deleteShape(message.ID);
					}
				}

				// re-draw all the shapes on each communicator by broadcasting through the server with updated IDs
				if (shape != null) {
					if(message.mode.equals("MOVE")) {
						// when in move mode, send opposite dx and dy back to return the shape to its place on the other screens
						server.broadcast(message.mode + " " + message.ID + " " + shape.toString() + " " + message.dx*-1 + " " + message.dy*-1);
					}
					else{
						server.broadcast(message.mode + " " + message.ID + " " + shape.toString());
					}
				}
			}
			// Clean up -- note that also removes self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

