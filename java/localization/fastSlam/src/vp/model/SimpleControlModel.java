package vp.model;

import java.util.Random;

import vp.robot.RobotPose;

public class SimpleControlModel implements ControlModel {
	
//	private double varx = 0;//150; //30.0;
//	private double vary = 0;//400; //80.0;
//	private double vartheta = 0;//1.0; // 0.2;
//	private double varx = 150; //30.0;
//	private double vary = 400; //80.0;
//	private double vartheta = 1.0; // 0.2;
	private double varx = 20;
	private double vary = 60;
	private double vartheta = 1.0;
	
	public SimpleControlModel() {
	}
	
	public SimpleControlModel(double varx, double vary, double vartheta) {
		this.varx = varx;
		this.vary = vary;
		this.vartheta = vartheta;
	}
	
	public RobotPose nextPose(Random rand, RobotPose pose, ActionModel action) {
		return nextPose(pose, action, rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
	}

//	public RobotPose nextPose(Random rand, RobotPose pose, ActionModel action) {
//		return nextPose(pose, action, 2*rand.nextDouble()-1, 2*rand.nextDouble()-1, 2*rand.nextDouble()-1);
//	}

	public RobotPose nextPose(RobotPose pose, ActionModel action, double f1, double f2, double f3) {
		double dtime = action.getElapsedTime();
		double dx = dtime*varx*f1 + action.getX();
		double dy = dtime*vary*f2 + action.getY();
		double dtheta = dtime*vartheta*f3;
		
		double s = Math.sin(pose.theta);
		double c = Math.cos(pose.theta);
		
//		System.out.println(dtime);
		
		return new RobotPose(dx*c+dy*s+pose.x, dx*s+dy*c+pose.y, dtheta+pose.theta+action.getTheta());
	}
}
