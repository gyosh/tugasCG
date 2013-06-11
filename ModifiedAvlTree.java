import java.util.*;
import java.awt.*;
import java.awt.event.*;

// node for 
class Node{
	int height;
	Node parent;
	
	public Node(){
		height = 0;
	}
}	

// stores foci of currently intersecting arcs
class InternalNode extends Node{
	Point leftFocus, rightFocus;
	Node left,right;
	
	public InternalNode(Point leftFocus, Point rightFocus, Node left, Node right){
		this.leftFocus = leftFocus;
		this.rightFocus = rightFocus;
		this.left = left;
		this.right = right;
	}
	
	public boolean equals(InternalNode other){
		return (leftFocus == other.leftFocus) && (rightFocus == other.rightFocus);
	}
}

// stores focus of an arc, plus the predecessor and successor arc's focus
class LeafNode extends Node{
	Point focus;
	LeafNode pred, succ;
	CircleEventPoint shrink;
	
	public LeafNode(Point focus){
		this.focus = focus;
		pred = succ = null;
		shrink = null;
	}
	
	public LeafNode(Point focus, LeafNode pred, LeafNode succ){
		this.focus = focus;
		this.pred = pred;
		this.succ = succ;
	}
	
	public boolean equals(LeafNode other){
		return (focus == other.focus) && (pred == other.pred) && (succ == other.succ);
	}
}

public class ModifiedAvlTree{
	private Node root;
	private LeafNode leftMost;
		
	public ModifiedAvlTree(){
		root = null;
		leftMost = null;
	}
	
	public boolean isEmpty(){
		return (root == null);
	}
	
	public void setRoot(Node r){
		root = r;
		root.parent = null;
		leftMost = (LeafNode)r;
	}
	
	public void splitArcAndHandleQueue(Point p, TreeSet<EventPoint> Q){
		root = splitArcAndHandleQueue(root, p, Q);
	}
	
	// to the splitting arc and find the new circle event
	public Node splitArcAndHandleQueue(Node t, Point p, TreeSet<EventPoint> Q){
		if (t instanceof LeafNode){
			// split here
			// insert a tree like this
			//   ab
			//   /\
			//  a ba
			//    /\
			//   b a
			
			LeafNode lt = (LeafNode)t;

			// remove false alarm
			if (lt.shrink != null){
				Q.remove(lt.shrink);
				lt.shrink = null;
			}
			
			LeafNode pastPred = lt.pred;
			LeafNode pastSucc = lt.succ;
			
			LeafNode t1 = new LeafNode(lt.focus);
			LeafNode t2 = new LeafNode(p);
			LeafNode t3 = new LeafNode(lt.focus);
			
			if (pastPred != null) pastPred.succ = t1;
			else leftMost = t1;
			
			t1.pred = lt.pred;
			t1.succ = t2;
			
			t2.pred = t1;
			t2.succ = t3;
			
			t3.pred = t2;
			t3.succ = lt.succ;
			
			if (pastSucc != null) pastSucc.pred = t3;
			
			Node p2 = new InternalNode(p, lt.focus, t2, t3);
			Node p1 = new InternalNode(lt.focus, p, t1, p2);

			t1.parent = p1;
			t2.parent = p2;
			t3.parent = p2;
			
			p2.parent = p1;
			p1.parent = t.parent;

			t = p1;
			
			t1.height = 0;
			t2.height = 0;
			t3.height = 0;
			p2.height = 1;
			p1.height = 2;
			
			findCircleEvent(t2, Q);
		}else{
			InternalNode it = (InternalNode)t;
		
			// find split arc
			int x = MathHelper.getArcMeetingX(it.leftFocus, it.rightFocus, p.y);
			
			if (p.x <= x){
				it.left = splitArcAndHandleQueue(it.left, p, Q);
			}else{
				it.right = splitArcAndHandleQueue(it.right, p, Q);
			}
			
			t = it;
		}
		return t;
	}
	
	// check wether the leaf cur still has two neighboring leafs to create circle event
	public void handleFalseAlarm(LeafNode cur, LeafNode toDissapear, TreeSet<EventPoint> Q){
		if (cur == null) return;
		
		CircleEventPoint event = cur.shrink;
		if (event == null) return;
		
		Q.remove(event);
		cur.shrink = null;
	}	
	
