package vp.util;
public final class Statistics {

	private static final double sqrt2 = Math.sqrt(2);
	private static final double twoOverSqrtPI = 2 / Math.sqrt(Math.PI);
	private static final double[] confidenceCalibration =
		{ 0.0, 0.6826894915501904, 0.9544997355357003, 0.997300203348207, 0.9999366569321159, 0.9999994261127411, };
	public static final double getConfidence(double offset, double covariance) {
		double sigma = Math.sqrt(covariance);
		double n = offset / sigma;
		int lowCalibrate = (int) Math.floor(n);
		lowCalibrate = Math.min(lowCalibrate, confidenceCalibration.length - 1);
		return confidenceCalibration[lowCalibrate] + estimateErf(lowCalibrate / sqrt2, n / sqrt2, covariance);
	}
	private static final double step = 0.0001;
	private static final double estimateErf(double start, final double end, final double covariance) {
		double rc = 0.0;
		double prev = derf(start, covariance);
		while (start + step < end) {
			start += step;
			double current = derf(start, covariance);
			rc += step * (current + prev) / 2;
			prev = current;
		}
		double current = derf(end, covariance);
		double lastStep = end - start;
		if (lastStep > 0) {
			rc += lastStep * (current + prev) / 2;
		}
		return twoOverSqrtPI * rc;
	}
	// this should be integrated from 0 to n/sqrt(2) and divided by 2/sqrtPI
	private static final double derf(double u, double covariance) {
		//		double u2 = Math.pow(u, 2) / (2*covariance);
		return Math.exp(-Math.pow(u, 2));
	}

	public static void main(String[] args) {
		double[] x = { 0.0, 0.1, 0.25, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 6.0 };
		for (int i = 0; i < x.length; i++) {
			System.out.println("erf(" + x[i] + ") = " + Statistics.getConfidence(x[i], 1.0));
		}
	}
}
