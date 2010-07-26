package oursland.kalman;
import java.util.Random;

import oursland.RandomSingleton;

public class KalmanSensorModel {
	private static final Random rand = RandomSingleton.instance;
	private double sensorNoise = 0.1;	// white noise error
	
	public double getSensorNoise() {
		return sensorNoise*rand.nextGaussian();
	}

//	public double getSensorErrorCovariance() {
//		return Math.pow(sensorNoise, 2);
//	}

	public void setSensorCovariance(double d) {
		sensorNoise = Math.sqrt(d);
	}

	public double getSensorCovariance() {
		return Math.pow(sensorNoise, 2);
	}

}
