package vp.mapping;

import java.awt.geom.Rectangle2D;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * @author oursland
 */
public class QuadMap3 implements LandmarkListener, Cloneable {
	private static final int LL = 2;
	private static final int LG = 1;
	private static final int GL = 3;
	private static final int GG = 0;
	private static final int NQ = -1;
		
	private Node[]	nodes		= new Node[100];
	private int		nextIndex	= 0;
	private int		rootIndex	= 0;
	private int		lmCount		= 0;
	private int		modCount;

	public QuadMap3(double x, double y, double w, double h) {
		this(new Rectangle2D.Double(x, y, w, h));
	}

	public QuadMap3(Rectangle2D bounds) {
		createNode(NQ, bounds, null);
	}

	public QuadMap3(QuadMap3 copy) {
		this.nodes = new Node[copy.nodes.length];
		System.arraycopy(copy.nodes, 0, this.nodes, 0, copy.nodes.length);
		this.nextIndex = copy.nextIndex;
		this.rootIndex = copy.rootIndex;
		this.lmCount = copy.lmCount;
		this.modCount = copy.modCount;
	}

	public Object clone() {
		try {
			QuadMap3 rc = (QuadMap3) super.clone();
			rc.nodes = this.nodes.clone();
			return rc;
		} catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		throw new Error("Unknown error in QuadMap3.");
	}

	private Node getRoot() {
		return nodes[rootIndex];
	}

	public int getLandmarkCount() {
		return lmCount;
	}

	public Iterator iterator() {
		return new QuadMapIterator();
	}

	public boolean contains(GenericLandmark lm) {
		return findLandmarkIndex(lm, rootIndex) != NQ;
	}

