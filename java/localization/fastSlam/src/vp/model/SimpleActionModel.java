package vp.model;

import java.text.NumberFormat;
import java.util.Random;

import vp.robot.RobotPath;

public class SimpleActionModel implements ActionModel {
	private final double xVel;
	private final double yVel;
	private final double thetaVel;
	private final double dtime; //  = 1.0;
	
	public SimpleActionModel(double xVel, double yVel, double thetaVel, double dtime) {
		while( thetaVel/dtime > Math.PI ) {
			thetaVel -= Math.PI*dtime;
		}
		while( thetaVel/dtime < -Math.PI ) {
			thetaVel += Math.PI*dtime;
		}
		this.xVel = xVel;
		this.yVel = yVel;
		this.thetaVel = thetaVel;
		this.dtime = dtime;
	}
	
	private static final NumberFormat nf = NumberFormat.getInstance();
	static {
		nf.setGroupingUsed(false);
//		nf.setMinimumIntegerDigits(5);
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
	}
	public String toString() {
		return 
			"action(" + 
			nf.format(xVel/dtime) + ", " + 
			nf.format(yVel/dtime) + ", " + 
			nf.format(thetaVel/dtime) + 
			")";
	}

	public double getX() {
		return xVel;
	}

	public double getY() {
		return yVel;
	}

	public double getTheta() {
		return thetaVel;
	}

	public double getElapsedTime() {
		return dtime;
	}

	public void selectActionHypothesis(Random rand) {
	}

	public ActionModel nextAction(RobotPath nextStep) {
		SimpleActionModel action = nextStep.getPreviousAction();
		return action;
	}

	public ActionModel average(ActionModel action) {
		double dx = (xVel+action.getX())/2;
		double dy = (yVel+action.getY())/2;
		double dtheta = (thetaVel+action.getTheta())/2;
		return new SimpleActionModel(dx, dy, dtheta, dtime);
	}
}
