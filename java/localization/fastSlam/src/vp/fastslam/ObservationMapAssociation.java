package vp.fastslam;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import oursland.kalman.Matrix2;
import oursland.naming.UniqueName;
import oursland.naming.UniqueNameGenerator;
import vp.mapping.GenericLandmark;
import vp.mapping.LandmarkObservation;
import vp.mapping.LandmarkObservationSet;
import vp.mapping.QuadMap3;

/**
 * @author oursland
 */
public class ObservationMapAssociation {


	public static ObservationMapAssociation createKnownAssociation(
		LandmarkObservationSet lms,
		LandmarkMap map,
		double[] sensorErrorCovariance,
		List observationPrediction) {
		double[] inverseSensorCovariance = Matrix2.inverse2_2(sensorErrorCovariance, new double[4]);
		for (Iterator iter = lms.iterator(); iter.hasNext();) {
			LandmarkObservation p = (LandmarkObservation) iter.next();
			UniqueName associationName = p.getAssociationName();
			double associationConfidence = 0.0;
			KalmanLandmark landmark = null;
			if (associationName != null) {
				landmark = map.getLandmark(associationName);
			}
			if (landmark != null) {
				associationConfidence = distanceMetric(landmark, p);
			}
			observationPrediction.remove(landmark);
		}

		return null;
	}

	private static double distanceMetric( KalmanLandmark landmark, LandmarkObservation p) {
		final double dx = landmark.getX() - p.getX();
		final double dy = landmark.getY() - p.getY();
		return Matrix2.getMahalanobisDistance2(landmark.getPosterioriErrorCovariance(), dx, dy);
	}

	public static ObservationMapAssociation createNearestNeighbor(
		LandmarkObservationSet lms,
		LandmarkMap map,
		UniqueNameGenerator lmGen
	) {
		ObservationMapAssociation rc = new ObservationMapAssociation(lmGen);
		for (Iterator iter = lms.iterator(); iter.hasNext();) {
			LandmarkObservation p = (LandmarkObservation) iter.next();
			// find association in map
			KalmanLandmark landmark = getMapAssociation(map, p);
			if( landmark != null ) {
				rc.map.put(p, landmark);
			}
		}
		return rc;
	}
	
	public static KalmanLandmark getMapAssociation(LandmarkMap map, LandmarkObservation p) {
		final double r = 500;
		Rectangle2D bounds = new Rectangle2D.Double(p.getX() - r, p.getY() - r, 2 * r, 2 * r);
		KalmanLandmark bestLandmark = null;
		double bestMetric = Double.POSITIVE_INFINITY;
		Iterator iter = map.getLandmarkIterator(bounds);
		while(iter.hasNext()) {
			KalmanLandmark lm = (KalmanLandmark) iter.next();
			double metric = distanceMetric(lm, p);
			if( metric < bestMetric ) {
				bestMetric = metric;
				bestLandmark = lm;
			}
		}
		return bestLandmark;
	}

	public static ObservationMapAssociation createOther(
		LandmarkObservationSet lms,
		LandmarkMap map,
		double[] sensorErrorCovariance,
		double[] processErrorCovariance,
		double dt,
		GenericLandmark[] near,
		List observationPrediction,
		QuadMap3 quadmap,
		double confidenceCutoff,
		UniqueNameGenerator lmGen
	) {
		final boolean useKnownDataAssociations = false;
		double[] inverseSensorCovariance = Matrix2.inverse2_2(sensorErrorCovariance, new double[4]);
		double associationConfidence = 0.0;
		ObservationMapAssociation rc = new ObservationMapAssociation(lmGen);
		for (Iterator iter = lms.iterator(); iter.hasNext();) {
			LandmarkObservation p = (LandmarkObservation) iter.next();
			UniqueName associationName =
				determineAssociation(
					p,
					sensorErrorCovariance,
					inverseSensorCovariance,
					false,
					near,
					quadmap,
					confidenceCutoff);
			KalmanLandmark landmark = null;
			if (associationName != null) {
				landmark = map.getLandmark(associationName);
			}
			if (landmark != null) {
				associationConfidence = distanceMetric(landmark, p);
			}
			observationPrediction.remove(landmark);
			if (landmark == null || associationConfidence < confidenceCutoff) {
				// this observation does not match anything in the map
				// add a new landmark
				//			if( rand.nextDouble() < 0.01 ) {
				//				System.out.println("New landmark at " + p + "a with confidence " + associationConfidence);
				//			}
				//				if (!useKnownDataAssociations || associationName == null) {
				//					associationName = lmGen.create();
				//				}
				//				KalmanLandmark newLandmark = new KalmanLandmark(p, sensorErrorCovariance, associationName);
				//			nextActiveLandmarkSet.add(newLandmark.getAssociationName());
				//			landmarkFilters.add( newLandmark );
				//				addLandmark(newLandmark.getAssociationName(), newLandmark);
				//				associationConfidence = confidenceCutoff; // What is the importance factor for new landmarks?
				//				posteriori = newLandmark.getPosterioriErrorCovariance();
				//				assert validateMapContents();
			} else {
				// update the current landmark
				KalmanLandmark oldLandmark = landmark;
				landmark = new KalmanLandmark(landmark);

				//				assert validateMapContents();
				landmark.timeUpdate(processErrorCovariance, dt);
				//				assert validateMapContents();
				landmark.measurementUpdate(p, sensorErrorCovariance);
				//				assert validateMapContents();

				//			nextActiveLandmarkSet.add(landmark.getAssociationName());
				landmark.incrementObservationCount();
				//				posteriori = landmark.getPosterioriErrorCovariance();
				//				this.importanceWeight *= associationConfidence;
				landmark.resetAge();
				//				assert validateMapContents();

				// landmark must ve added after updating its position
				//				addLandmark(associationName, landmark);
				rc.map.put(p, landmark);
			}
		}
		return rc;
	}

