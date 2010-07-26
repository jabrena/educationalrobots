package vp.mapping;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import oursland.collection.CompleteTree;


public final class QuadMap2 extends AbstractCollection<GenericLandmark> implements LandmarkListener, Cloneable {
	private Rectangle2D initialBounds;
	private CompleteTree<QuadMapNode> quadtree;
//	private long[] nodeLandmarkCount = new long[0];
	private TreeMap<Long, Integer> nodeLandmarkCount;
	
	private int modCount;
	
	public QuadMap2(Rectangle2D bounds) {
		initialBounds = bounds;
		quadtree = new CompleteTree<QuadMapNode>(4);
		nodeLandmarkCount = new TreeMap<Long, Integer>();
	}
	
	public QuadMap2(QuadMap2 copy) {
		this.initialBounds = copy.initialBounds;
		this.quadtree = new CompleteTree<QuadMapNode>(copy.quadtree);
		this.nodeLandmarkCount = new TreeMap<Long, Integer>(copy.nodeLandmarkCount);
//		this.nodeLandmarkCount = (int[]) copy.nodeLandmarkCount.clone();
	}
	
	public QuadMap2 clone() {
		try {
			QuadMap2 rc = (QuadMap2)super.clone();
			rc.quadtree = rc.quadtree.clone();
			rc.nodeLandmarkCount = new TreeMap<Long, Integer>(rc.nodeLandmarkCount);
//			rc.nodeLandmarkCount = (int[]) rc.nodeLandmarkCount.clone();
			// notice that this does not clone any of the nodes.
			// the nodes are const objects are are just replaced when they change.
			return rc;
		} catch (CloneNotSupportedException e) {
			throw new Error("QuadMap.clone should have worked.");
		}
	}

	public void cleanup() {
		initialBounds = null;
		quadtree.clear();
		nodeLandmarkCount = null;
	}
	
	private void add0(GenericLandmark lm) {
		QuadMapNode root = getNode(0);
		if( root == null ) {
			initialBounds = expandRectangleBounds(lm.getX(), lm.getY(), initialBounds);
			setNode(0, new QuadMapNode(lm, initialBounds));
		} else {
			expandRootBounds(lm.getX(), lm.getY());
			findAndInsert(lm, 0);
		}
	}
	
	private int validateNodeCounts(long index) {
		int rc = 0;
		if( getNode(index) != null ) {
			rc++;
			rc += validateNodeCounts(getChildIndex(index, 1));
			rc += validateNodeCounts(getChildIndex(index, 2));
			rc += validateNodeCounts(getChildIndex(index, 3));
			rc += validateNodeCounts(getChildIndex(index, 4));
		}
		
//		assert rc == this.getNodeLandmarkCount(index);
		return rc;
	}
	
	public boolean remove(GenericLandmark lm) {
		return findAndRemove(lm, 0);
	}
	
	public GenericLandmark[] getLandmarksInBounds(Rectangle2D bounds, GenericLandmark[] result) {
//		ArrayList rc = new ArrayList();
		getLandmarksInBounds(0, bounds, result, 0);
//		return (GenericLandmark[]) rc.toArray(new GenericLandmark[rc.size()]);
		return result;
	}
	
	public void getLandmarksInBounds(Rectangle2D bounds, List<GenericLandmark> list) {
		getLandmarksInBounds(0, bounds, list);
	}

	private void getLandmarksInBounds(long nodeIndex, Rectangle2D bounds, List<GenericLandmark> list) {
		final QuadMapNode node = getNode(nodeIndex);
		if( node != null ) {
			if( node.intersects(bounds) ) {
				if( node.isLandmarkInBounds(bounds) ) {
					list.add(node.getLandmark());
				}
				for( int i = 1; i <= 4; i++ ) {
					long childIndex = getChildIndex(nodeIndex, i);
					getLandmarksInBounds(childIndex, bounds, list);
				}
			}
		}
	}
	
