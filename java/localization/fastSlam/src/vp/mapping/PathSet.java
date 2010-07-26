package vp.mapping;

import java.util.Comparator;
import java.util.Random;

import vp.model.ControlModel;
import vp.model.SimpleActionModel;
import vp.robot.RobotPath;
import vp.robot.RobotPose;

public class PathSet {
	private static final Comparator errorComparator = new Comparator() {

		public int compare(Object o1, Object o2) {
			RobotPath p1 = (RobotPath) o1;
			RobotPath p2 = (RobotPath) o2;
			double diff = p1.getError() - p2.getError();
			if( diff < 0 ) return -1;
			if( diff > 0 ) return 1;
			return 0;
		}
		
	};
	private RobotPath[] set;
	
	private PathSet(RobotPath[] set) {
		this.set = set;
	}
	
	public PathSet(LandmarkObservationSet lms, int size) {
		this.set = new RobotPath[size];
		for (int i = 0; i < set.length; i++) {
			set[i] = new RobotPath(lms, new RobotPose(0,0,0));
		}
	}
	
	public int size() {
		return set.length;
	}
	
	public RobotPath get(int i) {
		return set[i];
	}
	
	private LandmarkObservationSet lastObs;
	public PathSet update(Random rand, SimpleActionModel action, ControlModel control, LandmarkObservationSet lmset) {
		RobotPath[] na = new RobotPath[size()];
//		na[0] = new RobotPath( lmset, new RobotPose(set[0].getLatestPose(), action), set[0]);
		for (int i = 0; i < na.length; i++) {
			// select with a preference for earlier paths
			double rsel = 0.0;
			do {
				rsel = Math.pow(rand.nextDouble(), 7);
			} while (rsel == 1.0);
			
			int sel = (int) Math.floor(size()*rsel);
//			action.setObservations(lastObs, lmset);
			lastObs = lmset;
			na[i] = new RobotPath( lmset, control.nextPose(rand, set[sel].getLatestPose(), action), set[sel]);
		}
		return new PathSet(na);
	}
	
//	public void error() {
//		for (int i = 0; i < set.length; i++) {
//			set[i].error();
//		}
//		Arrays.sort(set, errorComparator);
//	}
}
