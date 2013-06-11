//**************************************************************
//  PointsPanel.java	   
//
//  Represents the primary panel for user to enter points.
//*************************************************************

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.event.*;

public class PointsPanel extends JPanel {
	private ArrayList<Point> pointList;
	private ArrayList<Point> hull;
	private ArrayList<Line2D.Double> voronoiDiagram;
	private JLabel counterText, areaText, closestPairText;
	private int pointCounter;

	private Point pairA, pairB;
	private boolean showConvexHull, showClosestPair, showVoronoi;
	
	//------------------------------------------------------------
	//  Constructor: 
	//  Sets up this panel to listen for mouse events.	   
	//-----------------------------------------------------------
	public PointsPanel(JLabel counterText, JLabel areaText, JLabel closestPairText) {
		pointList = new ArrayList<Point>();
		hull = new ArrayList<Point>();
		showConvexHull = showClosestPair = showVoronoi = false;

		addMouseListener(new PointsListener());

		setBackground(Color.black);
		setPreferredSize(new Dimension(300, 200));
		
		this.counterText = counterText;
		this.areaText = areaText;
		this.closestPairText = closestPairText;
		
		pointCounter = 0;
		setInfo(0.0);
	}

	//------------------------------------------------------------
	//  Draws all of the points stored in the list.
	//-----------------------------------------------------------
	public void paintComponent(Graphics page) {
		super.paintComponent(page);

		page.setColor(Color.green);

		for (Point spot : pointList) {
			page.fillOval(spot.x - 2, spot.y - 2, 5, 5);
		}

		// 
		if(showConvexHull) {
			if(pointList.size() > 2) convexHull();
			for (int i = 0; i < hull.size(); i++) 
				page.drawLine(hull.get(i).x, hull.get(i).y, hull.get(succ(i, hull)).x, hull.get(succ(i, hull)).y);
		}
		
		if(showClosestPair) {
			if(pointList.size() > 1) findClosestPair();

			if(pairA != null && pairB != null) {
				page.setColor(Color.blue);
				page.fillOval(pairA.x - 2, pairA.y - 2, 5, 5);
				page.fillOval(pairB.x - 2, pairB.y - 2, 5, 5);
				
				page.drawLine(pairA.x, pairA.y, pairB.x, pairB.y);
			}
		}
		
		if (showVoronoi){
			if (pointList.size() > 1) findVoronoi();
			
			((Graphics2D) page).setColor(Color.RED);
			for (int i = 0; i < voronoiDiagram.size(); i++){
				((Graphics2D) page).draw(voronoiDiagram.get(i));
			}
		}
	}

	public void updateConvexHullFlag(boolean newValue) { showConvexHull = newValue; repaint(); }
	public void updateClosestPairFlag(boolean newValue) { showClosestPair = newValue; repaint(); }
	public void updateVoronoiFlag(boolean newValue){ showVoronoi = newValue; repaint(); }
	
	//------------------------------------------------------------
	//  Clear all of the points in the list
	//-----------------------------------------------------------
	public void clear() {
		pointList = new ArrayList<Point>();
		hull = new ArrayList<Point>();
		voronoiDiagram = new ArrayList<Line2D.Double>();
		pairA = pairB = null;
		repaint();
		
		pointCounter = 0;
		setInfo(0.0);
	}
	
	//------------------------------------------------------------
	//  Updates the number of points and area of convex hull to be displayed
	//-----------------------------------------------------------
	private void setInfo(double areaNum) {
		counterText.setText(" Counter : " + pointCounter);
		
		if(pairA == null) closestPairText.setText(" Closest Distance : -");
		else closestPairText.setText(String.format(" Closest Distance : %.5g", EuclidDistance(pairA, pairB)));
		
		if(areaNum > -0.5) areaText.setText(" Area	   : " + areaNum);
	}

