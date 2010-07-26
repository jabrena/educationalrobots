package oursland.math;

import java.util.Random;

public class Gaussian {
	private final double	mean;
	private final double	variance;
	private final double stddev;
	private final double	area;
	private final double A = Math.sqrt(2*Math.PI);

	public Gaussian(double mean, double variance) {
		this.mean = mean;
		this.variance = variance;
		this.stddev = Math.sqrt(variance);
		this.area = 1.0;
	}

	public double getArea() {
		return area;
	}
	
	public double getPDF(double x) {
		double d = x-mean;
		double c = -d*d/(2*variance);
		double result = Math.exp(c)/(stddev*A);
		return result;
	}

	public double productArea(Gaussian g) {
		return productArea(g.mean, this.mean, g.variance, this.variance);
		//		double rc = 0.0;
		//		double dist = g.mean - this.mean;
		//		double var = g.variance + this.variance;
		//		double num = Math.exp(-dist*dist / 2 / var);
		//		double den = 2*Math.sqrt(Math.PI*var);
		//		return num/den;
	}

	public double sampleValue(Random rand) {
		return mean + variance * rand.nextGaussian();
	}

	public double getMean() {
		return mean;
	}

	public static double productArea(double mean1, double mean2, double var1, double var2) {
		double dist = mean1 - mean2;
		double var = var1 * var1 + var2 * var2;
		double exp = -dist * dist / (2 * var);
		double num = Math.exp(exp);
		double den = 2 * Math.sqrt(Math.PI * var);
		return num / den;
	}

	public double getStdDev() {
		return stddev;
	}
}