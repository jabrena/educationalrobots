package vp.fastslam;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Random;
import oursland.RandomSingleton;
import oursland.kalman.GeneralKalmanFilter;
import oursland.kalman.KalmanFilter2D;
import oursland.naming.UniqueName;
import vp.VPConstant;
import vp.mapping.GenericLandmark;
import vp.mapping.LandmarkObservation;

public class KalmanLandmark implements GenericLandmark, Cloneable {
	private static final Shape big = new Ellipse2D.Double(-3, -3, 6, 6); 
	private static final Shape small = new Ellipse2D.Double(-1, -1, 2, 2); 
	private static final Random rand = RandomSingleton.instance;
	private GeneralKalmanFilter filter = new KalmanFilter2D(new double[] {1,0,0,1}); // identity covariance of 1
	private double[] expectedPosition = new double[2];
	private final UniqueName associationName;
	private int age = 0;
	private double observationCount = 1.0;

	public KalmanLandmark(KalmanLandmark copy) {
		filter = copy.filter.createCopy();
		expectedPosition[0] = copy.expectedPosition[0];
		expectedPosition[1] = copy.expectedPosition[1];
		this.associationName = copy.associationName;
		this.age = copy.age;
		this.observationCount = copy.observationCount;
	}
	
	public KalmanLandmark(LandmarkObservation landmark, double[] sensorError, UniqueName name) {
//		filter = new ExtKalmanFilter2D(sensorError);
		filter = new KalmanFilter2D(sensorError);
		expectedPosition[0] = landmark.getX();
		expectedPosition[1] = landmark.getY();
		this.associationName = name;
	}

	public void timeUpdate(double[] processNoise, double dt) {
		filter.setProcessNoiseCovariance(processNoise);
		filter.timeUpdate(dt);
	}

	public void measurementUpdate(LandmarkObservation lm, double[] sensorNoise) {
		// TODO: make KalmanLandmark const. Create copies on updates. Probably filters too.
		filter.setSensorNoiseCovariance(sensorNoise);
		Point2D oldPosition = new Point2D.Double(this.expectedPosition[0], this.expectedPosition[1]);
		this.expectedPosition = filter.measurementUpdate(this.expectedPosition, new double[] {lm.getX(), lm.getY()});
//		fireLandmarkUpdate(oldPosition);
	}

	public double[] getPosterioriErrorCovariance() {
		return filter.getPosterioriErrorCovariance();
	}

//	public double getMahalanobisDistance(LandmarkObservation lm) {
//		This seems wrong. We should also take into account the observation error covariance.
//		If the observation covariance is constant then all answers will be proportional.
//		return Matrix2.getMahalanobisDistance2(getPosterioriErrorCovariance(), lm.getX()-expectedPosition[0], lm.getY()-expectedPosition[1]);
//	}
//
	public KalmanLandmark clone() {
		try {
			KalmanLandmark rc = (KalmanLandmark) super.clone();
//			rc.xFilter = (KalmanFilter) rc.xFilter.clone();
//			rc.yFilter = (KalmanFilter) rc.yFilter.clone();
			rc.filter = (GeneralKalmanFilter) rc.filter.clone();
			rc.expectedPosition = rc.expectedPosition.clone();
//			rc.listeners = new ArrayList(); // listeners are not cloned
			return rc;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(getClass().getName() + ".clone() failed unexpectedly.");
		}
	}

	public void paint(Graphics2D g, Color fillColor, Color borderColor, int filterCount) {
		// TODO: Paint the error covariance around the landmark.
		AffineTransform xform = g.getTransform();
		double[] e = filter.getPosterioriErrorCovariance();
	//	g.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 127));

////		try {
//			g.setColor(Color.BLACK);
//			Shape errorCircle = new Ellipse2D.Double(-100, -100, 200, 200);
//			double[] covar = filter.getPosterioriErrorCovariance();
////			System.out.println(covar[0] + " " + covar[1] + " " + covar[2] + " " + covar[3] );
//			AffineTransform errorXform = new AffineTransform(filter.getPosterioriErrorCovariance());
////			AffineTransform errorXform = new AffineTransform();
//			errorXform.translate(expectedPosition[0], expectedPosition[1]);
//			g.transform(errorXform);
//			g.draw(errorCircle);
////		} catch (NoninvertibleTransformException e1) {
////			e1.printStackTrace();
////		}
//		g.setTransform(xform);

		for(int i = 0; i < filterCount; i++ ) {
			double rx = rand.nextGaussian();
			double ry = rand.nextGaussian();
			double x = e[0]*rx + e[1]*ry;
			double y = e[2]*rx + e[3]*ry;
			g.translate(expectedPosition[0]+x, expectedPosition[1]+y);
			g.scale(VPConstant.scaleDown, VPConstant.scaleDown);
			g.fill(small);
			g.setTransform(xform);
		}
		g.translate(expectedPosition[0], expectedPosition[1]);
		g.scale(VPConstant.scaleDown, VPConstant.scaleDown);
		g.setColor(fillColor);
		g.fill(big);
		g.setColor(borderColor);
		g.draw(big);
		g.setTransform(xform);
	}

	public double getX() {
		return expectedPosition[0];
	}

	public double getY() {
		return expectedPosition[1];
	}

//	public double[] localToGlobal(double[] local) {
//		return filter.localToGlobal(local, expectedPosition);
//	}
//
//	public double getPdfValue(double[] global) {
//		return filter.getPdfValue(global, expectedPosition);
//	}

	public UniqueName getAssociationName() {
		return associationName;
	}

	public void incrementAge() {
		age++;
	}
	
	public int getAge() {
		return age;
	}

	public void resetAge() {
		age = 0;
	}

//	public double[] getPosterioriErrorVariance() {
//		return filter.getPosterioriErrorVariance();
//	}

	public double getObservationConfidence(LandmarkObservation sensor, double[] sensorErrorCovariance, double[] inverseSensorCovariance) {
		double[] observed = { sensor.getX(), sensor.getY() };
		return filter.getProductArea(expectedPosition, observed, sensorErrorCovariance);
	}


	public void incrementObservationCount() {
		observationCount++;
	}
	public double getObservationCount() {
		return observationCount;
	}
	public void decrementObservationCount(double delta) {
		observationCount -= delta;
	}
	
//	private ArrayList listeners = new ArrayList();
//	public void addUpdateListener(LandmarkListener l) {
//		listeners.add(l);
//	}
//	
//	public void removeUpdateListener(LandmarkListener l) {
//		listeners.remove(l);
//	}
//	
//	public void fireLandmarkUpdate(Point2D oldPosition) {
//		LandmarkListener[] la = new LandmarkListener[listeners.size()]; 
//		listeners.toArray(la);
//		for (int i = 0; i < la.length; i++) {
//			la[i].notifyLandmarkMoved(this, oldPosition);
//		}
//	}
}
