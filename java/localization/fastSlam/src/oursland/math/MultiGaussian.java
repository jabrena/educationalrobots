package oursland.math;

/**
 * @author oursland
 */
public class MultiGaussian {
	// G(x) = 1.0/sqrt((2*PI)^n * norm(K))*exp(-0.5*x_T*K^-1*x)
	public static double pdf(double[] x, double[][] cov, double[][] invcov) {
		double e = 0.0;
		for(int i = 0; i < cov.length; i++) {
			double colsum = getInnerProduct(x, invcov[i]);
			e += colsum * x[i];
		}
		e *= -0.5;
		double det = getDeterminant(cov);
		double pi = Math.pow(2 * Math.PI, x.length);
		double a = 1.0 / Math.sqrt(pi * det);
		return a * Math.exp(e);
	}

	public static double logPdf(double[] x, double[][] cov, double[][] invcov) {
		double e = 0.0;
		for(int i = 0; i < cov.length; i++) {
			double colsum = getInnerProduct(x, invcov[i]);
			e += colsum * x[i];
		}
		e *= -0.5;
		double det = getDeterminant(cov);
		double pi = Math.pow(2 * Math.PI, x.length);
		double a = 1.0 / Math.sqrt(pi * det);
		return Math.log(a) + e;
	}

	private static double getDeterminant(double[][] a) {
		if(a.length == 2) {
			return a[0][0] * a[1][1] - a[0][1] * a[1][0];
		} else {
			double rc = 0.0;
			for(int i = 0; i < a.length; i++) {
				double x = a[i][0];
				double y = a[i][0];
				for(int j = 1; j < a.length; j++) {
					x *= a[(i + j) % a.length][j];
					y *= a[(i - j + a.length) % a.length][j];
				}
				rc += x;
				rc -= y;
			}
			return rc;
		}
	}

	private static double getInnerProduct(double[] x, double[] col) {
		double colsum = 0;
		for(int i = 0; i < x.length; i++) {
			colsum += x[i] * col[i];
		}
		return colsum;
	}
}