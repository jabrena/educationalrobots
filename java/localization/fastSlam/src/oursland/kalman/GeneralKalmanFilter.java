package oursland.kalman;

public interface GeneralKalmanFilter extends Cloneable {
	public abstract void timeUpdate(double dt);
	public abstract double[] measurementUpdate(double[] prioriValue, double[] measuredValue);
	public abstract void setProcessNoiseCovariance(double[] q);
	public abstract void setSensorNoiseCovariance(double[] r);
//	public abstract void setPrioriErrorCovariance(double[] prioriErrorCovariance);
	public abstract double[] getPosterioriErrorCovariance();
//	public abstract double[] getPosterioriErrorVariance();
//	public abstract double[] getProcessNoiseCovariance();
//	public abstract double[] getSensorNoiseCovariance();
//	public abstract double[] getInverseErrorCovariance();
//	public abstract double[] localToGlobal(double[] x, double[] mean);
//	public abstract double getPdfValue(double[] x, double[] mean);
	public abstract Object clone();
	public abstract double getProductArea(double[] priori, double[] observed, double[] sensorErrorCovariance);
	public abstract GeneralKalmanFilter createCopy();
}