	//------------------------------------------------------------
	//  Returns -1 if right turn, 1 if left turn, 0 if collinear
	//-----------------------------------------------------------
	private int areaSign(Point a, Point b, Point c) {
		int px = b.x - a.x;
		int py = b.y - a.y;

		int qx = c.x - b.x;
		int qy = c.y - b.y;

		int result = px * qy - qx * py;

		if (result < 0) {
			return -1;
		}
		if (result > 0) {
			return 1;
		}
		return 0;
	}
	
	//------------------------------------------------------------
	//  Checks the following points turn
	//-----------------------------------------------------------
	private boolean leftTurn(Point a, Point b, Point c) { return (areaSign(a, b, c) > 0); }
	private boolean rightTurn(Point a, Point b, Point c) { return (areaSign(a, b, c) < 0); }
	private boolean collinear(Point a, Point b, Point c) { return (areaSign(a, b, c) == 0); }

	//------------------------------------------------------------
	//  Checks if Points in pointList from index left to right 
	//  are all collinear
	//-----------------------------------------------------------
	private boolean allCollinear(int left, int right) {
		boolean result = true;

		// check if the first two points and each other points are collinear
		for (int i = left + 2; i <= right; i++) {
			if (!collinear(pointList.get(left), pointList.get(left + 1), pointList.get(i))) {
				// one of them are not collinear
				result = false;
				break;
			}
		}
		return result;
	}

	//------------------------------------------------------------
	//  Returns the index of right most point in arr
	//-----------------------------------------------------------
	private int rightMostID(ArrayList<Point> arr) {
		int result = 0;
		for (int i = 0; i < arr.size(); i++) {
			if (arr.get(result).x < arr.get(i).x) {
				result = i;
			}
		}
		return result;
	}

	//------------------------------------------------------------
	//  Returns the index of left most point in arr
	//-----------------------------------------------------------
	private int leftMostID(ArrayList<Point> arr) {
		int result = 0;
		for (int i = 0; i < arr.size(); i++) {
			if (arr.get(result).x > arr.get(i).x) {
				result = i;
			}
		}
		return result;
	}

	//------------------------------------------------------------
	//  Returns the predecessor index of x in arr
	//-----------------------------------------------------------
	private int pred(int x, ArrayList<Point> arr) {
		int res = x - 1;
		if (res < 0) {
			res = arr.size() - 1;
		}
		return res;
	}

	//------------------------------------------------------------
	//  Returns the successor index of x in arr
	//-----------------------------------------------------------
	private int succ(int x, ArrayList<Point> arr) {
		int res = x + 1;
		if (res >= arr.size()) {
			res = 0;
		}
		return res;
	}

	//------------------------------------------------------------
	//  Let B = p1'th element in arrayList a, A = the element before B
	//  and C = p2'th element in arrayList b, D = the element after C
	//-----------------------------------------------------------
	//  This function checks if (A, B, C) forms a right turn
	//-----------------------------------------------------------
	private boolean goodForUpperLeft(int p1, int p2, ArrayList<Point> a, ArrayList<Point> b){
		return rightTurn(a.get(pred(p1,a)), a.get(p1), b.get(p2));
	}
	//-----------------------------------------------------------
	//  And this function checks if (B, C, D) forms a right turn
	//-----------------------------------------------------------
	private boolean goodForUpperRight(int p1, int p2, ArrayList<Point> a, ArrayList<Point> b){
		return rightTurn(a.get(p1), b.get(p2), b.get(succ(p2,b)));
	}
	
	//------------------------------------------------------------
	//  Let B = p1'th element in arrayList a, A = the element after B
	//  and C = p2'th element in arrayList b, D = the element before C
	//-----------------------------------------------------------
	//  This function checks if (A, B, C) forms a left turn
	//-----------------------------------------------------------
	private boolean goodForLowerLeft(int p1, int p2, ArrayList<Point> a, ArrayList<Point> b){
		return leftTurn(a.get(succ(p1,a)), a.get(p1), b.get(p2));
	}
	//-----------------------------------------------------------
	//  And this function checks if (B, C, D) forms a left turn
	//-----------------------------------------------------------
	private boolean goodForLowerRight(int p1, int p2, ArrayList<Point> a, ArrayList<Point> b){
		return leftTurn(a.get(p1), b.get(p2), b.get(pred(p2,b)));
	}

