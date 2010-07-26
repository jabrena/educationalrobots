package vp.fastslam;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import oursland.RandomSingleton;
import oursland.kalman.Matrix2;
import oursland.math.MultiGaussian;
import oursland.naming.UniqueName;
import oursland.naming.UniqueNameGenerator;
import vp.VPConstant;
import vp.mapping.GenericLandmark;
import vp.mapping.LandmarkObservation;
import vp.mapping.LandmarkObservationSet;
import vp.mapping.QuadMap3;
import vp.model.SimpleActionModel;
import vp.robot.RobotPath;
import vp.robot.RobotPathPoseIterator;
import vp.robot.RobotPose;
import vp.sim.SensorImageGenerator;

public class LandmarkMap implements Cloneable {
	static double				minX					= Double.MAX_VALUE;
	static double				maxX					= -Double.MAX_VALUE;
	static double				minY					= Double.MAX_VALUE;
	static double				maxY					= -Double.MAX_VALUE;
	private static final Random	rand					= RandomSingleton.instance;
	//	private static double confidenceCutoff = 1E-300; // n: distance/sigma
	private static double		logConfidenceCutoff		= -300; // was -300					// n:
	// distance/sigma
	private static boolean		verbose					= false;
	//	private ArrayList landmarkFilters = new ArrayList();
	private QuadMap3			quadmap;
	private HashMap<UniqueName, KalmanLandmark> landmarkNameMap	 = new HashMap<UniqueName, KalmanLandmark>();
	private RobotPath			path					= null;
	private double				importanceWeight		= 1.0;
	private double				lastImportanceWeight	= 1.0;
	private double				logImportanceWeight		= 1.0;

	//	private TreeSet prevActiveLandmarkSet = new TreeSet();
	//	private TreeSet nextActiveLandmarkSet = new TreeSet();
	public LandmarkMap(Rectangle2D initialBoundsEstimate) {
		quadmap = new QuadMap3(initialBoundsEstimate);
	}

	public LandmarkMap(LandmarkMap original) {
		assert original.validateMapContents();
		this.quadmap =  new QuadMap3(original.quadmap); // (QuadMap3) original.quadmap.clone();
		this.landmarkNameMap = new HashMap<UniqueName, KalmanLandmark>(original.landmarkNameMap);
		this.path = original.path;
		this.importanceWeight = original.importanceWeight;
		this.lastImportanceWeight = original.lastImportanceWeight;
		assert validateMapContents();
	}