	private int getLandmarksInBounds(long nodeIndex, Rectangle2D bounds, GenericLandmark[] result, int resultIndex ) {
		final QuadMapNode node = getNode(nodeIndex);
		if( node != null ) {
			if( node.intersects(bounds) ) {
				if( node.isLandmarkInBounds(bounds) ) {
					result[resultIndex++] = node.getLandmark();
//					rc.add(node.getLandmark());
				}
				for( int i = 1; i <= 4; i++ ) {
					long childIndex = getChildIndex(nodeIndex, i);
					resultIndex = getLandmarksInBounds(childIndex, bounds, result, resultIndex);
				}
			}
		}
		return resultIndex;
	}

	public int size() {
		return getNodeLandmarkCount(0);
	}

	public boolean contains(Object obj) {
		long nodeIndex = findLandmark((GenericLandmark)obj, 0);
		return getNode(nodeIndex)!=null;
	}

	public Iterator<GenericLandmark> iterator() {
		return new QuadMapIterator();
	}

//	public Object[] toArray() {
//		return toArray(new GenericLandmark[size()]);
//	}

//	public Object[] toArray(Object[] arg0) {
//		Iterator iter = iterator();
//		for (int i = 0; i < arg0.length; i++) {
//			arg0[i] = iter.next();
//		}
//		return arg0;
//	}

	public boolean add(GenericLandmark arg0) {
		add0(arg0);
		return true;
	}

	public boolean remove(Object arg0) {
		return remove((GenericLandmark)arg0);
	}

	public void clear() {
		quadtree.clear();
	}

	private QuadMapNode getNode(long index) {
		if( index >= 0 ) {
			return quadtree.get(index);
		} else {
			assert false; // I don't think we really want to do this
			return null;
		}
	}

	private QuadMapNode setNode(long index, QuadMapNode node) {
//		validateNodeCounts(0);
//		if( node == null ) {
//			System.out.println("Setting " + node + " at node index " + index);
//		} else {
//			System.out.println("Setting " + node.getLandmark().getX() + " " + node.getLandmark().getY() + " at node index " + index);
//		}
		assert (node!=null) || (!hasChildren(index));	// make sure a removed node has no children
		assert (index == 0) || (node==null) || (hasParent(index));		// make sure an added node has a parent
		QuadMapNode lastNode = quadtree.set(index, node);
		if( node == null && lastNode != null ) {
			decrementNodeLandmarkCount(index);
			decrementParentCounts(index);
		} else if( node != null && lastNode == null ) {
			incrementNodeLandmarkCount(index);
			incrementParentCounts(index);
		}
		modCount++;
//		validateNodeCounts(0);
		return lastNode;
	}
	
	private QuadMapNode clearNode(long index) {
		return setNode(index, null);
	}

	private QuadMapNode removeNode(long index) {
		final QuadMapNode node = getNode(index);
		if( node != null ) {
//			assert getNodeLandmarkCount(index) >= 0;	// this count should never be negative
			if( !hasChildren(index) ) {
				// there are no landmarks below here. Remove the node.
				clearNode(index);
			} else {
				// find the bottom of the heaviest child branch to move here
				long replacementIndex = getHeaviestLeafNode(index);
				assert replacementIndex != index;
				QuadMapNode replacement = clearNode(replacementIndex);
				assert replacement != null;
				replacement = new QuadMapNode(replacement.getLandmark(), node);
				setNode(index, replacement);
			}
		}
		return node;
	}
	
	private boolean hasParent(long nodeIndex) {
		return getNode(getParentIndex(nodeIndex))!=null;
	}
	
	private boolean hasChildren(long nodeIndex) {
		boolean rc = false;
		rc |= getNode(getChildIndex(nodeIndex, 1))!=null;
		rc |= getNode(getChildIndex(nodeIndex, 2))!=null;
		rc |= getNode(getChildIndex(nodeIndex, 3))!=null;
		rc |= getNode(getChildIndex(nodeIndex, 4))!=null;
		return rc;
	}
	
	private long findLandmark( GenericLandmark lm, long searchIndex ) {
		return findLandmark(lm.getX(), lm.getY(), lm, searchIndex);	
	}
	
