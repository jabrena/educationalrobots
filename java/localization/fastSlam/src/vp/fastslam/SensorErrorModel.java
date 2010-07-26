package vp.fastslam;

import vp.mapping.GenericLandmark;
import vp.robot.RobotPose;

/**
 * @author oursland
 */
public interface SensorErrorModel {

	public double[] getSensorError(RobotPose pose, GenericLandmark lm, double[] sensorError);
}
