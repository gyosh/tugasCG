import java.util.*;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.event.*;

// represent a vertex in voronoi diagram
class Vertex {
	Point point;
	Point[] corr = new Point[3];
	
	public Vertex(Point point, Point a, Point b, Point c){
		this.point = point;
		corr[0] = a;
		corr[1] = b;
		corr[2] = c;
	}
}

class VoronoiEdge {
	Vertex[] endPoint;
	private int pointer;

	public VoronoiEdge(Vertex a, Vertex b) {
		endPoint = new Vertex[2];
		endPoint[0] = a;
		endPoint[1] = b;

		pointer = 0;
		if(a != null) pointer++;
		if(b != null) pointer++;
	}

	public VoronoiEdge(){ this(null, null); }
	public VoronoiEdge(Vertex a){ this(a, null); }

	public void addEndPoint(Vertex a) {
		if(pointer > 1) return;
		endPoint[pointer++] = a;
	}
}

class Pair {
	Point first, second;
	
	public Pair(Point first, Point second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Pair)) return false;
		Pair o = (Pair) other;

		return(this.first.equals(o.first) && this.second.equals(o.second));
	}

	@Override
	public int hashCode() {
		return first.hashCode() * 234231 + second.hashCode() * 42391;
	}
}

public class VoronoiGenerator{
	// event queue
	private static TreeSet<EventPoint> Q;
	
	// status of line sweep
	private static ModifiedAvlTree T;

	// set to store every Voronoi Edge formed so far
	private static HashMap<Pair, VoronoiEdge> H;
	
	// bounding box size
	private static int width = 1280;
	private static int height = 780;

	// get the voronoi diagram
	public static ArrayList<Line2D.Double> getVoronoiDiagram(ArrayList<Point> pointList) {
		// init
		ArrayList<Line2D.Double> ret = new ArrayList<Line2D.Double>();
		ArrayList<Vertex> vertex = new ArrayList<Vertex>();
		T = new ModifiedAvlTree();
		Q = new TreeSet<EventPoint>();
		H = new HashMap<Pair, VoronoiEdge>();
		
		// adds site events
		for (int i = 0; i < pointList.size(); i++){
			Q.add(new SiteEventPoint(pointList.get(i)));
		}
		
		// do the sweep in O(N log N)
		while (!Q.isEmpty()) {
			EventPoint u = Q.pollFirst();
			
			if (u instanceof SiteEventPoint){
				// a site event
				if (T.isEmpty()){
					T.setRoot(new LeafNode(u.point, null, null));	
				}else{
					T.splitArcAndHandleQueue(u.point, Q);
				}
			}else{
				// a circle event
				CircleEventPoint nu = (CircleEventPoint)u;
				
				T.removeArc(nu.gamma, Q, H, vertex);
			}
			
			//T.print();
		}
		
		// construct edges (hashMap implementation) in O(N)
		Iterator<Pair> it = H.keySet().iterator();
		while(it.hasNext()) {
			Pair pp = it.next();
			VoronoiEdge e = H.get(pp);
			
			if (e != null){
				if(e.endPoint[0] != null && e.endPoint[1] != null){
					// an ordinary edge
					ret.add(new Line2D.Double(e.endPoint[0].point.x, e.endPoint[0].point.y,
											  e.endPoint[1].point.x, e.endPoint[1].point.y));
					
					H.put(new Pair(pp.second, pp.first), null);
				}
			}
		}
		
		// create infinity half-edges in O(N)
		ArrayList<InternalNode> infLine = T.getListOfInternalNode();
		
		// store some probably incomplete vertex
		HashMap<Pair, Vertex> I = new HashMap<Pair, Vertex>();
		for (int k = 0; k < vertex.size(); k++){
			for (int i = 0; i < 3; i++){
				for (int j = 0; j < 3; j++){
					Vertex v = vertex.get(k);
					I.put(new Pair(v.corr[i], v.corr[j]), v);
				}
			}
		}
		
		for (InternalNode e : infLine){
			Point u = e.leftFocus;
			Point v = e.rightFocus;
			
			// from voronoi vertex towards mid, infinitely
			Vertex p = I.get(new Pair(u, v));
			
			if (p == null){
				// a line, not line segment
				Point mid = MathHelper.getMidPoint(u, v);
				int gradx = u.y - v.y;
				int grady = v.x - u.x;
				int t = 2000;
				ret.add(MathHelper.boundBox(width, height, new Point(mid.x - t*gradx, mid.y - t*grady), new Point(mid.x + t*gradx, mid.y + t*grady)));
			}else{
				for (int i = 0; i < 3; i++){
					for (int j = 0; j < 3; j++){
						if ((u == p.corr[i]) && (v == p.corr[j])){
							Point mid1 = MathHelper.getMidPoint(p.corr[i], p.corr[j]);
						
							// find other so {i} U {j} U {k} = {0,1,2}
							int k = 3 - i - j;
							Point mid2 = MathHelper.getMidPoint(p.corr[i], p.corr[k]);
							Point mid3 = MathHelper.getMidPoint(p.corr[j], p.corr[k]);
						
							int t = 2000;
							int gradx = mid1.x - p.point.x;
							int grady = mid1.y - p.point.y;
						
							int ar1 = MathHelper.areaSign(mid2, p.point, mid1);
							int ar2 = MathHelper.areaSign(mid2, p.point, mid3);
						
							if (!MathHelper.isBow(p.corr[i],p.corr[j],p.corr[k]) && 
							    !MathHelper.isInside(p.point, p.corr[0], p.corr[1], p.corr[2])){
								gradx *= -1;
								grady *= -1;
							}
						
							ret.add(MathHelper.boundBox(width, height, new Point(p.point.x, p.point.y), new Point(p.point.x + t*gradx, p.point.y + t*grady)));
						}		
					}
				}
			}
		}
		
		return ret;
	}
}