	private long findLandmark( final double x, final double y, final GenericLandmark lm, long searchIndex ) {
//		long[] history = new long[50];
//		int historyIndex = 0;
		long findIndex = searchIndex;
		for( QuadMapNode node = getNode(findIndex); node != null;  node = getNode(findIndex)) {
			assert node.contains(x,y);
			if( node.matchesLandmark(lm) ) {
				break;
			}
			int quadrant = node.getQuadrantIndex(x, y);
//			history[historyIndex++] = findIndex;
			findIndex = getChildIndex(findIndex, quadrant);
		}
		assert findIndex == 0 || getNode(getParentIndex(findIndex))!=null;
		return findIndex;	
	}
	
	private void findAndInsert(GenericLandmark lm, long searchIndex) {
//		QuadMapNode root = getNode(0);
//		System.out.println(root.getMinX() + " " + root.getMinX());
		long insertAt = findLandmark(lm, searchIndex);
		Rectangle2D newBounds = calculateNodeBounds(insertAt);
		if( !newBounds.contains(lm.getX(), lm.getY()) ) {
			insertAt = findLandmark(lm, searchIndex);
		}
		setNode(insertAt,  new QuadMapNode(lm, newBounds));
	}

	private boolean findAndRemove(GenericLandmark lm, long searchIndex) {
		long removeFrom = findLandmark(lm, searchIndex);
		QuadMapNode removedNode = removeNode(removeFrom);
		return removedNode != null;
	}
	
	private void incrementParentCounts(long nodeIndex) {
		while( nodeIndex > 0 ) {
			nodeIndex = getParentIndex(nodeIndex);
			incrementNodeLandmarkCount(nodeIndex);
		};
	}
	
	private void decrementParentCounts(long nodeIndex) {
		while( nodeIndex > 0 ) {
			nodeIndex = getParentIndex(nodeIndex);
			decrementNodeLandmarkCount(nodeIndex);
		};
	}
	
	private long getParentIndex(long childIndex) {
		return quadtree.getParentIndex(childIndex);
	}
	
	private long getChildIndex(long parentIndex, int quadIndex) {
		return quadtree.getChildIndex(parentIndex, quadIndex-1);
	}

	private int getQuadrantInParent( long childIndex ) {
		return 1+quadtree.getChildIndexOfParent(childIndex);
	}

	private Rectangle2D calculateNodeBounds(long nodeIndex) {
		final long parentIndex = getParentIndex(nodeIndex);
		final int quadrant = getQuadrantInParent(nodeIndex);
		final QuadMapNode parentNode = getNode(parentIndex);
		if( parentNode == null ) {
			assert false;	// we never want to be here -- I think
			Rectangle2D parentBounds = calculateNodeBounds(parentIndex);
			return QuadMapNode.calculateChildBounds(quadrant, parentBounds.getWidth()/2, parentBounds.getHeight()/2, parentBounds.getCenterX(), parentBounds.getCenterY(), parentBounds.getMinX(), parentBounds.getMinY());
		} else {
			return parentNode.calculateChildBounds(quadrant);
		}
	}
	
	private void expandRootBounds(final double x, final double y) {
		QuadMapNode node = quadtree.get(0);
		if( node!=null && !node.contains(x, y) ) {
			Rectangle2D newBounds = expandRectangleBounds(x, y, node.getBoundsCopy());
			node = new QuadMapNode(newBounds, node);
			setNode(0, node);
			updateChildBounds(getChildIndex(0, 1));
			updateChildBounds(getChildIndex(0, 2));
			updateChildBounds(getChildIndex(0, 3));
			updateChildBounds(getChildIndex(0, 4));
		}
	}
	