	// remove an arc (gamma) from the tree + create/erase some circle event
	public void removeArc(LeafNode gamma, TreeSet<EventPoint> Q, HashMap<Pair, VoronoiEdge> H, ArrayList<Vertex> V) {		
		InternalNode par = (InternalNode)gamma.parent;
		LeafNode pred = gamma.pred;
		LeafNode succ = gamma.succ;
		
		// removes potential false alarm
		handleFalseAlarm(pred, gamma, Q);
		handleFalseAlarm(succ, gamma, Q);
		 
		// add this to voronoi vertex list
		Point v = MathHelper.getCircleCenter(gamma.focus, pred.focus, succ.focus);
		Vertex newV = new Vertex(v, gamma.focus, pred.focus, succ.focus);
		V.add(newV);
		
		// add the newly formed voronoi vertex as endpoint to every bisector of three points
		Point arrayPoint[] = new Point[3];
		arrayPoint[0] = gamma.focus;
		arrayPoint[1] = pred.focus;
		arrayPoint[2] = succ.focus;

		for(int i = 0; i < 3; i++)
			for(int j = i + 1; j < 3; j++) {
				VoronoiEdge toBeEdited;
				Pair ij = new Pair(arrayPoint[i], arrayPoint[j]);
				Pair ji = new Pair(arrayPoint[j], arrayPoint[i]);

				if(H.get(ij) == null || H.get(ji) == null) {
					toBeEdited = new VoronoiEdge(newV);
					H.put(ij, toBeEdited);
					H.put(ji, toBeEdited);
				} else {
					toBeEdited = H.get(ij);
					toBeEdited.addEndPoint(newV);
				}
			}
		

		pred.succ = succ;
		succ.pred = pred;
		
		// will deletion of gamma causes circle event? check it!
		handleShrinkingArc(pred, Q);
		handleShrinkingArc(succ, Q);
		
		// fix the tree
		Node sibling;
		InternalNode grandPar;
		if (par.left == gamma){
			// promote right sibling (succ)
			sibling = ((InternalNode)par).right;
			
			// attaching them
			grandPar = (InternalNode)par.parent;
			if (grandPar.left == par){
				grandPar.left = sibling;
			}else{
				grandPar.right = sibling;
			}
			sibling.parent = grandPar;
			
			// find LCA, modify the tuples in internal node
			ArrayList<Node> one = new ArrayList<Node>();
			for (Node cur = succ.parent; cur != null; cur = cur.parent){
				one.add(cur);
			}	
			one = reversePath(one);
			
			ArrayList<Node> two = new ArrayList<Node>();
			for (Node cur = pred.parent; cur != null; cur = cur.parent){
				two.add(cur);
			}
			two = reversePath(two);
			
			int mismatch = 0;
			while (one.get(mismatch) == two.get(mismatch)){ 
				mismatch++;
				if (mismatch >= one.size() || mismatch >= two.size()) break;
			}
			mismatch--;
			
			InternalNode lca = (InternalNode)one.get(mismatch);
			lca.rightFocus = succ.focus;
			
		}else{
			// promote left sibling (pred)
			sibling = ((InternalNode)par).left;
			
			// attaching them
			grandPar = (InternalNode)par.parent;
			if (grandPar.left == par){
				grandPar.left = sibling;
			}else{
				grandPar.right = sibling;
			}
			sibling.parent = grandPar;
			
			// find LCA, modify the tuples in internal node
			ArrayList<Node> one = new ArrayList<Node>();
			for (Node cur = pred.parent; cur != null; cur = cur.parent){
				one.add(cur);
			}	
			one = reversePath(one);
			
			ArrayList<Node> two = new ArrayList<Node>();
			for (Node cur = succ.parent; cur != null; cur = cur.parent){
				two.add(cur);
			}
			two = reversePath(two);
			
			int mismatch = 0;
			while (one.get(mismatch) == two.get(mismatch)){ 
				mismatch++;
				if (mismatch >= one.size() || mismatch >= two.size()) break;
			}
			mismatch--;
			
			InternalNode lca = (InternalNode)one.get(mismatch);
			lca.leftFocus = pred.focus;
		}
	}	
	
	// newly added cur, test the circle event in the left/right
	public void findCircleEvent(LeafNode cur, TreeSet<EventPoint> Q){
		handleShrinkingArc(cur.pred, Q);
		handleShrinkingArc(cur.succ, Q);
	}
	
