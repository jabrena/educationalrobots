package vp.dataassoc;

import java.util.Random;

import vp.mapping.LandmarkObservation;
import vp.mapping.LandmarkObservationSet;
import vp.robot.RobotPose;
import vp.robot.TriangulationActionGenerator;


public class AssocMap {
	private static final Random rand = new Random();
	private static final TriangulationActionGenerator trig = new TriangulationActionGenerator();

	private final LandmarkObservationSet currSet;
	private final LandmarkObservationSet prevSet;
	private final int[] prevMap;
	private final int[] currMap;

	public AssocMap(LandmarkObservationSet currSet, LandmarkObservationSet prevSet, int[] prevMap, int[] currMap) {
		this.currSet = currSet;
		this.prevSet = prevSet;
		this.prevMap = prevMap;
		this.currMap = currMap;
	}
	
	public int getAssocForPrevious(int previousIndex) {
		return prevMap[previousIndex];
	}
	
	public int getAssocForCurrent(int currentIndex) {
		return currMap[currentIndex];
	}
	
	public RobotPose getRandomCurrentPose(RobotPose prevPose) {
		if( currMap.length < 2 || prevMap.length < 2 ) {
			throw new Error("Cannot find the offset with fewer than two landmarks.");
		}
		int temp = rand.nextInt(currMap.length);
		int index1 = temp;
		while(currMap[index1] == -1 ) {
			index1++;
			index1 %= currMap.length;
			if( index1 == temp ) {
				throw new Error("All landmarks are though to be new.");				
			}
		}
		temp = rand.nextInt(currMap.length);
		int index2 = temp;
		while(currMap[index2] == -1 || index1 == index2) {
			index2++;
			index2 %= currMap.length;
			if( index2 == temp ) {
				throw new Error("All but one landmarks are thought to be new.");				
			}
		}
		return getCurrentPose(prevPose, index1, index2);
	}

	private RobotPose getCurrentPose(RobotPose prevPose, int index1, int index2) {
		LandmarkObservation c1 = currSet.get(index1);
		LandmarkObservation c2 = currSet.get(index2);
		LandmarkObservation p1 = prevSet.get(currMap[index1]);
		LandmarkObservation p2 = prevSet.get(currMap[index2]);
		return trig.getNextPose(prevPose, c1, c2, p1, p2, 1.0);
	}
}
