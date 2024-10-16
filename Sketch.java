import java.awt.*;
import java.util.*;

/**
 * Holds a map of all of the shapes with IDs and holds methods to alter shapes.
 * @author Rebecca Azanaw
 */

public class Sketch {
    private TreeMap<Integer, Shape> shapes;     //map of ids to their shape

    public Sketch(){
        this.shapes = new TreeMap<Integer, Shape>();
    }

    public synchronized TreeMap<Integer, Shape> getShapes(){        // get all shapes
        return shapes;
    }   // returns the map

    public synchronized Shape getShape(Integer i){                // get one shape using ID
        return shapes.get(i);
    }      // returns the shape at the id

    public synchronized void addShape(int i, Shape s){             // add shape
        shapes.put(i, s);
    }       // adds the shape to the map

    public synchronized void deleteShape(int i){                    //remove shape
        shapes.remove(i);
    }       // removes the shape from the map

    public synchronized void recolorShape(int i, Color c){          // recolors the shape at the id
        Shape s = shapes.get(i);
        s.setColor(c);
        shapes.put(i, s);
    }

    public synchronized void moveShape(int i, int dx, int dy){      // moves the shape at the id by dx and dy
        Shape s = shapes.get(i);
        if (s != null) {
            s.moveBy(dx, dy);
            shapes.put(i, s);
        }
    }
}
