package vp.util;

public class MeanCollector {
	private double[] values;
	private double[] weights;
	private double mean;
	private double stddev;
	
	public MeanCollector( double[] values, double[] weights ) {
		this.values = values;
		this.weights = weights;
	}
	
	double[] membershipFn = { 0.0, 0.6826895, 0.9544997, 0.9973002, 0.9999366, 0.9999994, 1.0 };
	
	public double getMembership(double value) {
		double num_stddev = Math.abs(mean-value)/stddev;
		int lower = (int) Math.min(membershipFn.length-1, Math.floor(num_stddev));
		int upper = (int) Math.min(membershipFn.length-1, Math.ceil(num_stddev));
		
		double min = membershipFn[lower];
		double dif = (num_stddev - lower)*(membershipFn[upper] - membershipFn[lower]);
		return 1.0 - (min+dif);
	}
	
	public void updateWeights() {
		for (int i = 0; i < values.length; i++) {
			weights[i] = getMembership(values[i]);
		}
	}
	
	public void calculate() {
		double sum = 0.0;
		double sum2 = 0.0;
		double total = 0.0;
		for (int i = 0; i < values.length; i++) {
			sum += weights[i]*values[i];
			sum2 += weights[i]*Math.pow(values[i], 2);
			total += weights[i];
		}
		mean = sum/total;
		double moment2 = sum2/total;
		stddev = Math.sqrt(moment2 - Math.pow(mean,2));
	}
}
