import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Rebecca Azanaw
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color


	// Drawing state
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged
	private Point moveTo = null;				// where object is moved to
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		//iterates through every shape in the local sketch and prints each one
		if (sketch != null) {
			for (Integer id : sketch.getShapes().keySet()) {
				if (sketch.getShape(id) != null) {
					sketch.getShape(id).draw(g);
				}
			}
		}
		if (curr != null) {		// Draws the current shape if it exists
			curr.draw(g);
		}
	}

	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		for (Integer shapeID: sketch.getShapes().descendingKeySet()) {	// if point clicked is in an already-existing shape
			if(sketch.getShapes().get(shapeID).contains(p.x, p.y)) {
				curr = sketch.getShape(shapeID);
				if (mode == Mode.MOVE) {                    // In moving mode, start dragging if clicked in the shape
					moveFrom = p;
					moveTo = p;
					movingId = shapeID;
				} else if (mode == Mode.RECOLOR) {        // In recoloring mode, change the shape's color if clicked in it
					curr.setColor(color);
					String message = mode + " " + shapeID + " " + curr.toString();
					comm.send(message);
					movingId = shapeID;
					curr = null;
				} else if (mode == Mode.DELETE) {        // In deleting mode, delete the shape if clicked in it
					String message = mode + " " + shapeID + " " + curr.toString();
					comm.send(message);
					curr = null;
				}
				break;
			}
		}
		if (mode == Editor.Mode.DRAW) {                    // In drawing mode, start drawing a new shape
			drawFrom = p;                                // Set the point to drawFrom as the current mousepress point
			switch (shapeType) {                        // depending on the shape type, instantiate that shape type
				case "ellipse" -> curr = new Ellipse((int) p.getX(), (int) p.getY(), color);
				case "freehand" -> curr = new Polyline(p, color);
				case "segment" -> curr = new Segment((int) p.getX(), (int) p.getY(), color);
				case "rectangle" -> curr = new Rectangle((int) p.getX(), (int) p.getY(), color);
			}
		}
		repaint();        // Refresh the canvas

	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
			if (mode == Editor.Mode.DRAW) {            // In drawing mode, revise the shape as it is stretched out
				switch (shapeType) {
					case "rectangle" -> ((Rectangle) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
					case "ellipse" -> ((Ellipse) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
					case "segment" -> {
						((Segment) curr).setStart(drawFrom.x, drawFrom.y);
						((Segment) curr).setEnd(p.x, p.y);
					}
					case "freehand" -> ((Polyline) curr).addPoint(p);
				}
				movingId = sketch.getShapes().size();
			}

			if (mode == Editor.Mode.MOVE && curr != null && moveFrom != null) {        // In moving mode, shift the object and keep track of where next step is from
				curr.moveBy(p.x - moveTo.x, p.y - moveTo.y);
				moveTo = p;
			}
			repaint();        // Be sure to refresh the canvas (repaint) if the appearance has changed

	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease() {
		// if there's a shape to be handled
		if (curr != null) {
			String message = mode + " " + movingId + " " + curr.toString();		// send the information about the shape as a message to the server
			if(mode == Mode.MOVE && moveFrom != null){		// special case for move: add dx and dy to message
				message += " " + (moveFrom.x - moveTo.x) + " " + (moveFrom.y - moveTo.y);
				curr.moveBy(moveFrom.x - moveTo.x, moveFrom.y - moveTo.y);	// opposite reaction to prevent a double move
			}
			comm.send(message); // sends message to the server
		}
	}

	public void update(String line){
		Message message = new Message(line);		// instantiates new message
		if (message.mode.equals("DRAW")) {			// if mode is draw
			Shape shape = null;
			Color color = new Color(message.rgb);	// decode color from message

			// based on the shape type, instantiate a new shape based on the information from the message
			switch (message.shapeType) {
				case "ellipse" -> shape = new Ellipse(message.x1, message.y1, message.x2, message.y2, color);
				case "segment" -> shape = new Segment(message.x1, message.y1, message.x2, message.y2, color);
				case "rectangle" -> shape = new Rectangle(message.x1, message.y1, message.x2, message.y2, color);
				case "polyline" -> { shape = new Polyline(message.points, color); }
			}
			getSketch().addShape(message.ID, shape); 		// add the shape to the editor's sketch with its ID
		}
		if (message.mode.equals("MOVE")){		// if mode is move
			getSketch().moveShape(message.ID, message.dx, message.dy);

		}
		if (message.mode.equals("RECOLOR")){	// if mode is recolor
			getSketch().recolorShape(message.ID, new Color(message.rgb));
		}
		if (message.mode.equals("DELETE")) {	// if mode is delete;
			getSketch().deleteShape(message.ID);
		}
		repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}

}
