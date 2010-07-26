package oursland.kalman;
public class KalmanProcess extends KalmanFilter1D {

	private KalmanProcessModel posterioriModel;
	private double measuredValue;
	private double prioriValue;

	public KalmanProcess(KalmanProcessModel process, double prioriErrorCovariance, double posterioriErrorCovariance) {
		super(prioriErrorCovariance, posterioriErrorCovariance);
		this.posterioriModel = process;
		setProcessNoiseCovariance(Math.pow(posterioriModel.getProcessNoise(), 2));
		setSensorNoiseCovariance(0.0);
	}

	public double measurementUpdate(
		double prioriValue,
		double measuredValue) {
		this.measuredValue = measuredValue;
		this.prioriValue = prioriValue;
		return updatePosterioriValue();
	}
	
	private double updatePosterioriValue() {
		setProcessNoiseCovariance(Math.pow(posterioriModel.getProcessNoise(), 2));
		double posterioriValue = super.measurementUpdate(prioriValue, measuredValue);
		posterioriModel.setValue(posterioriValue);
		return posterioriValue; 
	}
	
	public double getMeasuredValue() {
		return measuredValue;
	}

	public double getPrioriValue() {
		return prioriValue;
	}

	public double getPosterioriValue() {
		return posterioriModel.getValue();
	}

	public void update(KalmanActionModel action, double measured, double sensorCovariance, double dtime) {
		setSensorNoiseCovariance(sensorCovariance);
		timeUpdate(dtime);
		measurementUpdate( posterioriModel.update(action), measured);
	}

	public KalmanProcessModel getProcess() {
		return posterioriModel;
	}
}
