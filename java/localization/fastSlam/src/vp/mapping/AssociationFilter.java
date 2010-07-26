package vp.mapping;

import java.util.Arrays;
import java.util.Random;

import oursland.RandomSingleton;
import oursland.math.DiscreteDistribution;
import oursland.math.Gaussian;
import oursland.math.Geometries;
import vp.robot.RobotPose;
import vp.robot.TriangulationActionGenerator;

public class AssociationFilter {
	private static final Random random = RandomSingleton.instance;
	private static final int PREV = 0;
	private static final int CURR = 1;
	private int[][][] associations;
	private double[] confidence;
	TriangulationActionGenerator actionGenerator = new TriangulationActionGenerator();

	public AssociationFilter(int filterSize, LandmarkObservationSet currOb, LandmarkObservationSet prevOb, double sensorVar, double dtime) {
	
		if( currOb.size() > 1 ) {
			this.confidence = new double[filterSize];
			this.associations = new int[filterSize][2][2];

			buildParticles(currOb, prevOb, sensorVar, dtime);
		} else {
			this.confidence = new double[0];
			this.associations = new int[0][2][2];
		}
	}

	public boolean getSample(int[] prevAssoc, int[] currAssoc) {
		int index = DiscreteDistribution.sample(confidence);
		if( index >=0 && index < confidence.length ) {
			int[][] rc = associations[index];
			prevAssoc[0] = rc[PREV][0];
			prevAssoc[1] = rc[PREV][1];
			currAssoc[0] = rc[CURR][0];
			currAssoc[1] = rc[CURR][1];
			if( prevAssoc[0] == prevAssoc[1] && prevAssoc[0] != -1 ) {
				System.out.println("Error");
			}
			return (prevAssoc[0]!=-1)&&(currAssoc[0]!=-1);
		}
		return false;
	}

	private void buildParticles(
		LandmarkObservationSet currOb,
		LandmarkObservationSet prevOb,
		double sensorVar,
		double dtime) {
		final int currCount = currOb.size();
		final int prevCount = prevOb.size();
		if( currCount <= 1 ) {
			// we should build random particles.
			return;
		}
		for (int i = 0; i < this.confidence.length; i++) {
			associations[i][CURR][0] = random.nextInt(currCount);
			associations[i][CURR][1] = getOtherLandmark(currOb, associations[i][CURR][0]);
			if( getAssociationSample(prevOb, currOb, associations[i][PREV], associations[i][CURR]) ) {
				RobotPose poseOffset =
					actionGenerator.getNextPose(
						new RobotPose(0, 0, 0),
						prevOb.get(associations[i][PREV][0]),
						prevOb.get(associations[i][PREV][1]),
						currOb.get(associations[i][CURR][0]),
						currOb.get(associations[i][CURR][1]),
						dtime);
				// the confidence of the association is how will it fits into a odometrry-less motion model
				double xc = Gaussian.productArea(0, poseOffset.getX(), sensorVar, sensorVar);
				double yc = Gaussian.productArea(0, poseOffset.getY(), sensorVar, sensorVar);
				confidence[i] = xc*yc;
				if( Double.isNaN(confidence[i])) {
					System.out.println("Error");
					confidence[i] = 0;
				}
			}
		}
		double[] backup = new double[confidence.length]; // (double[]) confidence.clone();
		System.arraycopy(confidence, 0, backup, 0, confidence.length);
		DiscreteDistribution.normalize(confidence);
		if( Double.isNaN(confidence[0]) ) {
			System.out.println("Error");
		}
	}

	public static boolean getAssociationSample(
		LandmarkObservationSet prevObservations,
		LandmarkObservationSet observations,
		int[] prevAssoc,
		int[] currAssoc) {
			
		if( prevObservations.size() <= 1 ) {
			prevAssoc[0] = -1;
			prevAssoc[1] = -1;
			return false;
		}

		LandmarkObservation origin = new LandmarkObservation(0, 0, null);

		LandmarkObservation ob1 = observations.get(currAssoc[0]);
		LandmarkObservation ob2 = observations.get(currAssoc[1]);
		double obAngle = getLandmarksTheta(ob1, ob2);
//		double poseObAngle = getLandmarksTheta(origin, ob1);

		// get the length between the two landmarks
		double obLenSq = observations.getDistanceSq(currAssoc[0], currAssoc[1]);
		//		double obLenSq = getLandmarkDistanceSq(ob1, ob2);

		// find the pair in the active landmarks that has the lenSq closest to our observation
		int[] bestAssoc = sampleDistance(prevObservations, obLenSq, 10000, 10000);
		//		LandmarkObservation lm1 = prevObservations.get(bestAssoc[0]);
		//		LandmarkObservation lm2 = prevObservations.get(bestAssoc[1]);
		//		if (lm1.getAssociationName() != ob1.getAssociationName()
		//			|| lm2.getAssociationName() != ob2.getAssociationName()) {
		//			//			System.out.println("WARNING: Data association failure.");
		//		} else {
		//			//			System.out.println("correct Data association.");
		//		}
		prevAssoc[0] = bestAssoc[0];
		prevAssoc[1] = bestAssoc[1];

		return true;
	}

	public static int getOtherLandmark(LandmarkObservationSet observations, int thisOne) {
		int other = random.nextInt(observations.size());
		while (thisOne == other) {
			other = random.nextInt(observations.size());
		}
		return other;
	}

	public static double getLandmarksTheta(GenericLandmark lm1, GenericLandmark lm2) {
		double dx = lm2.getX() - lm1.getX();
		double dy = lm2.getY() - lm1.getY();
		return Geometries.getAngle(dx, dy);
	}

	private static int[] sampleDistance(
		LandmarkObservationSet observations,
		double obLenSq,
		double lmVar,
		double obVar) {
		final int obCount = observations.size();
		if( obCount <= 1 ) {
			return new int[] { -1, -1 };
		}
		double[] p = new double[obCount * obCount];
		for (int i = 0; i < observations.size(); i++) {
			for (int j = i + 1; j < observations.size(); j++) {
				double lmLenSq = observations.getDistanceSq(i, j);
				double matchConfidence = i==j?0:Gaussian.productArea(lmLenSq, obLenSq, lmVar, obVar);
				p[j + obCount * i] = matchConfidence;
				p[i + obCount * j] = matchConfidence;
			}
		}
		DiscreteDistribution.normalize(p);
		int index = DiscreteDistribution.sample(p);
		int[] rc =  new int[] { index / obCount, index % obCount };
		// sometimes all of the length have zero confidence
		// fix it
		if( rc[0] == rc[1] ) {
			Arrays.fill(p, 1.0/p.length);
			while( rc[0] == rc[1] ) {
				index = DiscreteDistribution.sample(p);
				rc[0] = index / obCount;
				rc[1] = index % obCount;
			}
		}
		return rc;
	}

}