	//-----------------------------------------------------------
	//  Merges two convex hulls into one convex hull
	//-----------------------------------------------------------
	private ArrayList<Point> merge(ArrayList<Point> a, ArrayList<Point> b){
		int id1, id2;
		int p1, p2;
		
		// get the right most and left most id from corresponding ArrayList
		id1 = rightMostID(a);
		id2 = leftMostID(b);
		
		// used to store the last permittable point when finding the tangent line
		int limP1, limP2;

		// get upper
		p1 = id1;
		p2 = id2;
		limP1 = succ(p1, a);
		limP2 = pred(p2, b);
		// while it is not an appropriate Lower tangent line
		while (!goodForUpperLeft(p1, p2, a, b) || !goodForUpperRight(p1, p2, a, b)){
			// move the right pointer
			while (!goodForUpperRight(p1, p2, a, b)){
				p2 = succ(p2, b);
				if (p2 == limP2) break;
			}
			// move the left pointer
			while (!goodForUpperLeft(p1, p2, a, b)){
				p1 = pred(p1, a);
				if (p1 == limP1) break;
			}

			// when the limits are touched, we need to check if the tangent line is already found
			// if yes, quit immediately
			if ((p1 == limP1) && (p2 == limP2)) break;
			if ((p1 == limP1) && goodForUpperRight(p1, p2, a, b)) break;
			if ((p2 == limP2) && goodForUpperLeft(p1, p2, a, b)) break;
		}
		int ul1 = p1;
		int ul2 = p2;
		
		// get lower
		// very similar to the cobe above. It is just a reflection
		p1 = id1;
		p2 = id2;
		limP1 = pred(p1, a);
		limP2 = succ(p2, b);
		while (!goodForLowerLeft(p1, p2, a, b) || !goodForLowerRight(p1, p2, a, b)){
			while (!goodForLowerRight(p1, p2, a, b)){
				p2 = pred(p2, b);
				if (p2 == limP2) break;
			}
			while (!goodForLowerLeft(p1, p2, a, b)){
				p1 = succ(p1, a);
				if (p1 == limP1) break;
			}
			if ((p1 == limP1) && (p2 == limP2)) break;
			if ((p1 == limP1) && goodForLowerRight(p1, p2, a, b)) break;
			if ((p2 == limP2) && goodForLowerLeft(p1, p2, a, b)) break;
		}
		int ll1 = p1;
		int ll2 = p2;
		
		// now, the tangent lines have been found
		// then we form a new convex hull based from them
		ArrayList<Point> result = new ArrayList<Point>();
		result.add(b.get(ll2));
		result.add(a.get(ll1));
		
		if(ll1 != ul1) while(succ(ll1, a) != ul1) {
			ll1 = succ(ll1, a);
			result.add(a.get(ll1));
		}
		
		if (ll1 != ul1) result.add(a.get(ul1));
		if (ul2 != ll2) result.add(b.get(ul2));
		
		if(ul2 != ll2) while(succ(ul2, b) != ll2) {
			ul2 = succ(ul2, b);
			result.add(b.get(ul2));
		}
		
		return result;
	}

