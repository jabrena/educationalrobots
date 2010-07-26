package vp.model;

import java.util.Random;

import vp.robot.RobotPath;

/**
 * @author oursland
 */
public interface ActionModel {
	public double getX();
	public double getY();
	public double getTheta();
	public double getElapsedTime();
	public ActionModel nextAction(RobotPath nextStep);
	public void selectActionHypothesis(Random rand);
}