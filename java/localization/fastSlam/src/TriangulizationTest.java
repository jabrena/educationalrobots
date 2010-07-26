import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import oursland.RandomSingleton;
import vp.mapping.LandmarkObservation;
import vp.robot.RobotPose;
import vp.robot.TriangulationActionGenerator;

public class TriangulizationTest {
	
	private static Random r = RandomSingleton.instance;

	private static class Pose extends Point2D.Double implements Cloneable {
		private double theta;
		
		public Pose( double x, double y, double theta ) {
			super(x, y);
			this.theta = theta;
		}
		
		public Pose(Pose copy) {
			super(copy.x, copy.y);
			this.theta = copy.theta;
		}
		
		public void move( double dx, double dy ) {
			double ct = Math.cos(theta);
			double st = Math.sin(theta);
			setLocation(getX() + st*dy + ct*dx, getY() + ct*dy + st*dx);
		}
		
		public void turn( double dtheta ) {
			this.theta += dtheta;
		}
		
		public Object clone() {
			return super.clone();
		}
		
		public double getTheta() {
			return theta;
		}
		
		public String toString() {
			return getX() + " " + getY() + " " + getTheta();
		}
	}

	private static RobotPose moveRobot(RobotPose prevPose) {
//		Pose rc = (Pose) prevPose.clone();
		double dx = 2*r.nextDouble() - 1;
		double dy = 2*r.nextDouble() - 1;
		double dtheta = 1.0*r.nextDouble() - 0.5;
//		double dx = 0;
//		double dy = 0;
//		double dtheta = 0.0;
//		rc.move(dx, dy);
//		rc.turn(dtheta);
		
//		System.out.println("Moved " + dx + " " + dy + " " + dtheta);
		return moveRobot(prevPose, dx, dy, dtheta);
	}

	private static RobotPose moveRobot(RobotPose prevPose, double dx, double dy, double dtheta) {
		double c = Math.cos(prevPose.getTheta());
		double s = Math.sin(prevPose.getTheta());
		return new RobotPose(
			prevPose.getX() + s*dy + c*dx, 
			prevPose.getY() + c*dy + s*dx, 
			prevPose.getTheta()+dtheta);
	}
		
	private static ArrayList getSensorImage(ArrayList world, RobotPose pose) {
		AffineTransform xform = new AffineTransform();
		xform.translate(-pose.getX(), -pose.getY());
		xform.rotate(-pose.getTheta());
			
		ArrayList<LandmarkObservation> rc = new ArrayList<LandmarkObservation>();
		for (Iterator iter = world.iterator(); iter.hasNext();) {
			LandmarkObservation lm = (LandmarkObservation) iter.next();
			Point2D p = new Point2D.Double(lm.getX(), lm.getY());
			xform.transform(p, p);
			rc.add(new LandmarkObservation(p, null));
		}
		return rc;
	}

	private static double getLandmarksTheta(Point2D p1, Point2D p2) {
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		double h = Math.sqrt(dx*dx + dy*dy);
		double theta;
		// use the smaller value for the more precise angle
		if( Math.abs(dx) > Math.abs(dy) ) {
			theta = Math.asin(dy/h);
		} else {
			theta = Math.acos(dx/h);
		}
		return theta;
	}

	private static Pose localToLandmarks(Point2D lm1, Point2D lm2, Pose pose) {
		double theta = getLandmarksTheta(lm1, lm2);
		
//		Point2D lm2_ = (Point2D) lm2.clone();
//		AffineTransform.getTranslateInstance(-lm1.getX(), -lm1.getY()).transform(lm2, lm2_);
//		AffineTransform.getRotateInstance(-theta).transform(lm2_, lm2_);
//		System.out.println(lm2_);
			
		Pose rc = new Pose(pose);
		AffineTransform.getTranslateInstance(-lm1.getX(), -lm1.getY()).transform(rc, rc);		
		AffineTransform.getRotateInstance(-theta).transform(rc, rc);
		rc.turn(-theta);
//		xform.transform(rc, rc);
		return rc;
	}
	
	private static Pose landmarksToLocal(Point2D lm1, Point2D lm2, Pose pose) {
		double theta = getLandmarksTheta(lm1, lm2);
//		AffineTransform xform = new AffineTransform();
//		xform.translate(lm1.getX(), lm1.getY());
//		xform.rotate(theta);
		Pose rc = new Pose(pose.getX(), pose.getY(), pose.getTheta()+theta);
		AffineTransform.getRotateInstance(theta).transform(rc, rc);
		rc.move(lm1.getX(), lm1.getY());
//		xform.transform(rc, rc);
		return rc;
	}

