package oursland.math;

import oursland.RandomSingleton;

public class DiscreteDistribution {
	private final double[]	dist;

	private double			sum		= 0;

//	private double			entropy	= -1;

	public DiscreteDistribution(int size) {
		dist = new double[size];
	}

	public DiscreteDistribution(double[] dist) {
		this.dist = dist.clone();
		sum = getSum(this.dist);
	}

	public static double getSum(double[] dist) {
		double sum = 0.0;
		for(int i = 0; i < dist.length; i++) {
			sum += dist[i];
		}
		return sum;
	}

	public int sample() {
		normalize();
		return sample(dist);
	}

	public void normalize() {
		if(sum != 1.0) {
			normalize(dist, sum);
			sum = 1.0;
		}
	}

	public static void normalize(double[] dist) {
		normalize(dist, getSum(dist));
	}

	public static void normalize(double[] dist, double sum) {
		if(sum == 0) {
			// treat all particles equally
			sum = 1.0;
			for(int i = 0; i < dist.length; i++) {
				dist[i] /= 1.0 / dist.length;
			}
		} else {
			for(int i = 0; i < dist.length; i++) {
				dist[i] /= sum;
			}
		}
	}

	public static int sample(double[] dist) {
		final double r = RandomSingleton.instance.nextDouble();
		double acc = 0.0;
		for(int i = 0; i < dist.length; i++) {
			acc += dist[i];
			if(r <= acc) {
				return i;
			}
		}
		return dist.length - 1;
	}

	public static double getEntropy(double[] dist) {
		double rc = 0.0;
		for(int i = 0; i < dist.length; i++) {
			rc += dist[i] * Math.log(dist[i]) / UsefulConstants.NLOG2;
		}
		return rc;
	}
}