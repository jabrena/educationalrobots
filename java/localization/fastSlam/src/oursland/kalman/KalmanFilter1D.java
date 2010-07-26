package oursland.kalman;


public class KalmanFilter1D implements Cloneable {
//	double measuredValue = 0.0;												// z_k : measured value with error v_k
//	double prioriValue = 0.0;												// x-_k : estimate using previous state, action mode, and error
//	double posterioriValue = 0.0;											// x_k
//	double prioriErrorCovariance = 1.0;										// P-_k : from update error
//	double posterioriErrorCovariance = 1.0;									// P_k
	private double processNoiseCovariance = 0.0;	// Q : process noise covariance
	private double sensorNoiseCovariance = 0.0;		// R : sensor noise covariance
	
	private double prioriErrorCovariance;										// P-_k : from update error
	private double posterioriErrorCovariance;									// P_k
	
	private double expectedValue = 0.0;
	
	public KalmanFilter1D(double errorCovariance) {
		this( errorCovariance, errorCovariance );
	}
	
	public KalmanFilter1D(double prioriErrorCovariance, double posterioriErrorCovariance) {
		this.prioriErrorCovariance = prioriErrorCovariance;
		this.posterioriErrorCovariance = posterioriErrorCovariance;
	}
	
	public KalmanFilter1D(KalmanFilter1D copy) {
		this.prioriErrorCovariance = copy.prioriErrorCovariance;
		this.posterioriErrorCovariance = copy.posterioriErrorCovariance;
		this.processNoiseCovariance = copy.processNoiseCovariance;
		this.sensorNoiseCovariance = copy.sensorNoiseCovariance;
	}

	public void timeUpdate(double dt) {
		timeUpdate(dt, processNoiseCovariance);
	}
	
	public void timeUpdate(double dt, double processNoise) {
		prioriErrorCovariance = posterioriErrorCovariance + dt*processNoise;
	}

	public double measurementUpdate(double prioriValue, double measuredValue) {
		return measurementUpdate(prioriValue, measuredValue, sensorNoiseCovariance);
	}
	
	public double measurementUpdate(double prioriValue, double measuredValue, double sensorError) {
		double K = prioriErrorCovariance / (prioriErrorCovariance + sensorError);
		double posterioriValue = prioriValue + K*(measuredValue - prioriValue);
		posterioriErrorCovariance = (1-K)*prioriErrorCovariance;
		return posterioriValue;
	}
	
	public void measurementUpdate2(double observedValue, double sensorError) {
		this.expectedValue = this.measurementUpdate(this.expectedValue, observedValue, sensorError);
	}

	public void setProcessNoiseCovariance(double q) {
		this.processNoiseCovariance = q;
	}
	
	public void setSensorNoiseCovariance(double r) {
		this.sensorNoiseCovariance = r;
	}

	public double getProcessNoiseCovariance() {
		return this.processNoiseCovariance;
	}
	
	public double getSensorNoiseCovariance() {
		return this.sensorNoiseCovariance;
	}

	public double getPosterioriErrorCovariance() {
		return posterioriErrorCovariance;
	}

	public double getPosterioriErrorVariance() {
		return Math.sqrt(posterioriErrorCovariance);
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(getClass().getName() + ".clone() failed unexpectedly.");
		}
	}

	public void setPrioriErrorCovariance(double d) {
		prioriErrorCovariance = d;
	}

	public void setExpectedValue(double value) {
		this.expectedValue = value;
	}

	public void setExpectedValueConfidence(double error) {
		this.prioriErrorCovariance = error;
		this.posterioriErrorCovariance = error;
	}

	public double getExpectedValue() {
		return this.expectedValue;
	}

}
