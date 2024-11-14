import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 * @author Rebecca Azanaw
 */
public class Polyline implements Shape {

	private Color color;
	public ArrayList<Point> points;

	public Polyline(Point firstpoint, Color color) {		//constructor with one point
		this.color = color;
		points = new ArrayList<>();
		points.add(firstpoint);
	}

	public Polyline(ArrayList<Point> points, Color color){	// constructor with list of points
		this.color = color;
		this.points = points;
	}

	public String pointsToString() {						// points to string method for later parsing
		String giantString = "";
		for (Point point: points) {
			giantString += (int) point.getX() + "," + (int) point.getY() + "|";
		}
		return giantString;
	}

	public void addPoint(Point newPoint) {
		points.add(newPoint);
	}		// adds point

	@Override
	public void moveBy(int dx, int dy) {		//moves each point with a for loop
		ArrayList<Point> newList = new ArrayList<>();
		for (Point point: points) {
			// changes coordinate by using newPoint
			Point newPoint = new Point(((int) point.getX() + dx), ((int) point.getY() + dy));
			newList.add(newPoint);
		}
		points = newList;		// sets points to the new list
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public boolean contains(int x, int y) {
		boolean b = false;

		//assumed false, if the click is close enough to one pixel, changes to true
		// if condition is satisfied, the boolean is set to true
		// if the statement isn't true, b stays false

		for (int i = 0; i < points.size()- 1; i++) {
			if (Segment.pointToSegmentDistance(x, y, (int) points.get(i).getX(), (int) points.get(i).getY(), (int) points.get(i+1).getX(), (int) points.get(i+1).getY()) <= 3) {
				b = true;
			}
		}

		return b;
	}

	@Override
	public void draw(Graphics g) {		// draws line
		g.setColor(color);

		if (points.size() > 2) {		// at least 3 points are needed for a multi-jointed segment
			for (int i = 0; i < points.size() - 1; i++) {
				// draw lines point to point
				g.drawLine((int) points.get(i).getX(), (int) points.get(i).getY(), (int) points.get(i + 1).getX(), (int) points.get(i + 1).getY());
			}
		}
	}

	@Override
	public String toString() {return "polyline " + color.getRGB() + " " + pointsToString();
	}
}

