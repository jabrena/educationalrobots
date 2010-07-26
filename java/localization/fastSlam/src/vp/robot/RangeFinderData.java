package vp.robot;

import java.util.Arrays;

public class RangeFinderData {
	final int samples;
	final double minAngle;
	final double maxAngle;
	final double defaultValue;
	final double[] values;
	
	public RangeFinderData(int samples, double minAngle, double maxAngle, double defaultValue) {
		this.samples = samples;
		this.minAngle = minAngle;
		this.maxAngle = maxAngle;
		this.defaultValue = defaultValue;
		this.values = new double[samples];
		Arrays.fill(values, defaultValue);
	}
	
	public int getSampleCount() {
		return samples;
	}
	
	public double getSampleValue(int index) {
		return values[index];
	}
	
	public void addLandmarkReading(double angle, double distance, double var) {
		int minIndex = getAngleIndex(angle-10*var);
		int maxIndex = getAngleIndex(angle+10*var);
		final double covar = var*var;
		final double A = defaultValue - distance;
		for( int i = minIndex; i <= maxIndex; i++ ) {
			double tempAngle = getIndexAngle(i);
			values[i] = Math.min( 
				values[i],
				defaultValue - A*Math.exp( -Math.pow(angle-tempAngle, 2)/covar )
			);
		}
	}
	
	public double getError(RangeFinderData data) {
		double rc = 0.0;
		for (int i = 0; i < values.length; i++) {
			rc += Math.abs(this.values[i] - data.values[i]);
		}
		return rc;
	}

	private double getIndexAngle(int i) {
		return (i*(maxAngle - minAngle))/values.length ;
	}

	private int getAngleIndex(double angle) {
		angle = Math.max(angle, minAngle);
		angle = Math.min(angle, maxAngle);
		double ratio = (angle - minAngle)/(maxAngle - minAngle);
		return (int) (ratio*(values.length-1));
	}
	
}