	//-----------------------------------------------------------
	//  Finds a convex hull from pointList, index l to r
	//  Recursively compute convex hull, with divide and conquer algorithm	 
	//-----------------------------------------------------------
	private ArrayList<Point> findConvexHull(int l, int r) {
		int size = r - l + 1;
		ArrayList<Point> result = new ArrayList<Point>();

		// small enough to solve
		if (size <= 3) {
			// simply list them
			for (int i = l; i <= r; i++) {
				result.add(pointList.get(i));
			}

			// keep them clockwise
			if (size == 3) {
				if (collinear(result.get(0), result.get(1), result.get(2))) {
					result.remove(1);
				} else if (!rightTurn(result.get(0), result.get(1), result.get(2))) {
					Point temp = result.get(1);
					result.remove(1);
					result.add(temp);
				}
			}
		} else if (allCollinear(l, r)) {
			//simply list the edges
			result.add(pointList.get(l));
			result.add(pointList.get(r));
		} else {
			int m = (l + r) / 2;

			//find a split
			while ((m + 1 <= r)
					&& (pointList.get(m).x == pointList.get(m + 1).x)) {
				m++;
			}
			if (m + 1 > r) {
				m = (l + r) / 2;
				while (pointList.get(m).x == pointList.get(m + 1).x) {
					m--;
				}
			}

			// independently finds convex hulls
			ArrayList<Point> leftHull = findConvexHull(l, m);
			ArrayList<Point> rightHull = findConvexHull(m + 1, r);

			// merges those found convex hulls into a big one
			result = merge(leftHull, rightHull);
		}

		return result;
	}

	//-----------------------------------------------------------
	//  Removes duplicated elements in arr, given that arr are sorted
	//-----------------------------------------------------------
	private ArrayList<Point> unique(ArrayList<Point> arr) {
		PointCompareX comparator = new PointCompareX();
		ArrayList<Point> result = new ArrayList<Point>();

		result.add(arr.get(0));
		for (int i = 1; i < arr.size(); i++) {
			if (comparator.compare(arr.get(i - 1), arr.get(i)) != 0) {
				result.add(arr.get(i));
			}
		}

		return result;
	}

	//-----------------------------------------------------------
	//  Finds and show the convex hull
	//-----------------------------------------------------------
	public void convexHull() {
		if (pointList.size() <= 1) {
			JOptionPane.showMessageDialog(null, "Please add more points!\n(at least 2 points are needed)");
			return;
		}

		hull = new ArrayList<Point>();

		Collections.sort(pointList, new PointCompareX());
		pointList = unique(pointList);
		hull = findConvexHull(0, pointList.size() - 1);

		repaint();
	}
	
	//-----------------------------------------------------------
	//  Finds the area of convex hull found
	//-----------------------------------------------------------
	public void findArea() {
		if(hull.size() == 0) {
			JOptionPane.showMessageDialog(null, "Please find the convex hull first!\n(press 'Convex Hull' button)");
			return;
		}
		
		int area = 0;
		int N = hull.size();
		
		for(int i = 0; i < N; i++) {
			area += hull.get(i).x * (hull.get((i - 1 + N) % N).y - hull.get((i + 1) % N).y);
		}
		
		area *= 0.5;
		repaint();
		
		setInfo(area);
	}
	
	//-----------------------------------------------------------
	//  Finds the Euclidean distance between A and B
	//-----------------------------------------------------------
	public double EuclidDistance(Point A, Point B) {
		return Math.sqrt(Math.pow(A.x - B.x, 2) + Math.pow(A.y - B.y, 2));
	}
	