	private final HashMap<LandmarkObservation, KalmanLandmark> map = new HashMap<LandmarkObservation, KalmanLandmark>();
	private final UniqueNameGenerator lmGen;

	public ObservationMapAssociation(UniqueNameGenerator lmGen) {
		this.lmGen = lmGen;
	}

	public KalmanLandmark getAssociation(LandmarkObservation p) {
		return map.get(p);
	}

	private static UniqueName determineAssociation(
		LandmarkObservation p,
		double[] sensorErrorCovariance,
		double[] inverseSensorCovariance,
		boolean useKnownDataAssociations,
		GenericLandmark[] near,
		QuadMap3 quadmap,
		double confidenceCutoff) {
		UniqueName rc = null;
		double importanceWeight = 0.0;
		if (useKnownDataAssociations && p.getAssociationName() != null) {
			rc = p.getAssociationName();
		} else {
			KalmanLandmark bestLandmark = null;
			double bestConfidence = 0;
			// compare to the landmarks seen in the previous time period
			//			for (Iterator iter = prevActiveLandmarkSet.iterator(); iter.hasNext(); ) {
			//				KalmanLandmark landmark = (KalmanLandmark) landmarkNameMap.get(iter.next());
			//				double confidence = calculateConfidence(landmark, p, sensorErrorCovariance, inverseSensorCovariance, 10);
			//				if( confidence > bestConfidence ) {
			//					bestConfidence = confidence;
			//					bestLandmark = landmark;
			//				}					
			//			}
			if (bestConfidence < confidenceCutoff) {
				// if we don't have a definite match, look at all of the landmarks in an area.
				final double r = 300;
				Rectangle2D area = new Rectangle2D.Double(p.getX() - r, p.getY() - r, 2 * r, 2 * r);
				//				quadmap.getLandmarksInBounds(area, near); // why was this being called twice?
				quadmap.getLandmarksInBounds(area, near);
				for (int i = 0; near[i] != null; i++) {
					KalmanLandmark landmark = (KalmanLandmark) near[i];
					near[i] = null;
					//				for (Iterator iter = landmarkNameMap.values().iterator(); iter.hasNext();) {
					//					KalmanLandmark landmark = (KalmanLandmark) iter.next();
					//					double dx = p.getX() - landmark.getX();
					//					double dy = p.getX() - landmark.getY();
					//					double confidence1 = Matrix2.getMahalanobisDistance2(landmark.getPosterioriErrorCovariance(), dx, dy);
					//					double confidence2 = Matrix2.getMahalanobisDistance2(sensorErrorCovariance, -dx, -dy);
					//					double confidence = Math.min(confidence1, confidence2);
					double confidence =
						calculateConfidence(landmark, p, sensorErrorCovariance, inverseSensorCovariance, 10);
					//					System.out.println("\t"+confidence);
					if (confidence > bestConfidence) {
						//						if( rand.nextDouble() < 0.1 ) {
						//							System.out.println(confidence1 + "\t" +confidence2);
						//						}
						bestConfidence = confidence;
						bestLandmark = landmark;
					}
				}
			}
			if (bestLandmark != null) {
				rc = bestLandmark.getAssociationName();
			}
		}
		return rc;
	}

	private static double calculateConfidence(
		KalmanLandmark landmark,
		LandmarkObservation sensor,
		double[] sensorErrorCovariance,
		double[] inverseSensorCovariance,
		int count) {
		//		return estimateGaussianIntersection(landmark, sensor, sensorErrorCovariance, inverseSensorCovariance);
		//		return estimateGaussianIntersectionMulti(landmark, sensor, sensorErrorCovariance, inverseSensorCovariance, 10);
		return landmark.getObservationConfidence(sensor, sensorErrorCovariance, inverseSensorCovariance);
	}
	
	public KalmanLandmark createLandmark(LandmarkObservation p, double[] sensorErrorCovariance) {
		UniqueName associationName = lmGen.create();
		KalmanLandmark rc = new KalmanLandmark(p, sensorErrorCovariance, associationName);
		map.put(p, rc);
		return rc;
	}

	public int size() {
		return map.size();
	}
}