	private void updateChildBounds(long nodeIndex) {
		QuadMapNode node = quadtree.get(nodeIndex);
		if( node != null ) {
			Rectangle2D newBounds = calculateNodeBounds(nodeIndex);
			if( !node.equalsBounds(newBounds) ) {
				setNode(nodeIndex, new QuadMapNode(newBounds, node));
				updateChildBounds(getChildIndex(nodeIndex, 1));
				updateChildBounds(getChildIndex(nodeIndex, 2));
				updateChildBounds(getChildIndex(nodeIndex, 3));
				updateChildBounds(getChildIndex(nodeIndex, 4));
			}
		}
	}

//	private void expandNodeBounds(long nodeIndex, final double x, final double y) {
//		QuadMapNode node = (QuadMapNode) quadtree.get(nodeIndex);
//		
//		
//		
//		if( node!=null && !node.contains(x, y) ) {
//			Rectangle2D newBounds = expandRectangleBounds(x, y, node.getBoundsCopy());
//			if( !newBounds.contains(x,y) ) {
//				newBounds = expandRectangleBounds(x, y, node.getBoundsCopy());
//				newBounds.contains(x,y);
//			}
//			node = new QuadMapNode(newBounds, node);
//			setNode(nodeIndex, node);
//			if( getNodeLandmarkCount(nodeIndex) > 0 ) {
//				final double dividerX = node.getDividerX();
//				final double dividerY = node.getDividerY();
//				if( x < dividerX ) {
//					expandNodeBounds(getChildIndex(nodeIndex, 2), x, dividerY);
//					expandNodeBounds(getChildIndex(nodeIndex, 3), x, dividerY);
//				} else {
//					expandNodeBounds(getChildIndex(nodeIndex, 1), x, dividerY);
//					expandNodeBounds(getChildIndex(nodeIndex, 4), x, dividerY);
//				}
//				if( y < dividerY ) {
//					expandNodeBounds(getChildIndex(nodeIndex, 3), dividerX, y);
//					expandNodeBounds(getChildIndex(nodeIndex, 4), dividerX, y);
//				} else {
//					expandNodeBounds(getChildIndex(nodeIndex, 1), dividerX, y);
//					expandNodeBounds(getChildIndex(nodeIndex, 2), dividerX, y);
//				}
//			}
//		}
//	}

	private static Rectangle2D expandRectangleBounds(final double x, final double y, Rectangle2D r) {
		if( !r.contains(x,y) ) {
			double x1 = Math.min(x, r.getMinX());
			double y1 = Math.min(y, r.getMinY());
			final double x2 = Math.max(x, r.getMaxX());
			final double y2 = Math.max(y, r.getMaxY());
			double width = x2-x1;
			double height = y2-y1;
			if( r.getMaxX() <= x ) {
				width *= 1.1;
			}
			if( r.getMaxY() <= y ) {
				height *= 1.1;
			}
			if( r.getMinX() > x ) {
				final double dw = 0.1*width;
				x1 -= dw;
				width += dw;
			}
			if( r.getMinY() > y ) {
				final double dh = 0.1*height;
				y1 -= dh;
				width += dh;
			}
			r = new Rectangle2D.Double(x1, y1, width, height);
		}
		return r;
	}
	
	private void handleLandmarkPositionChange(long nodeIndex, GenericLandmark lm, Point2D oldPosition) {
		long landmarkIndex = findLandmark(oldPosition.getX(), oldPosition.getY(), lm, nodeIndex);
		QuadMapNode landmarkNode = getNode(landmarkIndex);
		assert landmarkNode.matchesLandmark(lm);
		if( !landmarkNode.contains(lm.getX(), lm.getY()) ) {
			removeNode(landmarkIndex);
			add(landmarkNode.getLandmark());
		}
	}

	private long getHeaviestLeafNode( long parentIndex ) {
		while( hasChildren(parentIndex) ) {
			parentIndex = getHeaviestChildIndex(parentIndex);
//			assert getNode(childIndex) != null;
		}
		return parentIndex;
	}
	
	private long getHeaviestChildIndex(long nodeIndex) {
		long best1 = heaviestNode(getChildIndex(nodeIndex, 1), getChildIndex(nodeIndex, 2));
		long best2 = heaviestNode(getChildIndex(nodeIndex, 3), getChildIndex(nodeIndex, 4));
		return heaviestNode(best1, best2);
	}

