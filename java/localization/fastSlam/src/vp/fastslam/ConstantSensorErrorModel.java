package vp.fastslam;

import vp.mapping.GenericLandmark;
import vp.robot.RobotPose;

/**
 * @author oursland
 */
public class ConstantSensorErrorModel implements SensorErrorModel {
	private double[] error;
	
	public ConstantSensorErrorModel(double[] error) {
		this.error = error;
	}

	public double[] getSensorError(RobotPose pose, GenericLandmark lm, double[] sensorError) {
		System.arraycopy(this.error, 0, sensorError, 0, 4);
		return sensorError;
	}
}
