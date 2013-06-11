import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.Math;
import java.awt.geom.Line2D;

public class MathHelper{
	// check area sign
	public static int areaSign(Point a, Point b, Point c) {
		double px = b.x - a.x;
		double py = b.y - a.y;

		double qx = c.x - b.x;
		double qy = c.y - b.y;

		double result = px * qy - qx * py;

		if (result < 0) {
			return -1;
		}
		if (result > 0) {
			return 1;
		}
		return 0;
	}

	// find rounded euclidean distance
	public static int getDistance(Point a, Point b){
		int dx = a.x - b.x;
		int dy = a.y - b.y;
		return (int)Math.round(Math.sqrt(dx*dx + dy*dy));
	}
	
	// find squared euclidean distance
	public static long getSquaredDistance(Point a, Point b){
		long dx = a.x - b.x;
		long dy = a.y - b.y;
		return dx*dx + dy*dy;
	}
	
	//returns the rounded x coordinate of two arcs meeting point
	public static int getArcMeetingX(Point f1, Point f2, int ly){
		double A1, B1, C1, A2, B2, C2;
		double K1, K2;
		
		K1 = 1./(2*(f1.y - ly));
		A1 = K1;
		B1 = K1*(-2*f1.x);
		C1 = K1*(f1.x*f1.x + f1.y*f1.y - ly*ly);
		
		K2 = 1./(2*(f2.y - ly));
		A2 = K2;
		B2 = K2*(-2*f2.x);
		C2 = K2*(f2.x*f2.x + f2.y*f2.y - ly*ly);
		
		ArrayList<Integer> intersection = MathHelper.getParabolaRoot(A1 - A2, B1 - B2, C1 - C2);
		Collections.sort(intersection);
		
		Point lo = f1;
		Point hi = f2;
		if (f1.y > f2.y){
			Point t = lo;
			lo = hi;
			hi = t;
		}
		
		if (f1 == lo && f2 == hi) return intersection.get(0);
		return intersection.get(1);
	}
	
	//returns rounded root(s) of a parabola
	public static ArrayList<Integer> getParabolaRoot(double A, double B, double C){
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		double discr = B*B - 4*A*C;
		
		//always double roots
		double x;
		
		x = (-B - Math.sqrt(discr))/(2*A);
		ret.add((int)Math.round(x));
		
		x = (-B + Math.sqrt(discr))/(2*A);
		ret.add((int)Math.round(x));
				
		return ret;
	}
	
	// returns the rounded center point of circle defined by 3 points
	public static Point getCircleCenter(Point a, Point b, Point c){
		int A1, B1, C1, A2, B2, C2;
		
		A1 = 2*(b.x - a.x);
		B1 = 2*(b.y - a.y);
		C1 = (b.x*b.x + b.y*b.y) 
		   - (a.x*a.x + a.y*a.y);
		
		A2 = 2*(c.x - a.x);
		B2 = 2*(c.y - a.y);
		C2 = (c.x*c.x + c.y*c.y) 
		   - (a.x*a.x + a.y*a.y);

		int det = A1*B2 - B1*A2;
		int xo = (C1*B2 - C2*B1);
		int yo = (C2*A1 - C1*A2);
		
		if (det != 0){
			double x = (double)xo / det;
			double y = (double)yo / det;
			
			return new Point((int)Math.round(x), (int)Math.round(y));
		}else{
			// they're colinear
			return null;
		}
	}
	
	// test if p is inside triangle (a,b,c)
	public static boolean isInside(Point p, Point a, Point b, Point c){
		int a1 = MathHelper.areaSign(a,b,p);
		int a2 = MathHelper.areaSign(b,c,p);
		int a3 = MathHelper.areaSign(c,a,p);
		
		return ((a1 == a2) && (a2 == a3)) || (a1*a2*a3 == 0);
	}
	
	// get the middle point from a line (u,v)
	public static Point getMidPoint(Point u, Point v){
		return new Point((u.x + v.x)/2, (u.y + v.y)/2);
	}
	
	public static boolean isBow(Point a, Point b, Point c){
		//System.out.println(getSquaredDistance(a,b) + " " + (getSquaredDistance(a,c) + getSquaredDistance(c,b)));
		return (getSquaredDistance(a,b) <= Math.max(getSquaredDistance(a,c), getSquaredDistance(c,b)));
	}

	// returns whether line (A1, A2) intersects (B1,B2) non paralelly
	public static boolean isIntersect(Point A1, Point A2, Point B1, Point B2){
		int h1 = areaSign(A1,A2, B1);
		int h2 = areaSign(A1,A2, B2);
		int h3 = areaSign(B1,B2, A1);
		int h4 = areaSign(B1,B2, A2);
		
		return ((h1*h2 <= 0) && (h3*h4 <= 0) && !(h1==0 && h2==0 && h3==0 && h4==0));
	}

	// returns the magnitude of point vector p
	public static double magnitude(Point p){
		double res = (double)p.x*p.x + (double)p.y*p.y;
		return Math.sqrt(res);
	}

	// get cross product of point vector a and b
	public static double crossProduct(Point a, Point b){
		return (double)a.x*b.y - (double)a.y*b.x;
	}

	// get intersection point
	public static Point getIntersection(Point A1, Point A2, Point B1, Point B2){
		Point a = new Point(B1.x-A1.x, B1.y-A1.y);
		Point b = new Point(A2.x-A1.x, A2.y-A1.y);
		double h1 = Math.abs(crossProduct(a,b)/magnitude(b));

		Point c = new Point(B2.x-A1.x, B2.y-A1.y);
		double h2 = h1 + Math.abs(crossProduct(c,b)/magnitude(b));

		double xp = B1.x + (h1/h2)*(B2.x - B1.x);
		double yp = B1.y + (h1/h2)*(B2.y - B1.y);
		
		return new Point((int)xp, (int)yp);
	}

	// bound a box within the line segment, cutting the line's part which fall out of the box
	public static Line2D.Double boundBox(int width, int height, Point A, Point B){
		ArrayList<Point> intersection = new ArrayList<Point>();

		Point NW = new Point(0,0);
		Point NE = new Point(width,0);
		Point SW = new Point(0,height);
		Point SE = new Point(width,height);

		if (isIntersect(A,B, NW,NE)) intersection.add(getIntersection(A,B,NW,NE));
		if (isIntersect(A,B, NE,SE)) intersection.add(getIntersection(A,B,NE,SE));
		if (isIntersect(A,B, SE,SW)) intersection.add(getIntersection(A,B,SE,SW));
		if (isIntersect(A,B, SW,NW)) intersection.add(getIntersection(A,B,SW,NW));

		if (intersection.size() == 2){
			// both end point of (A,B) is out of the box, return their intersections with box
			return new Line2D.Double(intersection.get(0).x, intersection.get(0).y,
									 intersection.get(1).x, intersection.get(1).y);
		}else if (intersection.size() == 1){
			// one of the end point of (A,B) is inside the box, return it and the intersection
			if ((0 <= A.x) && (A.x <= width) && (0 <= A.y) && (A.y <= height)){
				return new Line2D.Double(A.x, A.y, intersection.get(0).x, intersection.get(0).y);
			}else{
				return new Line2D.Double(B.x, B.y, intersection.get(0).x, intersection.get(0).y);
			}
		}else{
			// both end point is outside, return no line
			return new Line2D.Double(A.x, A.y, A.x, A.y);
		}
	}
}
