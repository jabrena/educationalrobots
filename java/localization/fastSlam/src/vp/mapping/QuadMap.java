package vp.mapping;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class QuadMap<E extends GenericLandmark> extends AbstractCollection<E> implements LandmarkListener, Cloneable {
	private QuadMapNode root;
	private int modCount;

	public QuadMap(Rectangle2D bounds) {
		setRoot(new QuadMapNode(bounds));
	}

	public boolean remove(GenericLandmark lm) {
		boolean rc = false;
		if( getRoot().remove(lm) ) {
//			lm.removeUpdateListener(this);
			rc = true;
		}
		modCount++;
		return rc;
	}
	
	public GenericLandmark[] getLandmarksInBounds(Rectangle2D bounds) {
		ArrayList<E> rc = new ArrayList<E>();
		getRoot().getLandmarksInBounds(bounds, rc);
		return rc.toArray(new GenericLandmark[rc.size()]);
	}
	
	public boolean replace( E toReplace, E replaceWith ) {
		return getRoot().replace(toReplace, replaceWith);
	}

	public void notifyLandmarkMoved(E lm, Point2D oldPosition) {
		getRoot().validateLandmark(lm, oldPosition);
	}
	
	@SuppressWarnings("unchecked")
	public QuadMap<E> clone() {
		try {
			QuadMap<E> rc = (QuadMap<E>)super.clone();
			rc.setRoot(rc.getRoot().clone());
			// we need to add rc as an updateListener to all of the landmarks
//			Iterator iter = iterator();
//			while(iter.hasNext()) {
//				GenericLandmark lm = (GenericLandmark) iter.next();
//				lm.addUpdateListener(this);
//			}
			return rc;
		} catch (CloneNotSupportedException e) {
			throw new Error("QuadMap.clone should have worked.");
		}
	}

	public int size() {
		return getRoot().getLandmarkCount();
	}

	public boolean isEmpty() {
		return getRoot().getLandmarkCount()==0;
	}

	public boolean contains(Object arg0) {
		boolean rc = false;
		if( arg0 instanceof GenericLandmark ) {
			rc = getRoot().containsLandmark((GenericLandmark)arg0);
		}
		return rc;
	}

	public Iterator<E> iterator() {
		return new QuadMapIterator();
	}

//	public E[] toArray() {
//		return toArray(new GenericLandmark[size()]);
//	}

//	public <T extends E> T[] toArray(T[] arg0) {
//		Iterator<E> iter = iterator();
//		for (int i = 0; i < arg0.length; i++) {
//			arg0[i] = (T)iter.next();
//		}
//		return arg0;
//	}

	public boolean add(E arg0) {
		if(!getRoot().getBounds().contains(arg0.getX(), arg0.getY())) {
			getRoot().expandBounds(arg0.getX(), arg0.getY());
		}
		getRoot().insert(arg0);
		// lm.addUpdateListener(this);
		modCount++;
		return true;
	}

	public boolean remove(Object arg0) {
		return remove((GenericLandmark)arg0);
	}

	public boolean containsAll(Collection<?> arg0) {
		boolean rc = true;
		Iterator<?> iter = arg0.iterator();
		while(rc && iter.hasNext()) {
			rc &= contains(iter.next());
		}
		return rc;
	}

	public boolean addAll(Collection<? extends E> arg0) {
		Iterator<? extends E> iter = arg0.iterator();
		while(iter.hasNext()) {
			add(iter.next());
		}
		return true;
	}

	public boolean removeAll(Collection arg0) {
		Iterator iter = arg0.iterator();
		while(iter.hasNext()) {
			remove(iter.next());
		}
		return true;
	}

	public boolean retainAll(Collection arg0) {
		Iterator iter = iterator();
		while(iter.hasNext()) {
			Object o = iter.next();
			if( !arg0.contains(o) ) {
				iter.remove();
			}
		}
		return true;
	}

	public void clear() {
		Iterator iter = iterator();
		while(iter.hasNext()) {
			iter.next();
			iter.remove();
		}
	}

	private class QuadMapIterator implements Iterator<E> {
		private int modCount = QuadMap.this.modCount;
		private QuadMapNode lastNode = null;
		private LinkedList<QuadMapNode> stack = new LinkedList<QuadMapNode>();
		
		public QuadMapIterator() {
			pushNode(QuadMap.this.getRoot());
		}

		public boolean hasNext() {
			if( modCount != QuadMap.this.modCount ) {
				throw new ConcurrentModificationException();
			}
			
			boolean nonEmptyStack = stack.size() > 0;
			boolean childrenOfLast = (lastNode != null) && 
				(	lastNode.quad4.lm != null	||
					lastNode.quad3.lm != null	||
					lastNode.quad2.lm != null	||
					lastNode.quad1.lm != null	);
			return nonEmptyStack || childrenOfLast;
		}

		public E next() {
			if( modCount != QuadMap.this.modCount ) {
				throw new ConcurrentModificationException();
			}
			if( lastNode != null ) {
				pushNode(lastNode.quad4);
				pushNode(lastNode.quad3);
				pushNode(lastNode.quad2);
				pushNode(lastNode.quad1);
			}
			if( stack.size() == 0 ) {
				throw new NoSuchElementException();
			}
			QuadMapNode node = stack.removeFirst();
			lastNode = node;
			return lastNode.lm;
		}

		public void remove() {
			if( modCount != QuadMap.this.modCount ) {
				throw new ConcurrentModificationException();
			}
			if( lastNode == null ) {
				throw new IllegalStateException();
			}
			modCount++;
			QuadMap.this.modCount++;
			lastNode.remove(lastNode.lm);
			// remove reuses the node so we push it back on the stack
			pushNode(lastNode);
			lastNode = null;
		}
		
		private boolean pushNode(QuadMapNode node) {
			if( node.lm != null ) {
				stack.addFirst(node);
				return true;
			} else {
				return false;
			}
		}
	}

	private class QuadMapNode implements Cloneable {
		private Rectangle2D bounds;
		private final Point2D divider;
		private E lm = null;
		private int lmCount = 0;
		private QuadMapNode quad1 = null; // upper right
		private QuadMapNode quad2 = null; // upper left
		private QuadMapNode quad3 = null; // lower left
		private QuadMapNode quad4 = null; // lower right

		@SuppressWarnings("unchecked")
		public QuadMapNode clone() {
			try {
				QuadMapNode rc = (QuadMapNode) super.clone();
				rc.bounds = (Rectangle2D) rc.bounds.clone();
				if( rc.lm != null ) {
					rc.lm = (E) rc.lm.clone();
					rc.quad1 = rc.quad1.clone();
					rc.quad2 = rc.quad2.clone();
					rc.quad3 = rc.quad3.clone();
					rc.quad4 = rc.quad4.clone();
				}
				return rc;
			} catch (CloneNotSupportedException e) {
				throw new Error("QuadMapNode.clone should be supported.");
			}
		}

		public void getLandmarksInBounds(Rectangle2D bounds, ArrayList<E> rc) {
			if( lm != null ) {
				if( this.bounds.intersects(bounds) ) {
					if( bounds.contains(lm.getX(), lm.getY()) ) {
						rc.add(lm);
					}
					quad1.getLandmarksInBounds(bounds, rc);
					quad2.getLandmarksInBounds(bounds, rc);
					quad3.getLandmarksInBounds(bounds, rc);
					quad4.getLandmarksInBounds(bounds, rc);
				}
			}
		}

		public boolean replace( E toReplace, E replaceWith ) {
			if( this.lm == null ) {
				return false;
			} else if( this.lm == toReplace ) {
				this.lm = replaceWith;
				QuadMap.this.getRoot().validateLandmark(this.lm, new Point2D.Double(toReplace.getX(), toReplace.getY()));
				return true;
			} else {
				int quadrant = getQuadrantIndex(toReplace.getX(), toReplace.getY());
				QuadMapNode target = getChildQuadrant(quadrant);
				return target.replace(toReplace, replaceWith);
			}
		}

		public QuadMapNode(Rectangle2D r) {
			this.bounds = r;
			this.divider = new Point2D.Double(r.getCenterX(), r.getCenterY());
		}

		public void expandBounds(final double x, final double y) {
			if( !getBounds().contains(x, y) ) {
				final double x1 = Math.min(x, bounds.getMinX());
				final double y1 = Math.min(y, bounds.getMinY());
				final double x2 = Math.max(x, bounds.getMaxX());
				final double y2 = Math.max(y, bounds.getMaxY());
				this.bounds = new Rectangle2D.Double(x1, y1, x2-x1, y2-y1);
				if( getLandmarkCount() > 0 ) {
					if( x < divider.getX() ) {
						quad2.expandBounds(x, quad2.divider.getY());
						quad3.expandBounds(x, quad3.divider.getY());
					} else {
						quad1.expandBounds(x, quad1.divider.getY());
						quad4.expandBounds(x, quad4.divider.getY());
					}
					if( y < divider.getY() ) {
						quad3.expandBounds(divider.getX(), y);
						quad4.expandBounds(divider.getX(), y);
					} else {
						quad1.expandBounds(divider.getX(), y);
						quad2.expandBounds(divider.getX(), y);
					}
				}
			}
		}

		public boolean validateLandmark(E lm, Point2D oldPosition) {
			if( bounds.contains(oldPosition) ) {
				// This node, or a child of this node, may hold the landmark.
				if(this.lm == lm) {
					// This node contain the landmark -- see if it still matches
					if( bounds.contains(lm.getX(), lm.getY()) ) {
						// the landmark is still in the bounds
						return true;	// no further investigation needed
					} else {
						// the landmark is no longer in this bound
						return false;	// the parent will needs to handle this case 
					}
				} else if( this.lmCount > 1 ){
					// one of the child nodes contains the landmark
					if (!validateLandmarkChild(lm, oldPosition, quad1)) return false;
					if (!validateLandmarkChild(lm, oldPosition, quad2)) return false;
					if (!validateLandmarkChild(lm, oldPosition, quad3)) return false;
					if (!validateLandmarkChild(lm, oldPosition, quad4)) return false;
					// there is no problem with any of the child nodes so don't worry about it
					return true;
				}
			}
			// We don't contain the old landmark and don't worry about it. 
			return true;	// no further investigation needed
		}

		private boolean validateLandmarkChild(E lm, Point2D oldPosition, QuadMap<E>.QuadMapNode tempQuadrantNode) {
			boolean rc = true;
			if( !tempQuadrantNode.validateLandmark(lm, oldPosition ) ) {
				// remove the landmark from the child
				tempQuadrantNode.remove(lm);
				// set the landmark of this node to the moved landmark
				E temp = this.lm;
				this.lm = lm;
				// reinsert the old landmark at this node into the children
				insert(temp);
				rc = false;	// let the parent handle this landmark change
			}
			return rc;
		}

		public void insert(E lm) {
			if( this.lm == null ) {
				this.lm = lm;
				this.lmCount = 1;
				this.quad1 = new QuadMapNode(getChildBounds(1));
				this.quad2 = new QuadMapNode(getChildBounds(2));
				this.quad3 = new QuadMapNode(getChildBounds(3));
				this.quad4 = new QuadMapNode(getChildBounds(4));
			} else {
				int quadrant = getQuadrantIndex(lm.getX(), lm.getY());
				QuadMapNode target = getChildQuadrant(quadrant);
				target.insert(lm);
				this.lmCount++;
			}
		}

		public boolean remove(GenericLandmark lm) {
			boolean rc = false;
			if( this.lm == null ) {
				// fall through and return false
			} else if( this.lm == lm ) {
				rc = true;
				this.lm = null;
				this.lmCount--;
				assert this.lmCount >= 0;	// this count should never be negative
				if( this.lmCount == 0 ) {
					// there are no more landmarks. Clean up the children.
					this.quad1 = null;
					this.quad2 = null;
					this.quad3 = null;
					this.quad4 = null;
				} else {
					// move a landmark from the heaviest child to this one.
					QuadMapNode heaviest = getHeaviestChild();
					this.lm = heaviest.lm;
					heaviest.remove(this.lm);
				}
			} else if( getBounds().contains(lm.getX(),lm.getY()) ) {
				// otherwise, try to remove it from a child
				rc |= quad1.remove(lm);
				rc |= quad2.remove(lm);
				rc |= quad3.remove(lm);
				rc |= quad4.remove(lm);
			}
			return rc;
		}
	
		private QuadMap<E>.QuadMapNode getHeaviestChild() {
			QuadMap<E>.QuadMapNode best1 = heaviestNode(quad1, quad2);
			QuadMap<E>.QuadMapNode best2 = heaviestNode(quad3, quad4);
			return heaviestNode(best1, best2);
		}

		public int getLandmarkCount() {
			return this.lmCount;
		}

		private QuadMapNode getChildQuadrant(int quadrant) {
			QuadMapNode target = null;
			switch (quadrant) {
				case 1 : {
					target = quad1;
				} break;
				case 2 : {
					target = quad2;
				} break;
				case 3 : {
					target = quad3;
				} break;
				case 4 : {
					target = quad4;
				} break;
			}
			return target;
		}

		public Rectangle2D getBounds() {
			return bounds;
		}

		private int getQuadrantIndex(final double newX, final double newY) {
			final double dividerX = divider.getX();
			final double dividerY = divider.getY();
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
	
		private Rectangle2D getChildBounds(int quadrant) {
			final double halfWidth = bounds.getWidth()/2;
			final double halfHeight = bounds.getHeight()/2;
			switch(quadrant) {
				case 1: return new Rectangle2D.Double(divider.getX(), divider.getY(), halfWidth, halfHeight);
				case 2: return new Rectangle2D.Double( bounds.getX(), divider.getY(), halfWidth, halfHeight);
				case 3: return new Rectangle2D.Double( bounds.getX(),  bounds.getY(), halfWidth, halfHeight);
				case 4: return new Rectangle2D.Double(divider.getX(),  bounds.getY(), halfWidth, halfHeight);
			}
			throw new IllegalArgumentException("Quadrant must be in the range [1..4]");
		}
	
		public boolean containsLandmark(GenericLandmark lm) {
			final double x = lm.getX();
			final double y = lm.getY();
			boolean rc = false;
			if( getLandmarkCount() > 0 && getBounds().contains(x, y) ) {
				if( this.lm == lm ) {
					return true;
				} else {
					rc |= quad1.containsLandmark(lm);
					rc |= quad2.containsLandmark(lm);
					rc |= quad3.containsLandmark(lm);
					rc |= quad4.containsLandmark(lm);
				}
			}
			return rc;
		}
	}
	
	private QuadMap<E>.QuadMapNode heaviestNode(QuadMap<E>.QuadMapNode node1, QuadMap<E>.QuadMapNode node2) {
		return (node1.getLandmarkCount() > node2.getLandmarkCount())?node1:node2;
	}

	private void setRoot(QuadMapNode root) {
		this.root = root;
	}

	private QuadMapNode getRoot() {
		return root;
	}

}

