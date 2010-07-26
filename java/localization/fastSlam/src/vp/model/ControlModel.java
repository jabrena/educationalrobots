package vp.model;

import java.util.Random;

import vp.robot.RobotPose;

/**
 * @author oursland
 */
public interface ControlModel {
	public RobotPose nextPose(Random rand, RobotPose pose, ActionModel action);
//	public RobotPose nextPose(RobotPose pose, RobotActionModel action, double f1, double f2, double f3);
}