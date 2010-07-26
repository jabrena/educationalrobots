package vp.dataassoc;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import oursland.math.Geometries;
import vp.mapping.LandmarkObservationSet;


public class AssocGenerator {
	LandmarkObservationSet currSet;
	LandmarkObservationSet prevSet;
	ArrayList<AssocTreeNode> list = new ArrayList<AssocTreeNode>();
	
	public AssocGenerator(LandmarkObservationSet currSet, LandmarkObservationSet prevSet) {
		this.currSet = currSet;
		this.prevSet = prevSet;
		AssocTreeNode.generateRoot(this, list);
	}
	
	public void nextLevel() {
		AssocTreeNode[] level = list.toArray(new AssocTreeNode[list.size()]);
		list.clear();
		double sum = 0.0;
		for (int i = 0; i < level.length; i++) {
			sum += level[i].generateChildren(this, list);
		}
//		System.out.println(sum);
		AssocTreeNode.normalizeResults(list, sum);
	}
	
	public void sortAndTrim() {
		Collections.sort(list, compare);
		int size = list.size();
		if( size > 0 ) {
			AssocTreeNode element = list.get(0);
			double firstOdds = element.getOdds();
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				element = (AssocTreeNode) iter.next();
				if( element.getOdds() < firstOdds/10 ) {
					iter.remove();
				}
			}
		}
//		while(list.size() > 10) {
//			list.remove(list.size()-1);
//		}
	}

	public List getList() {
		return list;
	}
	
	private final double landmarkError = 5;
	public double getDistanceMetric(AssocPair pair, AssocPair given) {
		// the probability of a single association without any extra information
		return getDistanceMetric(pair.current, pair.previous, given);
	}
	
	public double getDistanceMetric(int curr1, int prev1, AssocPair given) {
		int curr2 = given.current;
		int prev2 = given.previous;
		double currDistance = Math.sqrt(currSet.getDistanceSq(curr1, curr2));
		double prevDistance = Math.sqrt(prevSet.getDistanceSq(prev1, prev2));
		double signalToNoise = currDistance/landmarkError;
		double error = currDistance-prevDistance;
		return 1.0/(1.0+Math.pow(error/(4*landmarkError),2));
	}

	public double getAngleMetric(AssocPair p1, AssocPair p2, AssocPair p3) {
		double currDistance1 = Math.sqrt(currSet.getDistanceSq(p1.current, p2.current));
		double prevDistance1 = Math.sqrt(prevSet.getDistanceSq(p1.previous, p2.previous));
		double currDistance2 = Math.sqrt(currSet.getDistanceSq(p2.current, p3.current));
		double prevDistance2 = Math.sqrt(prevSet.getDistanceSq(p2.previous, p3.previous));
//		double minDistance = Math.min(Math.min(currDistance1, currDistance2), Math.min(prevDistance1, prevDistance2));
		double minDistance = Math.min(currDistance1, currDistance2);
		double signalToNoise = Math.min(currDistance1, currDistance2)/landmarkError;
		double angle1 = Geometries.getAngleCosine(currSet.get(p1.current), currSet.get(p2.current), currSet.get(p3.current))/(currDistance1*currDistance2);
		double angle2 = Geometries.getAngleCosine(prevSet.get(p1.previous), prevSet.get(p2.previous), prevSet.get(p3.previous))/(prevDistance1*prevDistance2);
		double estimateError = angle1-angle2;
		// the difference between angles should never be more than 180 degrees
		if( estimateError > 1 ) estimateError--;
		if( estimateError < -1 ) estimateError++;
		double metric = 1.0/(1.0+signalToNoise*Math.pow(estimateError, 2));
		double minMetric = 1.0/(1+signalToNoise/4);
		return Math.max(minMetric, metric);
	}

	public void printLevelOdds(PrintStream out) {
		int count = Math.min(list.size(), 10);
		for (int i = 0; i < count; i++) {
			AssocTreeNode element = list.get(i);
			out.print(element.getOdds() + "\t");
		}
		if( count < list.size() ) {
			out.print("... [" + list.size() + "]");
		}
		System.out.println();
	}

	private static Comparator<AssocTreeNode> compare = new Comparator<AssocTreeNode>() {
		public int compare(AssocTreeNode o1, AssocTreeNode o2) {
			double odds1 = o1.getOdds();
			double odds2 = o2.getOdds();
			if( odds1 == odds2 ) return 0;
			return odds1 > odds2 ? -1 : 1;
		}
	};

	public void printOddsComponents(PrintStream out) {
		int count = Math.min(list.size(), 10);
		for (int i = 0; i < count; i++) {
			AssocTreeNode element = list.get(i);
			element.printAssoc();
			out.println(" -- "+element.wrtParent+" "+element.wrtGrandparent+" "+element.wrtAngle);
		}
		if( count < list.size() ) {
			out.print("... [" + list.size() + "]");
		}
		out.println();
	}

	public void findPossibleAssociations(boolean verbose) {
		for( int i = 1; i < currSet.size(); i++ ) {
			nextLevel();
			sortAndTrim();
			if( verbose ) {
				printLevelOdds(System.out);
				printOddsComponents(System.out);
				System.out.println();
			}
		}
	}
	
	public AssocMapSet createAssocMapSet() {
		final int size = list.size();
		AssocMap[] maps = new AssocMap[size];
		double[] weights = new double[size];
		for( int i = 0; i < size; i++ ) {
			AssocTreeNode element = list.get(i);
			maps[i] = new AssocMap(currSet, prevSet, element.getPreviousMap(), element.getCurrentMap());
			weights[i] = element.getOdds();
		}
		return new AssocMapSet(maps, weights);
	}
}
