package vp.fastslam;

import vp.mapping.GenericLandmark;
import vp.robot.RobotPose;

/**
 * @author oursland
 */
public class DistanceSensorErrorModel implements SensorErrorModel {
	private double[] minError;
	private double scale;
	
	public DistanceSensorErrorModel(double[] minError, double scale) {
		this.minError = minError;
		this.scale = scale;
	}

	public double[] getSensorError(RobotPose pose, GenericLandmark lm, double[] sensorError) {
		System.arraycopy(this.minError, 0, sensorError, 0, 4);
		double d = pose.distance(lm.getX(), lm.getY());
		double e = d*scale;
		sensorError[0] += e;
		sensorError[3] += e;
		return sensorError;
	}
}
