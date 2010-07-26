package oursland.kalman;

public class CovarianceEstimator {
	private final int evidenceCount;
	private int currentCount = 1;
	private double sum = 0.0;
	private double sumOfSquares = 0.0;
	
	public CovarianceEstimator(int evidenceCount, double initialMean, double initialVariance) {
		this.evidenceCount = evidenceCount;
		this.currentCount = evidenceCount;
		this.sum = evidenceCount*initialMean;
		this.sumOfSquares = evidenceCount*(initialVariance + Math.pow(initialMean,2));
	}
	
	public void addEvidence(double value) {
		if( currentCount >= evidenceCount ) {
			sum -= sum/currentCount;
			sumOfSquares -= sumOfSquares/currentCount;
		} else {
			currentCount++;
		}
		sum += value;
		sumOfSquares += Math.pow(value,2);
	}
	
	public double getMean() {
		return sum/currentCount;
	}
	
	public double getVariance() {
		return sumOfSquares/currentCount - Math.pow(getMean(),2);
	}
}