	private long heaviestNode(long index1, long index2) {
		return (getNodeLandmarkCount(index1) > getNodeLandmarkCount(index2))?index1:index2;
	}

//	private void growCountArray(int size) {
//		if( nodeLandmarkCount.length < size ) {
//			int[] temp = new int[size];
//			System.arraycopy(nodeLandmarkCount, 0, temp, 0, nodeLandmarkCount.length);
//			nodeLandmarkCount = temp;
//		}
//	}
	
	private int getNodeLandmarkCount(long nodeIndex) {
		Integer count = this.nodeLandmarkCount.get(new Long(nodeIndex));
		return (count==null)?0:count.intValue();
//		if( nodeIndex < nodeLandmarkCount.length )
//			return nodeLandmarkCount[nodeIndex];
//		else
//			return 0;
	}
	
	private void setNodeLandmarkCount(long nodeIndex, int count) {
		this.nodeLandmarkCount.put(new Long(nodeIndex), new Integer(count));
	}
	
	private void incrementNodeLandmarkCount(long nodeIndex) {
//		growCountArray(nodeIndex+1);
//		nodeLandmarkCount[nodeIndex]++;
		setNodeLandmarkCount(nodeIndex, getNodeLandmarkCount(nodeIndex)+1);
	}
	
	private void decrementNodeLandmarkCount(long nodeIndex) {
//		nodeLandmarkCount[nodeIndex]--;
		setNodeLandmarkCount(nodeIndex, getNodeLandmarkCount(nodeIndex)-1);
	}
	
	private class QuadMapIterator implements Iterator<GenericLandmark> {
		private int modCount = QuadMap2.this.modCount;
		private long lastNodeIndex = -1;
		private LinkedList<Long> stack = new LinkedList<Long>();
		
		public QuadMapIterator() {
			pushNode(0);
		}

		public boolean hasNext() {
			if( modCount != QuadMap2.this.modCount ) {
				throw new ConcurrentModificationException();
			}
			
			boolean nonEmptyStack = stack.size() > 0;
			boolean lastHasChildren = (lastNodeIndex==-1)?false:hasChildren(lastNodeIndex);
			return nonEmptyStack || lastHasChildren;
		}

		public GenericLandmark next() {
			if( modCount != QuadMap2.this.modCount ) {
				throw new ConcurrentModificationException();
			}
			if( lastNodeIndex != -1 ) {
				pushNode(getChildIndex(lastNodeIndex, 4));
				pushNode(getChildIndex(lastNodeIndex, 3));
				pushNode(getChildIndex(lastNodeIndex, 2));
				pushNode(getChildIndex(lastNodeIndex, 1));
			}
			if( stack.size() == 0 ) {
				throw new NoSuchElementException();
			}
//			QuadMapNode node = (QuadMapNode) stack.removeFirst();
			Long nodeIndex = stack.removeFirst();
			lastNodeIndex = nodeIndex.intValue();
			return getNode(lastNodeIndex).getLandmark();
		}

		public void remove() {
			if( modCount != QuadMap2.this.modCount ) {
				throw new ConcurrentModificationException();
			}
			if( lastNodeIndex == -1 ) {
				throw new IllegalStateException();
			}
			modCount++;
//			QuadMap2.this.modCount++;
			QuadMapNode lastNode = getNode(lastNodeIndex);
			QuadMap2.this.findAndRemove(lastNode.getLandmark(), lastNodeIndex);
			// remove reuses the node so we push it back on the stack
			pushNode(lastNodeIndex);
			lastNodeIndex = -1;
		}
		
		private boolean pushNode(long nodeIndex) {
			if( getNode(nodeIndex) != null ) {
				stack.addFirst(new Long(nodeIndex));
				return true;
			} else {
				return false;
			}
		}
	}

	public int getLandmarkCount() {
		return size();
	}
}

class QuadMapNode implements Cloneable {
	private final Rectangle2D bounds;
	private final Point2D divider;
	private final GenericLandmark lm;
//	private long lmCount = 1;