	public GenericLandmark[] getLandmarksInBounds(Rectangle2D bounds, GenericLandmark[] result) {
		getLandmarksInBounds(nodes[rootIndex], bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), result, 0);
		return result;
	}

	private int getLandmarksInBounds(final Node node, final double x, final double y, final double w, final double h, final GenericLandmark[] result, int resultIndex ) {
			if( node != null ) {
				if( node.intersects(x, y, w, h) ) {
					if( node.containedBy(x, y, w, h)) {
						resultIndex = getLandmarks(node, result, resultIndex);
					} else {
						if( node.isLandmarkInBounds(x, y, w, h) ) {
							result[resultIndex++] = node.landmark;
						}
						if( node.ggNode != NQ ) resultIndex = getLandmarksInBounds(nodes[node.ggNode], x, y, w, h, result, resultIndex);
						if( node.lgNode != NQ ) resultIndex = getLandmarksInBounds(nodes[node.lgNode], x, y, w, h, result, resultIndex);
						if( node.llNode != NQ ) resultIndex = getLandmarksInBounds(nodes[node.llNode], x, y, w, h, result, resultIndex);
						if( node.glNode != NQ ) resultIndex = getLandmarksInBounds(nodes[node.glNode], x, y, w, h, result, resultIndex);
					}
				}
			}
			return resultIndex;
	}

	private int getLandmarks(Node node, GenericLandmark[] result, int resultIndex) {
		result[resultIndex++] = node.landmark;
		if( node.ggNode != NQ ) resultIndex = getLandmarks(nodes[node.ggNode], result, resultIndex);
		if( node.lgNode != NQ ) resultIndex = getLandmarks(nodes[node.lgNode], result, resultIndex);
		if( node.llNode != NQ ) resultIndex = getLandmarks(nodes[node.llNode], result, resultIndex);
		if( node.glNode != NQ ) resultIndex = getLandmarks(nodes[node.glNode], result, resultIndex);
		return resultIndex;
	}

	public void getLandmarksInBounds(Rectangle2D bounds, List<GenericLandmark> list) {
		getLandmarksInBounds(rootIndex, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), list);
	}

	private void getLandmarksInBounds(int nodeIndex, final double x, final double y, final double w, final double h, List<GenericLandmark> list) {
		final Node node = nodes[nodeIndex];
		if(node != null) {
			if(node.intersects(x, y, w, h)) {
				if(node.isLandmarkInBounds(x, y, w, h)) {
					list.add(node.landmark);
				}
				for(int i = 0; i < 4; i++) {
					int childIndex = getChildIndex(node, i);
					if( childIndex != NQ) {
						getLandmarksInBounds(childIndex, x, y, w, h, list);
					}
				}
			}
		}
	}

	public void add(GenericLandmark lm) {
		findAndInsert(lm, this.rootIndex);
	}

	private void findAndInsert(GenericLandmark lm, int searchIndex) {
		int insertAt = findInsertionIndex(lm, searchIndex);
		Node parent = nodes[insertAt];
		if( !parent.contains(lm.getX(), lm.getY())) {
			throw new Error();
		} else if( parent.landmark == null ) {
			setLandmark(parent, lm);
		} else if( parent.matchesLandmark(lm) ) {
			return;
		} else {
			int quad = parent.getQuadrantIndex(lm.getX(), lm.getY());
			assert parent.getChild(quad) == NQ;
			int childIndex = createNode(parent.index, parent.calculateChildBounds(quad), lm);
			setChild(parent, quad, nodes[childIndex]);
		}
//		printNodes();
	}

	private void printNodes() {
		for(int i = 0; i < nextIndex; i++) {
			System.out.println(nodes[i]);
		}
	}

	public boolean remove(GenericLandmark lm) {
		return findAndRemove(lm, this.rootIndex);
	}

	private boolean findAndRemove(GenericLandmark lm, int searchIndex) {
		boolean rc = false;
		int removeFrom = findLandmarkIndex(lm, searchIndex);
		if(removeFrom != NQ) {
			setLandmark(nodes[removeFrom], null);
			rc = true;
		}
		return rc;
	}
	
	private int findLandmarkIndex(GenericLandmark lm, int searchIndex) {
		int index = findInsertionIndex(lm, searchIndex);
		return nodes[index].matchesLandmark(lm) ? index : NQ;
	}

	private int findInsertionIndex(GenericLandmark lm, int searchIndex) {
		return findLandmark(lm.getX(), lm.getY(), lm, searchIndex);
	}

	private int findLandmark(final double x, final double y, final GenericLandmark lm, int searchIndex) {
		while(searchIndex != NQ) {
			Node node = nodes[searchIndex];
			assert node.contains(x, y);
			if( node.matchesLandmark(lm)) {
				break;
			}
			int quadrant = node.getQuadrantIndex(x, y);
			int childIndex = getChildIndex(nodes[searchIndex], quadrant);
			if( childIndex == NQ ) {
				break;
			}
			searchIndex = childIndex;
		}
		assert searchIndex == rootIndex || nodes[getParentIndex(searchIndex)] != null;
		return searchIndex;
	}

	private int createNode(int parentIndex, Rectangle2D bounds, GenericLandmark lm) {
		growArray();
		nodes[nextIndex] = new Node(parentIndex, nextIndex, bounds, lm, NQ, NQ, NQ, NQ);
		return nextIndex++;
	}

	private void growArray() {
		if(nextIndex >= nodes.length) {
			Node[] temp = new Node[2 * nodes.length + 1];
			System.arraycopy(nodes, 0, temp, 0, nodes.length);
			this.nodes = temp;
		}
	}

	public void setLandmark(Node node, GenericLandmark lm) {
		updateLandmarkCount(node, lm);
		nodes[node.index] = new Node(node.parent, node.index, node.bounds, lm, node.llNode, node.lgNode, node.glNode, node.ggNode);
	}
	
	public void setChild(Node parent, int quad, Node child) {
		switch(quad) {
		case LL:
			nodes[parent.index] = parent.setLL(child.index);
			return;
		case LG:
			nodes[parent.index] = parent.setLG(child.index);
			return;
		case GL:
			nodes[parent.index] = parent.setGL(child.index);
			return;
		case GG:
			nodes[parent.index] = parent.setGG(child.index);
			return;
		}
		throw new Error();
	}

	private void updateLandmarkCount(Node node, GenericLandmark lm) {
		if(node.landmark != null && lm == null) {
			lmCount--;
		} else if(node.landmark == null && lm != null) {
			lmCount++;
		}
	}

	private int getParentIndex(int index) {
		return nodes[index].parent;
	}

	private int getChildIndex(Node node, int quad) {
		switch(quad) {
		case LL:
			return node.llNode;
		case LG:
			return node.lgNode;
		case GL:
			return node.glNode;
		case GG:
			return node.ggNode;
		}
		throw new Error();
	}

	public void cleanup() {
		nodes = null;
		this.lmCount = Integer.MIN_VALUE;
		this.nextIndex = Integer.MIN_VALUE;
		this.rootIndex = Integer.MIN_VALUE;
	}

	private Rectangle2D calculateNodeBounds(int nodeIndex) {
		final int parentIndex = getParentIndex(nodeIndex);
		final int quadrant = getQuadrantInParent(nodeIndex);
		final Node parentNode = nodes[parentIndex];
		if(parentNode == null) {
			assert false; // we never want to be here -- I think
			Rectangle2D parentBounds = calculateNodeBounds(parentIndex);
			return Node.calculateChildBounds(quadrant, parentBounds.getWidth() / 2, parentBounds.getHeight() / 2, parentBounds.getCenterX(), parentBounds.getCenterY(),
					parentBounds.getMinX(), parentBounds.getMinY());
		} else {
			return parentNode.calculateChildBounds(quadrant);
		}
	}

	private int getQuadrantInParent(int childIndex) {
		int parentIndex = nodes[childIndex].parent;
		Node parent = nodes[parentIndex];
		for(int i = 0; i < 4; i++) {
			if(parent.getChild(i) == childIndex) {
				return i;
			}
		}
		return NQ;
	}

	private final static class Node {
		final int				parent;
		final int				index;
		final Rectangle2D		bounds;
		final GenericLandmark	landmark;
		final int				llNode;
		final int				lgNode;
		final int				glNode;
		final int				ggNode;

		public Node(final int parent, final int index, Rectangle2D bounds, final GenericLandmark landmark, final int llNode, final int lgNode, final int glNode, final int ggNode) {
			super();
			this.parent = parent;
			this.index = index;
			this.bounds = bounds;
			this.landmark = landmark;
			this.llNode = llNode;
			this.lgNode = lgNode;
			this.glNode = glNode;
			this.ggNode = ggNode;
		}
		
		public boolean containedBy(double x, double y, double w, double h) {
			return bounds.getMinX() >= x && bounds.getMinY() >=y && bounds.getMaxX() <= (x+w) && bounds.getMaxY() <= (y+h);
		}

		public String toString() {
			return index + " (" + llNode + " " + lgNode + " " + glNode + " " + ggNode +") " + landmark; 
		}

		public int getChild(int i) {
			switch(i) {
			case LL:
				return llNode;
			case LG:
				return lgNode;
			case GL:
				return glNode;
			case GG:
				return ggNode;
			}
			throw new Error();
		}

		public int getQuadrantIndex(double x, double y) {
			int rc;
			if(x < bounds.getCenterX()) { // ly
				if(y < bounds.getCenterY()) { // xl
					rc = LL;
				} else { // xg
					rc = LG;
				}
			} else { // gy
				if(y < bounds.getCenterY()) { // xl
					rc = GL;
				} else { // xg
					rc = GG;
				}
			}
			return rc;
		}

		public boolean matchesLandmark(GenericLandmark lm) {
			return this.landmark == lm;
		}

		public boolean isLandmarkInBounds(double xmin, double ymin, double w, double h) {
			if( this.landmark == null) return false;
			final double xmax = xmin+w;
			final double ymax = ymin+h;
			final double x = this.landmark.getX();
			final double y = this.landmark.getY();
			return (x >= xmin ) && ( x < xmax) && (y >= ymin) && (y < ymax);
		}

		public boolean intersects(double x, double y, double w, double h) {
			return this.bounds.intersects(x, y, w, h);
		}

		public Node(int parent, int index, Rectangle2D bounds) {
			this(parent, index, bounds, null, NQ, NQ, NQ, NQ);
		}

		public Node setLL(int nodeIndex) {
			return new Node(parent, index, bounds, landmark, nodeIndex, lgNode, glNode, ggNode);
		}

		public Node setLG(int nodeIndex) {
			return new Node(parent, index, bounds, landmark, llNode, nodeIndex, glNode, ggNode);
		}

		public Node setGL(int nodeIndex) {
			return new Node(parent, index, bounds, landmark, llNode, lgNode, nodeIndex, ggNode);
		}

		public Node setGG(int nodeIndex) {
			return new Node(parent, index, bounds, landmark, llNode, lgNode, glNode, nodeIndex);
		}

		public boolean contains(double x, double y) {
			return bounds.contains(x, y);
		}

		public Rectangle2D calculateChildBounds(int childQuadrant) {
			return calculateChildBounds(childQuadrant, this.bounds.getCenterX(), this.bounds.getCenterY(), this.bounds.getMinX(), this.bounds.getMinY(), this.bounds.getMaxX(),
					this.bounds.getMaxY());
		}

		public static Rectangle2D calculateChildBounds(int childQuadrant, final double dividerX, final double dividerY, final double minX, final double minY, final double maxX,
				final double maxY) {
			switch(childQuadrant) {
			case LL: // ll
				return new Rectangle2D.Double(minX, minY, dividerX - minX, dividerY - minY);
			case LG: // lg
				return new Rectangle2D.Double(minX, dividerY, dividerX - minX, maxY - dividerY);
			case GL: // gl
				return new Rectangle2D.Double(dividerX, minY, maxX - dividerX, dividerY - minY);
			case GG: // gg
				return new Rectangle2D.Double(dividerX, dividerY, maxX - dividerX, maxY - dividerY);
			}
			throw new IllegalArgumentException("Quadrant must be in the range [1..4]");
		}
		
		public boolean hasLandmark() {
			return landmark != null;
		}
	}

	private class QuadMapIterator implements Iterator {
		private int			modCount	= QuadMap3.this.modCount;
		private Node		lastNode	= null;
		private LinkedList<Node>	stack		= new LinkedList<Node>();

		public QuadMapIterator() {
			pushNode(QuadMap3.this.getRoot());
		}

		public boolean hasNext() {
			if(modCount != QuadMap3.this.modCount) {
				throw new ConcurrentModificationException();
			}
			boolean nonEmptyStack = stack.size() > 0;
			boolean childrenOfLast = (lastNode != null) && (hasLandmark(lastNode.ggNode) || hasLandmark(lastNode.glNode) || hasLandmark(lastNode.lgNode) || hasLandmark(lastNode.llNode));
			return nonEmptyStack || childrenOfLast;
		}
		
		private boolean hasLandmark(int index) {
			if(index != NQ)
				return nodes[index].hasLandmark();
			else
				return false;
		}

		public Object next() {
			if(modCount != QuadMap3.this.modCount) {
				throw new ConcurrentModificationException();
			}
			if(lastNode != null) {
				pushChildren(lastNode);
			}
			if(stack.size() == 0) {
				throw new NoSuchElementException();
			}
			Node node = stack.removeFirst();
			lastNode = node;
			return lastNode.landmark;
		}

		private boolean pushChildren(Node parentNode) {
			boolean rc = false;
			rc = pushNode(parentNode.ggNode) | rc;
			rc = pushNode(parentNode.glNode) | rc;
			rc = pushNode(parentNode.lgNode) | rc;
			rc = pushNode(parentNode.llNode) | rc;
			return rc;
		}

		public void remove() {
			if(modCount != QuadMap3.this.modCount) {
				throw new ConcurrentModificationException();
			}
			if(lastNode == null) {
				throw new IllegalStateException();
			}
			modCount++;
			QuadMap3.this.modCount++;
			setLandmark(lastNode, null);
			// remove reuses the node so we push it back on the stack
			pushNode(lastNode);
			lastNode = null;
		}

		private boolean pushNode( int index ) {
			if( index != NQ ) {
				Node node = nodes[index];
				if( node.landmark == null ) {
					return pushChildren(node);
				} else {
					return pushNode(node);				
				}
			}
			return false;
		}
		
		private boolean pushNode(Node node) {
			if(node.landmark != null) {
				stack.addFirst(node);
				return true;
			} else {
				return false;
			}
		}
	}

	public static void main(String[] args) {
		Random r = new Random(0);
		QuadMap3 map = new QuadMap3(-5000, -5000, 10000, 10000);
		for(int i = 0; i < 10; i++) {
			LandmarkObservation lm = new LandmarkObservation(1000 * r.nextGaussian(), 1000 * r.nextGaussian(), null);
//			System.out.println("Adding landmark " + i);
			map.add(lm);
		}
		Iterator iter = map.iterator();
		while(iter.hasNext()) {
			Object next = iter.next();
//			System.out.println(next);
		}
	}

