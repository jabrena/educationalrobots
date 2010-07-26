package oursland.kalman;
import java.util.Random;

import oursland.RandomSingleton;

public class KalmanProcessModel {
	
	protected static final Random rand = RandomSingleton.instance;
	protected double processValue = 5.3775;
	protected double processNoise = 0.00001;	// Q
	
	public void setValue(double value) {
		this.processValue = value;
	}
		
	public double getValue() {
		return processValue;
	}
	
	// gets the a priori estimate of the process
	public double getValueEstimate( KalmanSensorModel sensorModel ) {
		// z_k = H*x_k + v_k
		double rc = processValue + sensorModel.getSensorNoise();
		return rc;
	}
	
	public double update(KalmanActionModel action) {
		// computes x_(k+1) = x_k + B*u_k + noise
		double dx = action.getDelta() + processNoise*rand.nextGaussian();
		processValue += dx;
		return processValue;
	}

	public void setProcessNoise(double d) {
		processNoise = d;
	}

	public double getProcessNoise() {
		return processNoise;
	}

}
