package vp.dataassoc;

import java.util.ArrayList;
import java.util.Arrays;

public class AssocTreeNode {
	private static final AssocTreeNode[] emptyChildren = {};
	private static final boolean includeNewLandmarks = true;

	final AssocPair pair;
	final AssocTreeNode parent;
	double wrtParent;
	double wrtGrandparent;
	double wrtAngle;
	double prob;

	public AssocTreeNode(AssocPair pair, AssocTreeNode parent) {
		this.pair = pair;
		this.parent = parent;
	}

	private double calculateOdds(AssocGenerator set) {
		wrtParent = 0.7;
		wrtGrandparent = 0.7;
		wrtAngle = 0.7;
		prob = 0.7;
		if( pair.previous != -1 ) {
			AssocTreeNode p = getAssocParent();
			if( p != null ) {
				wrtParent = set.getDistanceMetric(pair, p.pair);
				AssocTreeNode gp = p.getAssocParent();
				if( gp != null ) {
					wrtGrandparent = set.getDistanceMetric(pair, gp.pair);
					wrtAngle = set.getAngleMetric(pair, p.pair, gp.pair); 
				}
				prob = wrtParent * wrtGrandparent * wrtAngle;
			}
		} else {
			//TODO: Find a model to determine the chance of a new node 
			// For now just make each new landmark occur with decreasing probability.
//			prob = Math.pow(0.5, getPreviousNewLandmarkCount());

			// get the maximum distance metric
			AssocTreeNode assocParent = getAssocParent();
			if( assocParent != null ) {
				double maxDistanceMetric = getMaxDistanceMetric(pair.current, assocParent.pair, set);
				double newLandmark = (1.0 - maxDistanceMetric);
//				int newLandmarkMin = set.currSet.size() - set.prevSet.size();
//				double newLandmark = Math.pow(0.3, Math.max(1, 1+getPreviousNewLandmarkCount()-newLandmarkMin));
				prob = wrtParent * wrtGrandparent * wrtAngle * newLandmark;
			} else {
				prob = wrtParent * wrtGrandparent * wrtAngle * Math.pow(0.5, getPreviousNewLandmarkCount());
			}
		}
		if( parent != null ) {
			prob *= parent.prob;
		}
		return prob;
	}

	private double getMaxDistanceMetric(int curr1, AssocPair parent, AssocGenerator set) {
		double maxValue = 0.0;
		for( int prev1 = 0; prev1 < set.prevSet.size(); prev1++ ) {
			if( !isPreviousInUse(prev1) ) {
				double value = set.getDistanceMetric(curr1, prev1, parent);
				if( value > maxValue ) {
					maxValue = value;
				}
			}
		}
		return maxValue;
	}

	private int getPreviousNewLandmarkCount() {
		int rc = 0;
		AssocTreeNode node = parent;
		while( node != null ) {
			if( node.pair.previous == -1 ) {
				rc++;
			}
			node = node.parent;
		}
		return 0;
	}

	/**
	 * Returns the first parent node with an actual data association (i.e. Not a new landmark).
	 */
	private AssocTreeNode getAssocParent() {
		AssocTreeNode node = parent;
		while( node != null ) {
			if( node.pair.previous != -1 ) {
				return node;
			}
			node = node.parent;
		}
		return null;
	}

	private void normalizeOdds(double sum) {
		prob /= sum;
//		if( parent != null ) {
//			prob *= parent.prob;
//		}
	}

	public double generateChildren(AssocGenerator set, ArrayList<AssocTreeNode> result) {
		final int currSize = set.currSet.size();
		final int prevSize = set.prevSet.size();
		final int currIndex = pair.current + 1;
		double sum = 0.0;
		if (currIndex < currSize) {
			for (int prevIndex = 0; prevIndex < prevSize; prevIndex++) {
				if (!isPreviousInUse(prevIndex)) {
					sum += addChild(result, currIndex, prevIndex, this, set);
				}
			}
			if( includeNewLandmarks ) {
				sum +=  addChild(result, currIndex, -1, this, set);
			}
		}		
		return sum;
	}

	public static void generateRoot(AssocGenerator set, ArrayList<AssocTreeNode> result) {
		final int currSize = set.currSet.size();
		final int prevSize = set.prevSet.size();
		double sum = 0.0;
		final int currIndex = 0;
		for (int prevIndex = 0; prevIndex < prevSize; prevIndex++) {
			sum += addChild(result, currIndex, prevIndex, null, set);
		}
		if( includeNewLandmarks ) {
			addChild(result, currIndex, -1, null, set);
		}
		normalizeResults(result, sum);
	}

	private static double addChild(ArrayList<AssocTreeNode> result, int currIndex, int prevIndex, AssocTreeNode parent, AssocGenerator set) {
		AssocTreeNode node = new AssocTreeNode(new AssocPair(currIndex, prevIndex), parent);
		double sum = node.calculateOdds(set);
		result.add(node);
		return sum;
	}

	public static void normalizeResults(final ArrayList rc, final double sum) {
		for (int i = 0; i < rc.size(); i++) {
			AssocTreeNode node = (AssocTreeNode) rc.get(i);
			node.normalizeOdds(sum);
		}
	}

	private boolean isPreviousInUse(int prevIndex) {
		boolean rc = false;
		AssocTreeNode p = this;
		while (p != null) {
			if (p.pair.previous == prevIndex) {
				rc = true;
				break;
			}
			p = p.parent;
		}
		return rc;
	}

	public double getOdds() {
		return prob;
	}

	public void printAssoc() {
		boolean rc = false;
		AssocTreeNode p = this;
		while (p != null) {
			System.out.print(p.pair);
			System.out.print(" ");
			p = p.parent;
		}
	}

	public int[] getPreviousMap() {
		int maxPrev = getMaxPrevious();
		int[] prevAssoc = createAssocArray(maxPrev);
		AssocTreeNode node = this;
		while( node != null ) {
			fillAssocArray(prevAssoc, node.pair.previous, node.pair.current);
			node = node.parent;
		}
		return prevAssoc;
	}
	
	public int[] getCurrentMap() {
		int maxCurr = getMaxCurrent();
		int[] currAssoc = createAssocArray(maxCurr);
		AssocTreeNode node = this;
		while( node != null ) {
			fillAssocArray(currAssoc, node.pair.current, node.pair.previous);
			node = node.parent;
		}
		return currAssoc;
	}

	private static void fillAssocArray(int[] assoc, int index1, int index2) {
		if(index1 != -1 ) {
			assoc[index1] = index2;
		}
	}

	private int[] createAssocArray(int maxIndex) {
		int[] assoc = new int[maxIndex+1];
		Arrays.fill(assoc, -1);
		return assoc;
	}
	
	private int getMaxCurrent() {
		int maxCurr = 0;
		AssocTreeNode node = this;
		while( node != null ) {
			maxCurr = Math.max(maxCurr, node.pair.current);
			node = node.parent;
		}
		return maxCurr;
	}
	
	private int getMaxPrevious() {
		int maxPrev = 0;
		AssocTreeNode node = this;
		while( node != null ) {
			maxPrev = Math.max(maxPrev, node.pair.previous);
			node = node.parent;
		}
		return maxPrev;
	}
	
	private int getSize() {
		int rc = 0;
		AssocTreeNode node = this;
		while( node != null ) {
			rc++;
			node = node.parent;
		}
		return rc;
	}
}
