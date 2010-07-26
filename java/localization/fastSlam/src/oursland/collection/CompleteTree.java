package oursland.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * A tree represented as an array.
 * @author oursland
 */
public class CompleteTree<E> implements Cloneable {
	public static final long	ROOT			= 0;

	// the number of children that each and every child has
	final private int			branchingFactor;

	//	private ArrayList treeArray = new ArrayList();
	private Object[]			treeArrayFront	= new Object[341];

	// the first four levels of the tree (for b=4) will have fast access
	private Hashtable<Long, E>			treeArrayBack	= new Hashtable<Long, E>(347);

	// the rest will be hashed. 347 is prime
	public CompleteTree(CompleteTree<E> copy) {
		this.branchingFactor = copy.branchingFactor;
		//		this.treeArray = new ArrayList(copy.treeArray);
		this.treeArrayFront = copy.treeArrayFront.clone();
		this.treeArrayBack = new Hashtable<Long, E>(copy.treeArrayBack);
	}

	public CompleteTree(int branchingFactor) {
		this.branchingFactor = branchingFactor;
	}

	@SuppressWarnings("unchecked")
	public CompleteTree<E> clone() {
		try {
			CompleteTree rc = (CompleteTree) super.clone();
			rc.treeArrayFront = rc.treeArrayFront.clone();
			rc.treeArrayBack = (Hashtable<Long, Object>)rc.treeArrayBack.clone();
			//			rc.treeArray = (ArrayList) rc.treeArray.clone();
			return rc;
		} catch(CloneNotSupportedException e) {
			throw new Error("CompleteTree.clone should have worked...");
		}
	}

	public E setRoot(E o) {
		return set(ROOT, o);
	}

	public E getRoot() {
		return get(0);
	}

	@SuppressWarnings("unchecked")
	public E set(long index, E o) {
		long parentIndex = getParentIndex(index);
		if(index == 0 || hasContent(parentIndex)) {
			if(index < treeArrayFront.length) {
				assert (index <= Integer.MAX_VALUE);
				int intIndex = (int) index;
				E rc = (E)treeArrayFront[intIndex];
				treeArrayFront[intIndex] = o;
				return rc;
			} else {
				if(o == null) {
					return treeArrayBack.remove(new Long(index));
				} else {
					return treeArrayBack.put(new Long(index), o);
				}
			}
			//			treeArray.ensureCapacity(index+1);
			//			while(index >= treeArray.size()) {
			//				treeArray.add(null);
			//			}
			//			return treeArray.set(index, o);
			//			if( o == null ) {
			//				return treeArray.remove(new Long(index));
			//			} else {
			//				return treeArray.put(new Long(index), o);
			//			}
		}
		// no parent for index
		throw new ArrayIndexOutOfBoundsException();
	}

	public boolean hasContent(long index) {
		return get(index) != null;
	}

	@SuppressWarnings("unchecked")
	public E get(long index) {
		// when this was backed by an ArrayList we wanted to validate the index
		//		return treeArray.get(new Long(index));
		if(index < treeArrayFront.length) {
			assert (index <= Integer.MAX_VALUE);
			int intIndex = (int) index;
			return (E)treeArrayFront[intIndex];
		} else {
			return treeArrayBack.get(new Long(index));
		}
		//		if( index < treeArray.size() ) {
		//			return treeArray.get(index);
		//		}
		//		return null;
	}

	public int getBranchingFactor() {
		return branchingFactor;
	}

	public long getParentIndex(long index) {
		return (index - 1) / branchingFactor;
	}

	public long getChildIndex(long parentIndex, int childIndex) {
		assert (childIndex >= 0);
		assert (childIndex < branchingFactor);
		return parentIndex * 4 + childIndex + 1;
	}

	public int getChildIndexOfParent(long childIndex) {
		return (int) ((childIndex - 1) % branchingFactor);
	}

	public void clear() {
		treeArrayFront = new Object[treeArrayFront.length];
		treeArrayBack.clear();
		//		treeArray = new ArrayList(0);
		treeArrayBack = new Hashtable<Long, E>();
	}

	public long getRootIndex() {
		return 0;
	}
}