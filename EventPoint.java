import java.util.*;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.event.*;

public class EventPoint implements Comparable<EventPoint>{
	Point point;
	
	public EventPoint(Point p){
		point = p;
	}
	
	public int compareTo(EventPoint o){
		if (point.y != o.point.y){
			return point.y - o.point.y;
		}else{
			return point.x - o.point.x;
		}
	}
}

class SiteEventPoint extends EventPoint{
	public SiteEventPoint(Point p){
		super(p);
	}
}

class CircleEventPoint extends EventPoint{
	// stores the triplet LeafNode representing arcs which causes gamma to shrink
	LeafNode pred, gamma, succ;
	
	public CircleEventPoint(Point p, LeafNode na, LeafNode nb, LeafNode nc){
		super(p);
		pred = na;
		gamma = nb;
		succ = nc;
	}
}	
