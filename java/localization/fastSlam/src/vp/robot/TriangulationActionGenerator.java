package vp.robot;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import oursland.RandomSingleton;
import vp.mapping.GenericLandmark;
import vp.model.ControlModel;
import vp.model.SimpleControlModel;
import vp.model.SimpleActionModel;

public class TriangulationActionGenerator {

	private ControlModel controlModel = new SimpleControlModel(0,0,0);

	public RobotPose getNextPose(
		RobotPose prevPose,
		GenericLandmark prevOb1,
		GenericLandmark prevOb2,
		GenericLandmark currOb1,
		GenericLandmark currOb2,
		double dtime) {

//		System.out.println("prevOb1: " + prevOb1);
//		System.out.println("prevOb2: " + prevOb2);
//		System.out.println("currOb1: " + currOb1);
//		System.out.println("currOb2: " + currOb2);

		// The is the robot's pose in the robot's own coordinate space. This is a constant.
		RobotPose robotSpacePose = new RobotPose(0, 0, 0);

		// Get the previous location with respect to the mapped landmarks
		RobotPose prevLoc = localToLandmarks(prevOb1, prevOb2, robotSpacePose);
		// Get the next location with respect to the observed landmarks
		RobotPose currLoc = localToLandmarks(currOb1, currOb2, robotSpacePose);

		// Get the offset of the previous location to the new location
		SimpleActionModel robotAction = getRobotAction(prevPose, prevLoc, currLoc, dtime);

//		System.out.println("Offset: " + locOffset);

//		double c = Math.cos(prevPose.getTheta());
//		double s = Math.sin(prevPose.getTheta());
//		if( Math.abs(robotAction.getX()) < 300 && Math.abs(robotAction.getY()) < 300 )
//			System.out.println(robotAction);
			
//		controlModel.setObservations(prevObservations, currObservations);
		RobotPose nextPose = controlModel.nextPose(RandomSingleton.instance, prevPose, robotAction);

//		RobotPose nextPose = new RobotPose(
//			prevPose.getX()+c*robotAction.getX()+s*robotAction.getY(), 
//			prevPose.getY()+s*robotAction.getX()+c*robotAction.getY(), 
//			prevPose.getTheta()+robotAction.getTheta());
		return nextPose;
	}

	private static double getLandmarksTheta(GenericLandmark p1, GenericLandmark p2) {
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		double h = Math.sqrt(dx*dx + dy*dy);
		double theta;
		// use the smaller value for the more precise angle
		if( Math.abs(dx) > Math.abs(dy) ) {
			theta = Math.asin(dy/h);
			if( dx < 0 ) {
				theta = -theta;
			}
		} else {
			theta = Math.acos(dx/h);
			if( dy < 0 ) {
				theta = Math.PI-theta;
			}
		}
		return theta;
	}

	private static RobotPose localToLandmarks(GenericLandmark lm1, GenericLandmark lm2, RobotPose pose) {
		double landmarkAngle = getLandmarksTheta(lm1, lm2);

		Point2D lm2_ = new Point2D.Double(lm2.getX(), lm2.getY());
		AffineTransform.getTranslateInstance(-lm1.getX(), -lm1.getY()).transform(lm2_, lm2_);		
		AffineTransform.getRotateInstance(-landmarkAngle).transform(lm2_, lm2_);

		Point2D loc = new Point2D.Double(pose.getX(), pose.getY());
		AffineTransform.getTranslateInstance(-lm1.getX(), -lm1.getY()).transform(loc, loc);		
		AffineTransform.getRotateInstance(-landmarkAngle).transform(loc, loc);
		double heading = pose.getTheta() - landmarkAngle;
		return new RobotPose(loc.getX(), loc.getY(), heading);
	}
	
	public SimpleActionModel getRobotAction(RobotPose globalRef, RobotPose prefRef, RobotPose nextRef, double dtime) {
//		System.out.println("prevLoc: " + prevPose);
//		System.out.println("currLoc: " + currPose);
		Point2D locOffset = new Point2D.Double(nextRef.getX(), nextRef.getY());
		
		AffineTransform.getTranslateInstance(-prefRef.getX(), -prefRef.getY()).transform(locOffset,locOffset);
		AffineTransform.getRotateInstance(globalRef.getTheta()-prefRef.getTheta()).transform(locOffset,locOffset);
		
		double headingOffset = nextRef.getTheta() - prefRef.getTheta();
		return new SimpleActionModel(locOffset.getX(), locOffset.getY(), headingOffset, dtime);
	}

}
