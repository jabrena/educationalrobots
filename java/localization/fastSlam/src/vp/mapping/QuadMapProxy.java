package vp.mapping;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author oursland
 */
public class QuadMapProxy implements Cloneable {
	private QuadMap2 qm2;
	private QuadMap3 qm3;
	
	public QuadMapProxy(Rectangle2D bounds) {
		qm2 = new QuadMap2(bounds);
		qm3 = new QuadMap3(bounds);
	}
	
	public Object clone() {
		try {
			QuadMapProxy rc = (QuadMapProxy) super.clone();
			rc.qm2 = qm2.clone();
			rc.qm3 = (QuadMap3) qm3.clone();
			return rc;
		} catch(CloneNotSupportedException e) {
			throw new Error();
		}
	}

	public void getLandmarksInBounds(Rectangle2D bounds, List<GenericLandmark> result) {
		List<GenericLandmark> check = new ArrayList<GenericLandmark>();
		qm2.getLandmarksInBounds(bounds, result);
		qm3.getLandmarksInBounds(bounds, check);
		List<GenericLandmark> resultList = result;
		List<GenericLandmark> checkList = check;
		if( !resultList.containsAll(checkList) || !checkList.containsAll(resultList) ) {
			ArrayList<GenericLandmark> resultArrayList = new ArrayList<GenericLandmark>(resultList);
			ArrayList<GenericLandmark> recheck = new ArrayList<GenericLandmark>();
			qm3.getLandmarksInBounds(bounds, recheck);
			throw new Error();
		}
	}

	public void getLandmarksInBounds(Rectangle2D bounds, GenericLandmark[] result) {
		GenericLandmark[] check = result.clone();
		qm2.getLandmarksInBounds(bounds, result);
		qm3.getLandmarksInBounds(bounds, check);
		List<GenericLandmark> resultList = Arrays.asList(result);
		List<GenericLandmark> checkList = Arrays.asList(check);
		if( !resultList.containsAll(checkList) || !checkList.containsAll(resultList) ) {
			throw new Error();
		}
	}

	public void add(GenericLandmark lm) {
		qm2.add(lm);
		qm3.add(lm);
		getLandmarkCount(); // for an error check
	}

	public int getLandmarkCount() {
		int count1 = qm2.getLandmarkCount();
		int count2 = qm2.getLandmarkCount();
		if( count1 != count2 ) throw new Error();
		return count1;
	}

	public void cleanup() {
		qm2.cleanup();
		qm3.cleanup();
	}

	public boolean remove(GenericLandmark lm) {
		boolean rc1 = qm2.remove(lm);
		boolean rc2 = qm3.remove(lm);
		if( rc1 != rc2 ) throw new Error();
		getLandmarkCount(); // for error check
		return rc1;
	}

	public boolean contains(GenericLandmark lm) {
		boolean rc1 = qm2.contains(lm);
		boolean rc2 = qm3.contains(lm);
		if( rc1 != rc2 ) throw new Error();
		return rc1;
	}
}
