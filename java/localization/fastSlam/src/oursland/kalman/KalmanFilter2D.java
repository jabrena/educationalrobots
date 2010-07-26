package oursland.kalman;

import oursland.math.Gaussian;

public class KalmanFilter2D implements GeneralKalmanFilter, Cloneable {
	private KalmanFilter1D x;
	private KalmanFilter1D y;

	public KalmanFilter2D(double[] errorCovariance) {
		this.x = new KalmanFilter1D(errorCovariance[0]);
		this.y = new KalmanFilter1D(errorCovariance[3]);
	}

	public KalmanFilter2D(double errorCovariance) {
		this.x = new KalmanFilter1D(errorCovariance);
		this.y = new KalmanFilter1D(errorCovariance);
	}

	public KalmanFilter2D(double prioriErrorCovariance, double posterioriErrorCovariance) {
		this.x = new KalmanFilter1D(prioriErrorCovariance, posterioriErrorCovariance);
		this.y = new KalmanFilter1D(prioriErrorCovariance, posterioriErrorCovariance);
	}

	public KalmanFilter2D(KalmanFilter2D copy) {
		this.x = new KalmanFilter1D(copy.x);
		this.y = new KalmanFilter1D(copy.y);
	}

	public void timeUpdate(double dt) {
		this.x.timeUpdate(dt);
		this.y.timeUpdate(dt);
	}

	public double[] measurementUpdate(double[] prioriValue, double[] measuredValue) {
		double x = this.x.measurementUpdate(prioriValue[0], measuredValue[0]);
		double y = this.y.measurementUpdate(prioriValue[1], measuredValue[1]);
		return new double[] { x, y };
	}

	public void setProcessNoiseCovariance(double q) {
		this.x.setProcessNoiseCovariance(q);
		this.y.setProcessNoiseCovariance(q);
	}

	public void setSensorNoiseCovariance(double[] r) {
		this.x.setSensorNoiseCovariance(r[0]);
		this.y.setSensorNoiseCovariance(r[3]);
	}

	public double getProcessNoiseCovariance() {
		return this.x.getProcessNoiseCovariance();
	}

	public double getSensorNoiseCovariance() {
		return this.x.getSensorNoiseCovariance();
	}

	public double[] getPosterioriErrorCovariance() {
		return new double[] { x.getPosterioriErrorCovariance(), 0, 0, y.getPosterioriErrorCovariance()};
	}

	public double[] getPosterioriErrorVariance() {
		return new double[] { x.getPosterioriErrorCovariance(), 0, 0, y.getPosterioriErrorCovariance()};
	}

	public void setProcessNoiseCovariance(double[] d) {
		this.x.setProcessNoiseCovariance(d[0]);
		this.y.setProcessNoiseCovariance(d[3]);
	}

	public Object clone() {
		try {
			KalmanFilter2D rc = (KalmanFilter2D) super.clone();
			rc.x = (KalmanFilter1D) this.x.clone();
			rc.y = (KalmanFilter1D) this.y.clone();
			return rc;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(getClass().getName() + ".clone() failed unexpectedly.");
		}
	}

	public double getProductArea(double[] priori, double[] observed, double[] sensorErrorCovariance) {
		return getProductArea(
			priori,
			observed,
			sensorErrorCovariance,
			new double[] { x.getPosterioriErrorVariance(), 0, 0, y.getPosterioriErrorVariance()});
	}

	public static double getProductArea(
		double[] priori,
		double[] observed,
		double[] sensorErrorCovariance,
		double[] posterioriErrorVariance) {
		double p1 =
			Gaussian.productArea(
				priori[0],
				observed[0],
				posterioriErrorVariance[0],
				Math.sqrt(sensorErrorCovariance[0]));
		double p2 =
			Gaussian.productArea(
				priori[1],
				observed[1],
				posterioriErrorVariance[3],
				Math.sqrt(sensorErrorCovariance[3]));
		return p1 * p2;
	}

	public GeneralKalmanFilter createCopy() {
		return new KalmanFilter2D(this);
	}
}