//	public boolean add(GenericLandmark lm) {
//	return insert(getRoot(), lm);
//}
//
//private boolean insert(Node parent, GenericLandmark lm) {
//	if(parent.contains(lm.getX(), lm.getY())) {
//		if(parent.landmark == null) {
//			nodes[parent.index] = setLandmark(parent, lm);
//			return true;
//		}
//		final int childIndex = getQuadrant(parent, lm);
//		return insert(nodes[childIndex], lm);
//	}
//	throw new Error("Tried to add a landmark out of bounds.");
//}
//
//public int getQuadrant(Node node, GenericLandmark lm) {
//	final Rectangle2D b = node.bounds;
//	final double cx = b.getCenterX();
//	final double cy = b.getCenterY();
//	final double hw = b.getWidth() / 2;
//	final double hh = b.getHeight() / 2;
//	final double mx = b.getMinX();
//	final double my = b.getMinY();
//	if(lm.getX() < cx) {
//		if(lm.getY() < cy) {
//			if(node.lgNode == -1) {
//				node.setLG(createNode(node.index, new Rectangle2D.Double(mx, my, hw, hh)));
//			}
//			return node.lgNode;
//		} else {
//			if(node.ggNode == NQ) {
//				node.setGG(createNode(node.index, new Rectangle2D.Double(mx, cy, hw, hh)));
//			}
//			return node.ggNode;
//		}
//	} else {
//		if(lm.getY() < cy) {
//			if(node.llNode == NQ) {
//				node.setLL(createNode(node.index, new Rectangle2D.Double(cx, my, hw, hh)));
//			}
//			return node.llNode;
//		} else {
//			if(node.glNode == NQ) {
//				node.setGL(createNode(node.index, new Rectangle2D.Double(cx, cy, hw, hh)));
//			}
//			return node.glNode;
//		}
//	}
//}

}