	public QuadMapNode(GenericLandmark lm, Rectangle2D r) {
		this.bounds = r;
		this.divider = new Point2D.Double(r.getCenterX(), r.getCenterY());
		this.lm = lm;
		assert this.bounds.contains(this.lm.getX(), this.lm.getY());
	}

	public boolean equalsBounds(Rectangle2D newBounds) {
		return bounds.equals(newBounds);
	}

	public QuadMapNode(Rectangle2D newBounds, QuadMapNode node) {
		this.bounds = newBounds;
		this.divider = node.divider;
		this.lm = node.lm;
//		this.lmCount = node.getLmCount();
	}

	public QuadMapNode(GenericLandmark lm, QuadMapNode node) {
		this.bounds = node.bounds;
		this.divider = node.divider;
		this.lm = lm;
//		this.lmCount = node.getLmCount();
	}
	
//	public void removeUpdateListener(QuadMap2 map2) {
//		lm.removeUpdateListener(map2);
//	}
//
//	public void addUpdateListener(QuadMap2 map2) {
//		lm.addUpdateListener(map2);
//	}

	public Rectangle2D getBoundsCopy() {
		return new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

	public boolean isLandmarkInBounds(Rectangle2D bounds) {
		return bounds.contains(lm.getX(), lm.getY());
	}

	public boolean matchesLandmark(GenericLandmark lm) {
		return this.lm == lm;
	}

	public GenericLandmark getLandmark() {
		return lm;
	}

	public boolean contains(Point2D p) {
		return this.bounds.contains(p);
	}
	
	public boolean contains(double x, double y) {
		return this.bounds.contains(x,y);
	}
	
	public boolean intersects(Rectangle2D r) {
		return this.bounds.intersects(r);
	}
	
	public double getDividerX() {
		return divider.getX();
	}

	public double getDividerY() {
		return divider.getY();
	}

	public double getMinX() {
		return bounds.getMinX();
	}

	public double getMinY() {
		return bounds.getMinY();
	}

	public double getMaxX() {
		return bounds.getMaxX();
	}

	public double getMaxY() {
		return bounds.getMaxY();
	}
	
	public double getWidth() {
		return bounds.getWidth();
	}
	
	public double getHeight() {
		return bounds.getHeight();
	}

	public int getQuadrantIndex(final double newX, final double newY) {
		final double dividerX = this.divider.getX();
		final double dividerY = this.divider.getY();
		int quadrant;
		boolean isLeft = newX < dividerX;
		boolean isLower = newY < dividerY;
		if (isLeft) {
			if (isLower) {
				quadrant = 3;
			} else {
				quadrant = 2;
			}
		} else {
			if (isLower) {
				quadrant = 4;
			} else {
				quadrant = 1;
			}
		}
		return quadrant;
	}

	public Rectangle2D calculateChildBounds(int childQuadrant) {
		return calculateChildBounds(childQuadrant, this.divider.getX(), this.divider.getY(), this.bounds.getMinX(), this.bounds.getMinY(), this.bounds.getMaxX(), this.bounds.getMaxY());
	}

	public static Rectangle2D calculateChildBounds(
		int childQuadrant,
		final double dividerX,
		final double dividerY,
		final double minX,
		final double minY,
		final double maxX,
		final double maxY) {
		switch(childQuadrant) {
			case 1: return new Rectangle2D.Double(dividerX, dividerY, maxX-dividerX, maxY-dividerY);
			case 2: return new Rectangle2D.Double(    minX, dividerY, dividerX-minX, maxY-dividerY);
			case 3: return new Rectangle2D.Double(    minX,     minY, dividerX-minX, dividerY-minY);
			case 4: return new Rectangle2D.Double(dividerX,     minY, maxX-dividerX, dividerY-minY);
		}
		throw new IllegalArgumentException("Quadrant must be in the range [1..4]");
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("QuadMapNode.clone should be supported.");
		}
	}

//	public void incrementLmCount() {
//		this.lmCount++;
//	}
//	
//	public void decrementLmCount() {
//		this.lmCount--;
//	}
//
//	public int getLmCount() {
//		return lmCount;
//	}
}
	