	protected Object clone() {
		try {
			LandmarkMap rc = (LandmarkMap) super.clone();
			//			rc.landmarkFilters = (ArrayList) rc.landmarkFilters.clone();
			rc.quadmap = (QuadMap3) rc.quadmap.clone(); // new QuadMap3(rc.quadmap);
			//			rc.quadmap = (QuadMap3) rc.quadmap.clone();
			rc.landmarkNameMap = new HashMap<UniqueName, KalmanLandmark>(rc.landmarkNameMap);
			//			for (Iterator iter = landmarkNameMap.entrySet().iterator();
			// iter.hasNext();) {
			//				Map.Entry entry = (Entry) iter.next();
			//				KalmanLandmark landmark = (KalmanLandmark) entry.getValue();
			//				rc.landmarkNameMap.put(entry.getKey(), landmark.clone());
			//			}
			// path is immutable
			//			rc.path = (LinkedList) rc.path.clone();
			return rc;
		} catch(CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	public void updateMap(RobotPose nextPose, LandmarkObservationSet observationSet, SensorImageGenerator gen, double dt, SensorErrorModel sensorErrorModel, double[] processError,
	//		ObservationMapAssociation assoc,
			//		boolean useKnownDataAssociations,
			UniqueNameGenerator lmGen) {
		path = new RobotPath(observationSet, nextPose, path);
		List observationPrediction = getLandmarksInView(nextPose, gen);
		// TODO: Determine if a landmark is in the robot's field of view, but is not seen. If it is, increase its error more.
		// TODO: Add log-odds filter to remove bad landmarks
		//		AffineTransform xform = new AffineTransform();
		//		xform.translate(nextPose.x, nextPose.y);
		//		xform.rotate(nextPose.theta);
		//		nextActiveLandmarkSet = prevActiveLandmarkSet;
		//		prevActiveLandmarkSet = new TreeSet();
		AffineTransform rotateXform = AffineTransform.getRotateInstance(nextPose.theta);
		AffineTransform translateXform = AffineTransform.getTranslateInstance(nextPose.x, nextPose.y);
		lastImportanceWeight = importanceWeight;
		logImportanceWeight = 0.0;
		importanceWeight = 1.0;
		observationSet = observationSet.transform(rotateXform).transform(translateXform);
		ObservationMapAssociation assoc = ObservationMapAssociation.createNearestNeighbor(observationSet, this, lmGen);
		//		for (Iterator iter = landmarkNameMap.values().iterator();
		// iter.hasNext();) {
		//			KalmanLandmark element = (KalmanLandmark) iter.next();
		//			System.out.println(element.getX() + " " + element.getY());
		//		}
		//		System.out.println();
		assert validateMapContents();
		//		GenericLandmark[] nearBuffer = new GenericLandmark[Math.max(1,
		// observationSet.size() + landmarkNameMap.size())];
		double[] sensorError = new double[4];
		for(Iterator iter = observationSet.iterator(); iter.hasNext();) {
			//			assert arrayIsNull(nearBuffer);
			LandmarkObservation element = (LandmarkObservation) iter.next();
			//			element = element.transform(rotateXform);
			//			element = element.transform(translateXform);
			
			sensorErrorModel.getSensorError(getLatestPose(), element, sensorError);
			double logNextEvidence = addLandmarkEvidence(element, sensorError, processError, dt, assoc, lmGen);
			if(logNextEvidence < logConfidenceCutoff) {
				logNextEvidence = logConfidenceCutoff;
			}
			logImportanceWeight += logNextEvidence;
			//			importanceWeight *= nextEvidence;
		}
		double extraLandmarks = Math.pow(0.7, observationPrediction.size() - assoc.size());
		logImportanceWeight += Math.log(extraLandmarks);
		//		importanceWeight *= extraLandmarks;
		assert landmarkNameMap.size() == quadmap.getLandmarkCount();
		assert validateMapContents();
		//		removeOldLandmarks();
		//		removeTransientLandmarks();
	}

	public KalmanLandmark getLandmark(UniqueName name) {
		return landmarkNameMap.get(name);
	}

	public RobotPose getLatestPose() {
		if(path == null) {
			return new RobotPose(0, 0, 0);
		}
		return path.getLatestPose();
	}

	public Iterator getPoseIterator() {
		return new RobotPathPoseIterator(path);
	}

	public SimpleActionModel getPreviousAction(double dt) {
		if(path != null) {
			RobotPath prev = path.previousPath();
			if(prev != null) {
				return path.getLatestPose().getActionFrom(prev.getLatestPose(), dt);
			}
		}
		return new SimpleActionModel(0, 0, 0, dt);
	}

	public RobotPath getPath() {
		return path;
	}

	public RobotPose[] getPoseArray() {
		return path.getPoseArray();
	}

//	public Iterator getLandmarkIterator() {
//		return quadmap.iterator();
//	}

	public Iterator<GenericLandmark> getLandmarkIterator(Rectangle2D bounds) {
		ArrayList<GenericLandmark> list = new ArrayList<GenericLandmark>();
		quadmap.getLandmarksInBounds(bounds, list);
		return list.iterator();
	}

	public void setVerbose(boolean b) {
		verbose = b;
	}

	public int getLandmarkCount() {
		return landmarkNameMap.size();
	}

	public void normalizeImportanceWeight(double sum) {
		this.importanceWeight /= sum;
	}

	public void scaleLogImportanceWeight(double scale) {
		this.logImportanceWeight += scale;
	}

	public void convertLogImportance() {
		this.importanceWeight = Math.exp(this.logImportanceWeight);
	}

	public void setImportanceWeight(double d) {
		this.lastImportanceWeight = this.importanceWeight;
		this.importanceWeight = d;
	}

	public double getImportanceWeight() {
		return importanceWeight;
	}

	public double getLogImportanceWeight() {
		return logImportanceWeight;
	}

	public double getLastImportanceWeight() {
		return lastImportanceWeight;
	}

	public void paintLandmarks(Graphics2D g, Color fillColor, Color borderColor, Rectangle2D bounds) {
		ArrayList<GenericLandmark> results = new ArrayList<GenericLandmark>();
		quadmap.getLandmarksInBounds(bounds, results);
		for(Iterator iter = results.iterator(); iter.hasNext();) {
			KalmanLandmark lm = (KalmanLandmark) iter.next();
			//			if(lm.getObservationCount() < 5) {
			//				lm.paint(g, g.getBackground(), borderColor, 0);
			//			} else {
			lm.paint(g, fillColor, borderColor, 0);
			//			}
		}
	}

	public void paintPath(Graphics2D g, Color color, int pathDisplayAge, float pathAgeDecay) {
		Stroke oldStroke = g.getStroke();
		g.setStroke(VPConstant.basicStroke);
		try {
			Iterator poseIterator = getPoseIterator();
			Point2D last = ((RobotPose) poseIterator.next()).getLocation();
			Point2D next = ((RobotPose) poseIterator.next()).getLocation();
			float decay = 1.0f;
			for(int i = 0; i < pathDisplayAge; i++) {
				g.setColor(new Color(color.getRed(), color.getBlue(), color.getGreen(), (int) (decay * color.getAlpha())));
				g.draw(new Line2D.Double(last, next));
				last = next;
				next = ((RobotPose) poseIterator.next()).getLocation();
				decay *= pathAgeDecay;
			}
		} catch(NoSuchElementException e) {
			// Fewer poses available than requested.
		} finally {
			g.setStroke(oldStroke);
		}
	}

	public void paintRobot(Graphics2D g2) {
		getLatestPose().paint(g2, VPConstant.scaleDown);
	}

	public void paintSmallRobot(Graphics2D g2) {
		getLatestPose().paintTiny(g2);
	}

	//	private static double estimateConfidence1(
	//		KalmanLandmark landmark,
	//		LandmarkObservation sensor,
	//		double[] sensorErrorCovariance,
	//		double[] inverseSensorCovariance,
	//		double range,
	//		double count
	//		) {
	//		double rc = 0;
	//		double[] areaLen1 = { range/Math.sqrt(count), range/Math.sqrt(count) };
	//		double[] areaLen2 = { range/Math.sqrt(count), range/Math.sqrt(count) };
	//		areaLen1 = ExtKalmanFilter2D.localToGlobal(areaLen1, new double[] {0,0},
	// landmark.getPosterioriErrorVariance());
	//		areaLen2 = ExtKalmanFilter2D.localToGlobal(areaLen2, new double[] {0,0},
	// sensorErrorCovariance);
	//		final double area1 = areaLen1[0]*areaLen1[1];
	//		final double area2 = areaLen2[0]*areaLen2[1];
	//		for( int i = 0; i < count; i++ ) {
	//			double[] local = { range*rand.nextDouble() - range/2,
	// range*rand.nextDouble() - range/2 };
	//			double[] global = landmark.localToGlobal(local);
	//			double[] sensorPosition = {sensor.getX(), sensor.getY()};
	//			double pdf1 = landmark.getPdfValue(global);
	//			double pdf2 = ExtKalmanFilter2D.getPdfValue(global, sensorPosition,
	// sensorErrorCovariance, inverseSensorCovariance);
	//			rc += pdf1 * pdf2;
	//		}
	//		for( int i = 0; i < count; i++ ) {
	//			double[] local = { range*rand.nextDouble() - range/2,
	// range*rand.nextDouble() - range/2 };
	//			double[] sensorPosition = {sensor.getX(), sensor.getY()};
	//			double[] global = ExtKalmanFilter2D.localToGlobal(local, sensorPosition,
	// sensorErrorCovariance);
	//			double pdf1 = landmark.getPdfValue(global);
	//			double pdf2 = ExtKalmanFilter2D.getPdfValue(global, sensorPosition,
	// sensorErrorCovariance, inverseSensorCovariance);
	//			rc += pdf1 * pdf2;
	//		}
	//		return rc;
	//	}
	//	private double estimateGaussianIntersection(
	//		KalmanLandmark landmark,
	//		LandmarkObservation sensedValue,
	//		double[] sensorErrorCovariance,
	//		double[] inverseSensorCovariance) {
	//		double[] sensorErrorVariance =
	// ExtKalmanFilter2D.covar2var(sensorErrorCovariance);
	//		double rc = 0.0;
	//		final double range = 4;
	//		{
	//			double[] local = { 0, 0 };
	//			double[] global = landmark.localToGlobal(local);
	//			double[] sensorPosition = {sensedValue.getX(), sensedValue.getY()};
	//			double pdf1 = landmark.getPdfValue(global);
	//			double pdf2 = ExtKalmanFilter2D.getPdfValue(global, sensorPosition,
	// sensorErrorCovariance, inverseSensorCovariance);
	//			rc += pdf1 * pdf2;
	//		}
	//		{
	//			double[] local = { 0, 0 };
	//			double[] sensorPosition = {sensedValue.getX(), sensedValue.getY()};
	//			double[] global = ExtKalmanFilter2D.localToGlobal(local, sensorPosition,
	// sensorErrorVariance);
	//			double pdf1 = landmark.getPdfValue(global);
	//			double pdf2 = ExtKalmanFilter2D.getPdfValue(global, sensorPosition,
	// sensorErrorCovariance, inverseSensorCovariance);
	//			rc += pdf1 * pdf2;
	//		}
	//		return rc;
	//	}
	//	private double estimateGaussianIntersectionMulti(
	//		KalmanLandmark landmark,
	//		LandmarkObservation sensedValue,
	//		double[] sensorErrorCovariance,
	//		double[] inverseSensorCovariance,
	//		int count ) {
	//		double rc = 0.0;
	//		final double range = 4;
	//		double[] sensorErrorVariance =
	// ExtKalmanFilter2D.covar2var(sensorErrorCovariance);
	//		for( int i = 0; i < count; i++ ) {
	//			double[] local = { 2*rand.nextGaussian(), 2*rand.nextGaussian() };
	//			double[] global = landmark.localToGlobal(local);
	//			double[] sensorPosition = {sensedValue.getX(), sensedValue.getY()};
	//			double pdf1 = landmark.getPdfValue(global);
	//			double pdf2 = ExtKalmanFilter2D.getPdfValue(global, sensorPosition,
	// sensorErrorCovariance, inverseSensorCovariance);
	//			rc += pdf1 * pdf2;
	//		}
	//		for( int i = 0; i < count; i++ ) {
	//			double[] local = { 2*rand.nextGaussian(), 2*rand.nextGaussian() };
	//			double[] sensorPosition = {sensedValue.getX(), sensedValue.getY()};
	//			double[] global = ExtKalmanFilter2D.localToGlobal(local, sensorPosition,
	// sensorErrorVariance);
	//			double pdf1 = landmark.getPdfValue(global);
	//			double pdf2 = ExtKalmanFilter2D.getPdfValue(global, sensorPosition,
	// sensorErrorCovariance, inverseSensorCovariance);
	//			rc += pdf1 * pdf2;
	//		}
	//		return rc;
	//	}
	public void pack() {
		quadmap.cleanup();
		quadmap = null;
		if(path != null) {
			path.pack();
		}
	}

	public void unpack(Rectangle2D initialBoundsEstimate) {
		quadmap = new QuadMap3(initialBoundsEstimate);
		for(Iterator iter = landmarkNameMap.values().iterator(); iter.hasNext();) {
			KalmanLandmark lm = (KalmanLandmark) iter.next();
			quadmap.add(lm);
		}
		assert landmarkNameMap.size() == quadmap.getLandmarkCount();
	}

	private boolean arrayIsNull(Object[] a) {
		for(int i = 0; i < a.length; i++) {
			if(a[i] != null) {
				return false;
			}
		}
		return true;
	}

	void addLandmark(UniqueName name, KalmanLandmark lm) {
		assert validateMapContents();
		assert landmarkNameMap.size() == quadmap.getLandmarkCount();
		KalmanLandmark old = landmarkNameMap.remove(name);
		if(old != null && old != lm) {
			boolean removed = quadmap.remove(old);
			assert removed;
		}
		assert validateMapContents();
		landmarkNameMap.put(name, lm);
		quadmap.add(lm);
		assert validateMapContents();
		assert landmarkNameMap.size() == quadmap.getLandmarkCount();
	}

	private List getLandmarksInView(RobotPose pose, SensorImageGenerator gen) {
		LinkedList<GenericLandmark> rc = new LinkedList<GenericLandmark>();
		Rectangle2D aroundBot = gen.getOrientationFreeBounds(pose.x, pose.y);
		AffineTransform xform = new AffineTransform();
		xform.rotate(-pose.theta);
		xform.translate(-pose.x, -pose.y);
		quadmap.getLandmarksInBounds(aroundBot, rc);
		for(Iterator iter = rc.iterator(); iter.hasNext();) {
			GenericLandmark lm = (GenericLandmark) iter.next();
			Point2D local = xform.transform(new Point2D.Double(lm.getX(), lm.getY()), null);
			if(!gen.isSensed(local.getX(), local.getY())) {
				iter.remove();
			}
		}
		return rc;
	}

	private double addLandmarkEvidence(LandmarkObservation p, double[] sensorErrorCovariance, double[] processErrorCovariance, double dt, ObservationMapAssociation assoc,
	//		List observationPrediction,
			UniqueNameGenerator lmGen) {
		//		if(p.getX() < minX) {
		//			minX = p.getX();
		//			System.out.println("Min X: " + minX);
		//		}
		//		if(p.getX() > maxX) {
		//			maxX = p.getX();
		//			System.out.println("Max X: " + maxX);
		//		}
		//		if(p.getY() < minY) {
		//			minY = p.getY();
		//			System.out.println("Min Y: " + minY);
		//		}
		//		if(p.getY() > maxY) {
		//			maxY = p.getY();
		//			System.out.println("Max Y: " + maxY);
		//		}
		assert validateMapContents();
		KalmanLandmark landmark = assoc.getAssociation(p);
		double associationConfidence = -Double.MAX_VALUE;
		if(landmark != null) {
			double dx = p.getX() - landmark.getX();
			double dy = p.getY() - landmark.getY();
			//			associationConfidence =
			// Matrix2.getMahalanobisDistance2(landmark.getPosterioriErrorCovariance(),
			// dx, dy);
			//			associationConfidence =
			// likelihood1(landmark.getPosterioriErrorCovariance(), dx, dy);
			associationConfidence = likelihood2(landmark.getPosterioriErrorCovariance(), sensorErrorCovariance, dx, dy);
		}
		//		System.out.println(associationConfidence);
		//		double[] posteriori = new double[] { 0.0, 0.0 };
		if(associationConfidence < logConfidenceCutoff) {
			assert validateMapContents();
			// add a new landmark
			//			if( rand.nextDouble() < 0.01 ) {
			//				System.out.println("New landmark at " + p + "a with confidence "
			// + associationConfidence);
			//			}
			landmark = assoc.createLandmark(p, sensorErrorCovariance);
			//			nextActiveLandmarkSet.add(newLandmark.getAssociationName());
			//			landmarkFilters.add( newLandmark );
			associationConfidence = logConfidenceCutoff;
			// TODO: What is the importance factor for new landmarks?
			assert validateMapContents();
		} else {
			// update the current landmark
			landmark = new KalmanLandmark(landmark);
			assert validateMapContents();
			landmark.timeUpdate(processErrorCovariance, dt);
			assert validateMapContents();
			landmark.measurementUpdate(p, sensorErrorCovariance);
			assert validateMapContents();
			//			nextActiveLandmarkSet.add(landmark.getAssociationName());
			landmark.incrementObservationCount();
			landmark.resetAge();
			assert validateMapContents();
			// landmark must be added after updating its position
		}
		addLandmark(landmark.getAssociationName(), landmark);
		//		posteriori = landmark.getPosterioriErrorCovariance();
		//		if( closestDistance > 0 ) {
		//			System.out.println(closestDistance);
		//		}
		// TODO: What is correct calculation for fitness?
		//		System.out.println();
		//		bestConfidence = Math.pow(bestConfidence, 0.001);
		//		double simplicityPreference = landmarkFilters.size();
		//		double simplicityPreference = 0;
		//		return 1.0/bestConfidence + simplicityPreference;
		return associationConfidence;
		//		return bestConfidence*posteriori;
	}

	private double likelihood2(double[] landmarkError, double[] sensorError, double dx, double dy) {
		double[] error = {landmarkError[0] + sensorError[0], landmarkError[1] + sensorError[1], landmarkError[2] + sensorError[2], landmarkError[3] + sensorError[3]};
		double[] inverror = Matrix2.inverse2_2(error, new double[4]);
		double cov[][] = { {error[0], error[1]}, {error[2], error[3]}};
		double invcov[][] = { {inverror[0], inverror[1]}, {inverror[2], inverror[3]}};
		return MultiGaussian.logPdf(new double[] {dx, dy}, cov, invcov);
	}

	private double likelihood1(double[] landmarkError, double dx, double dy) {
		double associationConfidence;
		double[] error = landmarkError;
		double[] inverror = Matrix2.inverse2_2(error, new double[4]);
		double[][] cov = { {error[0], error[2]}, {error[1], error[3]}};
		double[][] invcov = { {inverror[0], inverror[2]}, {inverror[1], inverror[3]}};
		associationConfidence = MultiGaussian.logPdf(new double[] {dx, dy}, cov, invcov);
		return associationConfidence;
	}

	private boolean validateMapContents() {
		for(Iterator iter = landmarkNameMap.values().iterator(); iter.hasNext();) {
			KalmanLandmark element = (KalmanLandmark) iter.next();
			if(!quadmap.contains(element)) {
				return false;
			}
		}
		return true;
	}

	// ported from Tekkotsu_1.5
	private static double afsMeasurementUpdate(KalmanLandmark landmark, LandmarkObservation sensedValue, double theta) {
		/* Here are all the matrices 'n stuff we need to do the Kalman filter */
		double weight = 0.0;
		double[] H = new double[2];
		double[] K = new double[2];
		double expected_theta;
		/*
		 * This q variable is used to compute the Jacobean, H. It's also used
		 * later to alter the measurement covariance based on distance. See the
		 * note below.
		 */
		double q;
		/* What we now have is a great deal of 2D matrix math. */
		/*
		 * First we compute the Jacobean, H, which is as I understand it a
		 * transformation from 2D location space to "measurement space" (?) or
		 * at least the kind of numbers that bearings only sensors give us.
		 * Actually, it's a linear approximation of such a transformation.
		 */{
			double dx = landmark.getX() - sensedValue.getX();
			double dy = landmark.getY() - sensedValue.getY();
			q = dx * dx + dy * dy;
			/* Check against divide by 0--hopefully it won't ever happen */
			if(q < 0.0000001) {
				System.err.println("RARE ERROR: near divide by 0 in afsMeasurementUpdate.");
				//				p.gotweight = 0;
				return 0.0;
			}
			H[0] = -dy / q;
			H[1] = dx / q;
			/*
			 * While we're at it, let's also find the value of theta we expected
			 * to see for this landmark.
			 */
			expected_theta = Math.atan2(dy, dx);
		}
		{
			/* Now we compute K, the Kalman gain. */
			double[] H_Sigma = new double[2];
			double divisor;
			double dtheta;
			/*
			 * This is the measurement covariance we use to compute the Kalman
			 * gain. Here, in bearings-only FastSLAM, it's dynamic. We increase
			 * the covariance value linearly with the distance of the object.
			 * Measurements about distant objects therefore have less effect on
			 * where the objects are placed in the particle's map and don't
			 * drastically affect the particle's weighting. This is a kludge,
			 * but then so is the EKF.
			 */
			/*
			 * AFS_MEASURE_VARIANCE is now the measurement variance at a
			 * distance of a meter. AFS_VARIANCE_MULTIPLIER is multiplied by the
			 * distance and added to AFS_MEASUREMENT_VARIANCE to describe a
			 * linear change in covariance. It is a Good Idea to set this value
			 * such that covariance never becomes negative!
			 */
			/*
			 * If you don't like any of this, set AFS_VARIANCE_MULTIPLIER to 1
			 * and don't think about it.
			 */
			double AFS_VARIANCE_MULTIPLIER = 1.0;
			double AFS_MEASURE_VARIANCE = 4 * Math.PI / 180;
			double R = AFS_VARIANCE_MULTIPLIER * (Math.sqrt(q) - 1000) + AFS_MEASURE_VARIANCE;
			/*
			 * We should never let R get below AFS_MEASURE_VARIANCE, so we
			 * correct that here.
			 */
			if(R < AFS_MEASURE_VARIANCE)
				R = AFS_MEASURE_VARIANCE;
			double[] variance = landmark.getPosterioriErrorCovariance();
			H_Sigma[0] = H[0] * variance[0] + H[1] * variance[1];
			H_Sigma[1] = H[0] * variance[2] + H[1] * variance[3];
			/* Note measurement variance fudge factor */
			divisor = H_Sigma[0] * H[0] + H_Sigma[1] * H[1] + R;
			K[0] = H_Sigma[0] / divisor;
			K[1] = H_Sigma[1] / divisor;
			/* Hooray--now we can compute the new mean for the landmark */
			dtheta = find_dtheta(expected_theta, theta);
			double x = landmark.getX() + K[0] * dtheta;
			double y = landmark.getY() + K[1] * dtheta;
			/*
			 * Since we have the divisor and expected value already, we can go
			 * ahead and compute the weight for this particle.
			 */
			//			p.gotweight = 1;
			/* 2 * Math.PI is removed -- scaling doesn't need it */
			/*
			 * Why do we multiply here? If we have multiple measurements before
			 * a resampling, we want to be able to use them all to influence the
			 * resampling. We multiply together all the weights from the
			 * measurements to create a final weight that is used for
			 * resampling. DANGER: May fall to zero after too many improbable
			 * measurements. 300 weights of 0.05 is zero in double precision.
			 * The lesson: for now, move around and resample frequently.
			 */
			weight = (1 / Math.sqrt(divisor)) * Math.exp(-0.5 * dtheta * dtheta / divisor);
		}
		/* Kalman filters are rad, even if I don't understand them very well */
		return weight;
	}

	private static double find_dtheta(double th1, double th2) {
		double naive_delta = th2 - th1;
		if(naive_delta <= -Math.PI)
			return naive_delta + 2 * Math.PI;
		if(naive_delta > Math.PI)
			return naive_delta - 2 * Math.PI;
		return naive_delta;
	}

	//public double getPredictedSensorError(
	//	RobotPose pose,
	//	RangeFinderData observed,
	//	SensorImageGenerator sensorImageGen,
	//	final int oversampleCount,
	//	final double var) {
	//	AffineTransform xform = new AffineTransform();
	//	xform.rotate(-pose.theta);
	//	xform.translate(-pose.x, -pose.y);
	//	Rectangle2D aroundBot = sensorImageGen.getOrientationFreeBounds(pose.x,
	// pose.y);
	//	GenericLandmark[] mapped = quadmap.getLandmarksInBounds(aroundBot, new
	// GenericLandmark[quadmap.size()]);
	//	RangeFinderData predicted =
	//		new RangeFinderData(oversampleCount, -UsefulConstants.PIOVERTWO,
	// UsefulConstants.PIOVERTWO, 2600);
	//	for (int i = 0; i < mapped.length && mapped[i] != null; i++) {
	//		GenericLandmark lm = mapped[i];
	//		Point2D local = xform.transform(new Point2D.Double(lm.getX(), lm.getY()),
	// null);
	//		if (sensorImageGen.isSensed(local.getX(), local.getY())) {
	//			predicted.addLandmarkReading(Geometries.getAngle(local.getX(),
	// local.getY()), local.distance(0, 0), var);
	//		}
	//	}
	//
	//	return observed.getError(predicted);
	//}
	public void removeOldLandmarks() {
		assert validateMapContents();
		for(Iterator iter = landmarkNameMap.values().iterator(); iter.hasNext();) {
			KalmanLandmark landmark = (KalmanLandmark) iter.next();
			landmark.incrementAge();
			if(landmark.getAge() > 5) {
				iter.remove();
				quadmap.remove(landmark);
				assert landmarkNameMap.size() == quadmap.getLandmarkCount();
			}
		}
		assert validateMapContents();
	}

	public void removeTransientLandmarks() {
		assert validateMapContents();
		for(Iterator iter = landmarkNameMap.values().iterator(); iter.hasNext();) {
			KalmanLandmark landmark = (KalmanLandmark) iter.next();
			if(landmark.getObservationCount() < 5) {
				landmark.decrementObservationCount(0.2);
				//			if (RandomSingleton.instance.nextDouble() < 0.01) {
				//			}
			}
			if(landmark.getObservationCount() < -5) {
				//			System.out.println("Removing Landmark");
				iter.remove();
				quadmap.remove(landmark);
				assert landmarkNameMap.size() == quadmap.getLandmarkCount();
			}
		}
		assert validateMapContents();
	}
}