	//-----------------------------------------------------------
	//  Finding closest pair between pointList[xLeft..xRight]
	//  Y is the sorted-by-Y points in pointList[xLeft..xRight]
	//  Stores the pair globally in pairA and pairB
	//-----------------------------------------------------------
	public void dividePoint(int xLeft, int xRight, ArrayList<Point> Y) {
		//base case
		if(xRight - xLeft + 1 <= 3) {		
			for(int i = xLeft; i < xRight; i++)
				for(int j = i + 1; j <= xRight; j++) {
					double tempDis = EuclidDistance(pointList.get(i), pointList.get(j));		
					//recall that pairA and pairB are global
					if((pairA == null) || EuclidDistance(pairA, pairB) > tempDis) {
						pairA = pointList.get(i);
						pairB = pointList.get(j);
					}
				}
			
			//no need to proceed
			return;
		}
		
		int xMid = (xLeft + xRight) / 2;
		
		//splitting Y into YLeft and YRight by their x-coordinate
		ArrayList<Point> YLeft, YRight;
		YLeft = new ArrayList<Point>();
		YRight = new ArrayList<Point>();
		PointCompareX comparator = new PointCompareX();
		for(int i = 0; i < Y.size(); i++) {
			Point now = Y.get(i);
			if(comparator.compare(now, pointList.get(xMid)) <= 0) YLeft.add(now); 
			else YRight.add(now);
		}
		
		//recursively finds the closest pair
		dividePoint(xLeft, xMid, YLeft);
		dividePoint(xMid + 1, xRight, YRight);
		
		//distNow stores the distance of closest pair right now
		double disNow = EuclidDistance(pairA, pairB);
		
		//filtering Y, so we get the points in the middle stripe
		ArrayList<Point> Ymid = new ArrayList<Point>();
		for(int i = 0; i < Y.size(); i++) 
			if(Math.abs(Y.get(i).x - pointList.get(xMid).x) <= disNow) Ymid.add(Y.get(i));
		
		//finding the possible closest pair in the middle stripe
		for(int i = 0; i < Ymid.size(); i++)
			//need only to check 7 next points
			for(int j = 1; j <= 7 && i + j < Ymid.size(); j++) {
				double disTemp = EuclidDistance(Ymid.get(i), Ymid.get(i + j));
				if(disTemp < disNow) {
					disNow = disTemp;
					pairA = Ymid.get(i);
					pairB = Ymid.get(i + j);
				}
			}
	}
	
	//-----------------------------------------------------------
	//  Finds the closest pair and display it
	//-----------------------------------------------------------
	public void findClosestPair() {
		if(pointList.size() < 2) {
			JOptionPane.showMessageDialog(null, "Please add more points to calculate closest pair!");
			return;
		}
		
		Collections.sort(pointList, new PointCompareX());
		pointList = unique(pointList);
		
		ArrayList<Point> Y = new ArrayList<Point>();
		for(int i = 0; i < pointList.size(); i++) Y.add(new Point());
		Collections.copy(Y, pointList);
		Collections.sort(Y, new PointCompareY());
		
		dividePoint(0, pointList.size() - 1, Y);
		repaint();
		setInfo(-1);
	}
	
	public void findVoronoi() {
		voronoiDiagram = VoronoiGenerator.getVoronoiDiagram(pointList);
	}
	
	//***********************************************************
	//  Compares two points according to the smaller x first, 
	//  then the smaller y first
	//***********************************************************
	public class PointCompareX implements Comparator<Point> {
		public int compare(final Point a, final Point b) {
			if (a.x != b.x) {
				return a.x - b.x;
			} else {
				return a.y - b.y;
			}
		}
	}
	//***********************************************************
	//  Compares two points according to the smaller y first, 
	//  then the smaller x first
	//***********************************************************
	public class PointCompareY implements Comparator<Point> {
		public int compare(final Point a, final Point b) {
			if (a.y != b.y) {
				return a.y - b.y;
			} else {
				return a.x - b.x;
			}
		}
	}

	//***********************************************************
	//  Represents the listener for mouse events.
	//***********************************************************
	private class PointsListener implements MouseListener {
		//-------------------------------------------------------
		//  Adds the current point to the list of points 
		//  and redraws
		//  the panel whenever the mouse button is pressed.
		//------------------------------------------------------

		public void mousePressed(MouseEvent event) {
			pointList.add(event.getPoint());
			repaint();
			
			pointCounter++;
			setInfo(-1);
		}

		//-----------------------------------------------------
		//  Provide empty definitions for unused event methods.
		//-----------------------------------------------------
		public void mouseClicked(MouseEvent event) {
		}

		public void mouseReleased(MouseEvent event) {
		}

		public void mouseEntered(MouseEvent event) {
		}

		public void mouseExited(MouseEvent event) {
		}
	}
}
