package vp.mapping;
import java.util.Arrays;
import java.util.Collections;

import oursland.RandomSingleton;
import oursland.math.DiscreteDistribution;
import oursland.math.Gaussian;

public class LocalDataAssociation {
	private static void sampleSingleAssociation(
		int currIndex,
		DiscreteDistribution[] assocDistributions,
		int[] currToPrevAssoc,
		int[] prevToCurrent
		) {
		int prevIndex = assocDistributions[currIndex].sample();
		currToPrevAssoc[currIndex] = prevIndex;
		if( prevToCurrent[prevIndex] != -1 ) {
			// there is an association collision
			// choose one biased towards less uncertainty
			if( RandomSingleton.instance.nextBoolean() ) {
				// reselect this index
				sampleSingleAssociation(currIndex, assocDistributions, currToPrevAssoc, prevToCurrent);
			} else {
				// reselect the other index
				int oldCurr = prevToCurrent[prevIndex];
				prevToCurrent[prevIndex] = currIndex;
				sampleSingleAssociation(oldCurr, assocDistributions, currToPrevAssoc, prevToCurrent);
			}
		} else {
			prevToCurrent[prevIndex] = currIndex;
		}
	}
	
	private static int[] sampleAssociation(LandmarkObservationSet prevOb, LandmarkObservationSet currOb, DiscreteDistribution[] assocDistributions) {
		final int currLen = currOb.size();
		Integer[] sampleOrder = new Integer[currLen];
		for (int i = 0; i < sampleOrder.length; i++) {
			sampleOrder[i] = new Integer(i);
		}
		Collections.shuffle( Arrays.asList(sampleOrder), RandomSingleton.instance );
		int[] rc = new int[currLen];
		int[] inUse = new int[prevOb.size()];
		Arrays.fill(inUse, -1);
		for (int i = 0; i < currLen; i++) {
			int randomOrderIndex = sampleOrder[i].intValue();
			sampleSingleAssociation(randomOrderIndex, assocDistributions, rc, inUse);
		}
		return rc;
	}

	private static DiscreteDistribution[] getAssociationDistributions(
		LandmarkObservationSet prevOb,
		LandmarkObservationSet currOb) {
		final int currLen = currOb.size();
		DiscreteDistribution[] assocMatrix = new DiscreteDistribution[currLen];
		for ( int i = 0; i < currLen; i++) {
			assocMatrix[i] = getAssociationDistribution(prevOb, currOb, i);
		}
		return assocMatrix;
	}
	
	private static DiscreteDistribution getAssociationDistribution(
		LandmarkObservationSet prevObservations,
		LandmarkObservationSet currObservation,
		int thisLm ) {
		final int currLen = currObservation.size();
		final int prevLen = prevObservations.size();
		double[] prevAssocDist = new double[prevLen];
		for( int thatLm = 0; thisLm < currLen; thatLm++ ) {
			if( thisLm == thatLm ) continue;
			double currLen2 = currObservation.getDistanceSq(thisLm, thatLm);
			for( int i = 0; i < prevLen; i++ ) {
				for( int j = 0; j < prevLen; j++ ) {
					if( i == j ) continue;
					double prevLen2 = prevObservations.getDistanceSq(i, j);
					double probability = Gaussian.productArea(currLen2, prevLen2, 50, 50);
					prevAssocDist[i] += probability;
				}
			}
		}
		return new DiscreteDistribution(prevAssocDist);
	}


}