	// check wether cur shrinks and causes circle event, register it to event queue
	public void handleShrinkingArc(LeafNode cur, TreeSet<EventPoint> Q){
		LeafNode prevLeaf = cur.pred;
		LeafNode nextLeaf = cur.succ;
		
		// must have 3 consecutive arcs
		if (prevLeaf == null || nextLeaf == null){
			return;
		}
		
		Point pre = prevLeaf.focus;
		Point now = cur.focus;
		Point nex = nextLeaf.focus;
		
		Point potentialEvent = MathHelper.getCircleCenter(pre, now, nex);
		// converge?
		if ((potentialEvent != null) && converge(pre, now, nex)){
			int r = MathHelper.getDistance(potentialEvent, now);
			Point bottom = new Point(potentialEvent.x, potentialEvent.y + r);
			CircleEventPoint event = new CircleEventPoint(bottom, prevLeaf, cur, nextLeaf);
			Q.add(event);
			
			// register this circle point event to the corresponding leaf
			cur.shrink = event;
		}
	}	
	
	// determines the middle of three arcs shrinks or not
	private boolean converge(Point a, Point b, Point c){
		return MathHelper.areaSign(a,b,c) > 0;
	}
	
	// reverse a list of node
	private ArrayList<Node> reversePath(ArrayList<Node> a){
		ArrayList<Node> ans = new ArrayList<Node>();
		for (int i = a.size()-1; i >= 0; i--){
			ans.add(a.get(i));
		}
		return ans;
	}
	
	// print current leaves in tree, for debugging purpose
	public void print(){
		LeafNode a = leftMost;
		while (a != null){
			a = a.succ;		
		}
		System.out.println();
		System.out.println();
	}
	
	// get the currently available internal node
	public ArrayList<InternalNode> getListOfInternalNode(){
		ArrayList<InternalNode> ret = new ArrayList<InternalNode>();
		
		gatherInternalNode(root, ret);
		return ret;
	}
	
	// recursively adds internal node to list
	private void gatherInternalNode(Node t, ArrayList<InternalNode> list){
		if (t instanceof InternalNode){
			InternalNode it = (InternalNode)t;
			
			list.add(it);
			gatherInternalNode(it.left, list);
			gatherInternalNode(it.right, list);
		}
	}
	
	// AVL's utility, rebalancing stuff (currently not being used)
	private void updateHeight(Node x){
		x.height = max(((InternalNode)x).left.height, ((InternalNode)x).right.height) + 1;
	}
	
	private Node rebalance(Node x){
		updateHeight(x);
		if ((x == null) || (x.height <= 2)) return x;
		
		//guaranteed to have height > 2
		InternalNode t = (InternalNode)x;
		
		if (t.left.height - t.right.height >= 2){
			InternalNode tl = (InternalNode)t.left;
			if (tl.left.height > tl.right.height){
				x = rotateWithLeftChild(x);	
			}else{
				x = doubleWithLeftChild(x);
			}	
		}else if (t.right.height - t.left.height >= 2){
			InternalNode tr = (InternalNode)t.right;
			if (tr.left.height > tr.right.height){
				x = doubleWithRightChild(x);	
			}else{
				x = rotateWithRightChild(x);
			}
		}	
		
		updateHeight(x);
		return x;
	}
	
	private Node rotateWithLeftChild(Node k2) {
		Node k1 = ((InternalNode)k2).left;
		((InternalNode)k2).left = ((InternalNode)k1).right;
		((InternalNode)k1).right = k2;
		
		updateHeight(k2);
		updateHeight(k1);
		
		k1.parent = k2.parent;
		k2.parent = k1;
		((InternalNode)k2).left.parent = k2;
		
		return k1;
    }

    private Node rotateWithRightChild(Node k1) {
		Node k2 = ((InternalNode)k1).right;
		((InternalNode)k1).right = ((InternalNode)k2).left;
		((InternalNode)k2).left = k1;
	
		updateHeight(k1);
		updateHeight(k2);
		
		k2.parent = k1.parent;
		k1.parent = k2;
		((InternalNode)k1).right.parent = k1;
	
		return k2;
    }

    private Node doubleWithLeftChild(Node k3) {
		((InternalNode)k3).left = rotateWithRightChild(((InternalNode)k3).left);
		return rotateWithLeftChild(k3);
    }

    private Node doubleWithRightChild(Node k1) {
		((InternalNode)k1).right = rotateWithLeftChild(((InternalNode)k1).right);
		return rotateWithRightChild(k1);
    }

    private int max(int lhs, int rhs) {
        return lhs > rhs ? lhs : rhs;
    }
}
