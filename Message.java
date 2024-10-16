import java.awt.*;
import java.util.ArrayList;
/**
 * Parses information between clients.
 * Takes a string representation and parses it into usable variables.
 * @author Rebecca Azanaw
 */
public class Message {
    // variables to be set later
    String mode;
    String shapeType;
    int ID;
    int x1;
    int y1;
    int x2;
    int y2;
    int rgb;
    int dx;
    int dy;
    ArrayList<Point> points;

    public Message(String s) {
        processMessage(s);
    }

    public void processMessage(String s){

        String[] tokens = s.split(" ");     // breaks inital string into pieces split by spaces
        mode = tokens[0];                         // mode will be first
        ID = Integer.parseInt(tokens[1]);         // parse int ID
        shapeType = tokens[2];                    // parse shape type

        if (shapeType.equals("polyline")){        // polyline requires different parsing
            rgb = Integer.parseInt(tokens[3]);
            String toSplit = tokens[4];           // split into different points
            String[] allPoints = toSplit.split("\\|");
            points = new ArrayList<>();

            for (String point: allPoints) {
                String[] XandY = point.split(",");     // split into x and y
                int x = (int) Integer.parseInt(XandY[0]);
                int y = (int) Integer.parseInt(XandY[1]);
                points.add(new Point(x, y));                  // add to points list from the parsed x and y
            }
            if (mode.equals("MOVE") && tokens.length > 6) {
                dx = Integer.parseInt(tokens[5]);
                dy = Integer.parseInt(tokens[6]);
            }

        }
        else {
            // parse all the tokens into their respective values
            x1 = Integer.parseInt(tokens[3]);
            y1 = Integer.parseInt(tokens[4]);
            x2 = Integer.parseInt(tokens[5]);
            y2 = Integer.parseInt(tokens[6]);
            rgb = Integer.parseInt(tokens[7]);
            if (mode.equals("MOVE") && tokens.length > 9) {     //special case for move: parse dx and dy
                dx = Integer.parseInt(tokens[8]);
                dy = Integer.parseInt(tokens[9]);
            }
        }
    }

    @Override
    public String toString() {
        if (shapeType.equals("polyline")) {
            Polyline polyline = new Polyline(points, new Color(rgb));       //instantiate new polyline in order to call pointsToString()
            return mode + " " + ID + " " + shapeType + " " + rgb + " " + polyline.pointsToString();
        }
        else {
            return mode + " " + ID + " " + shapeType + " " + x1 + " " + x2 + " " + y1 + " " + y2 + " " + rgb;
        }
    }
}