	public static Pose getPoseOffset(Pose p1, Pose p2) {
		System.out.println("Pose 1: " + p1);
		System.out.println("Pose 2: " + p2);
		Pose rc = new Pose(p2);
//		AffineTransform.getRotateInstance(-p1.getTheta()).transform(rc, rc);
		AffineTransform.getTranslateInstance(-p1.getX(), -p1.getY()).transform(rc,rc);
//		System.out.println("Translated p2: " + rc);
		AffineTransform.getRotateInstance(-p2.getTheta()).transform(rc, rc);
		rc.turn(-p1.getTheta());
		return rc;
	}
/*
prevOb1: lm8: Point2D.Double[1502.1146150571892, -873.9112792974785]
prevOb2: lm34: Point2D.Double[1065.3222422278143, -363.4532847199108]
currOb1: lm8: Point2D.Double[1502.1413245884494, -873.8653682435429]
currOb2: lm34: Point2D.Double[1065.3333503199644, -363.4207240307442]
Offset: (-0.000, 0.000, -0.000)
(0.000, 0.000, -1.600)	--	(0.000, 0.000, -1.600)
prevOb1: lm32: Point2D.Double[88.07069318504678, 977.1490089327663]
prevOb2: lm8: Point2D.Double[1502.1413245884494, -873.8653682435429]
currOb1: lm32: Point2D.Double[86.04620337270309, 977.1511657715939]
currOb2: lm8: Point2D.Double[1500.1632253576151, -873.8277706951452]
Offset: (2.000, 0.000, -0.000)
(2.000, 0.000, -1.600)	--	(-0.058, -1.999, -1.600)
prevOb1: lm32: Point2D.Double[86.04620337270309, 977.1511657715939]
prevOb2: lm50: Point2D.Double[894.5620941820335, -1233.9177442980695]
currOb1: lm32: Point2D.Double[82.22612168705248, 977.1528558267202]
currOb2: lm50: Point2D.Double[890.7874525338282, -1233.89943777177]
Offset: (3.800, 0.000, -0.000)
(5.800, 0.000, -1.600)	--	(-0.169, -5.798, -1.600)
*/
	public static void main(String[] args) {
		TriangulationActionGenerator trig = new TriangulationActionGenerator();

//		ArrayList world = new ArrayList();
//		world.add( new LandmarkObservation(1722.6349209675643, -229.34290901363624, null) );
//		world.add( new LandmarkObservation(1123.1809663214558, 73.98842377150322, null) );
		
		RobotPose prevPose = new RobotPose(0.000, 0.000, -1.600);
//		RobotPose nextPose = moveRobot(prevPose);
		RobotPose nextPose = new RobotPose(-0.058, -1.999, -1.600);
		
		System.out.println("Actual Location 0: " + prevPose);
		System.out.println("Actual Location 1: " + nextPose);
		
//		ArrayList sensorImage1 = getSensorImage(world, prevPose);
//		ArrayList sensorImage2 = getSensorImage(world, nextPose);
		
//		LandmarkObservation l1 = (LandmarkObservation) sensorImage1.get(0);
//		LandmarkObservation l2 = (LandmarkObservation) sensorImage1.get(1);
//		LandmarkObservation s1 = (LandmarkObservation) sensorImage2.get(0);
//		LandmarkObservation s2 = (LandmarkObservation) sensorImage2.get(1);

		LandmarkObservation l1 = new LandmarkObservation(88.07069318504678, 977.1490089327663, null);
		LandmarkObservation l2 = new LandmarkObservation(1502.1413245884494, -873.8653682435429, null);
		LandmarkObservation s1 = new LandmarkObservation(86.04620337270309, 977.1511657715939, null);
		LandmarkObservation s2 = new LandmarkObservation(1500.1632253576151, -873.8277706951452, null);
		
//		Pose lm1 = localToLandmarks(s1, s2, new Pose(0,0,0)); 
			// Landmark sensors are in robot coordinates - always 0,0,0

		RobotPose estimatePose = trig.getNextPose(prevPose, l1, l2, s1, s2, 0.1);

//		System.out.println("lm 1: " + lm1);
//		Pose poseOffset = getPoseOffset(lm0, lm1); 
		
		System.out.println("Estimated moved to: " + estimatePose);
		
//		System.out.println(nextPose.distance(0,0));
//		System.out.println(poseOffset.distance(0,0));
//		System.out.println(poseOffset2);
		
		// time passes and the world is updated.
		
		// reset the coordinate space to the robot's frame of reference
//		Point2D nextP1 = new Point2D.Double(p1.getX(), p1.getY());
//		Point2D nextP2 = new Point2D.Double(p2.getX(), p2.getY());
		
		
		
		// the points are now the robot's observations
		
		// select two observed points and give a possible associate with points in the model (easy in this case)
	}
}
