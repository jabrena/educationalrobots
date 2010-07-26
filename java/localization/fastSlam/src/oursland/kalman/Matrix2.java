package oursland.kalman;

public class Matrix2 {
	public static final double[] identity2_2 = {1,0,0,1};
	public static double[] transpose2_2(double[] a, double[] dst) {
		assert(a!=dst);
		dst[0] = a[0];
		dst[1] = a[2];
		dst[2] = a[1];
		dst[3] = a[3];
		return dst;
	}
	public static double[] subtract2_2(double[] a, double[] b, double[] dst) {
		dst[0] = a[0] - b[0];
		dst[1] = a[1] - b[1];
		dst[2] = a[2] - b[2];
		dst[3] = a[3] - b[3];
		return dst;
	}
	public static double[] scale2_2( double scalar, double[] a, double[] dst) {
		dst[0] = a[0] * scalar;
		dst[1] = a[1] * scalar;
		return dst;
	}
	public static double[] inverse2_2(double[] a, double[] dst) {
		assert(a!=dst);
		final double det = a[0]*a[3]-a[1]*a[2];
		dst[0] = a[3]/det;
		dst[1] = -a[1]/det;
		dst[2] = -a[2]/det;
		dst[3] = a[0]/det;
		return dst;
	}
	public static double[] product2_2(double[] a, double[] b, double[] dst) {
		assert(a!=dst);
		assert(b!=dst);
		dst[0] = a[0]*b[0] + a[1]*b[2];
		dst[1] = a[0]*b[1] + a[1]*b[3];
		dst[2] = a[2]*b[0] + a[3]*b[2];
		dst[3] = a[2]*b[1] + a[3]*b[3];
		return dst;
	}
	public static double[] add2_2(double[] a, double[] b, double[] dst) {
		dst[0] = a[0] + b[0];
		dst[1] = a[1] + b[1];
		dst[2] = a[2] + b[2];
		dst[3] = a[3] + b[3];
		return dst;
	}

	/**
	 * From Besset, Didier. Object-Oriented Implementations of Numberical Methods. Morgan Kaufman. 2001. p617
	 * Parameters describe the landmark who distance from the Gaussian will be returned.
	 * @return The distance of the landmark from the Kalman filter Gaussian.
	 */
	public static double getMahalanobisDistance2(double[] covar, double dx, double dy ) {
		double[] V = Matrix2.inverse2_2(covar, new double[4]);
		double product1 = dx*V[0]  + dy*V[2];
		double product2 = dx*V[1]  + dy*V[3];
		return product1*dx + product2*dy;
	}

	public static double determinant2_2(double[] a) {
		return a[0]*a[3] - a[1]*a[2];
	}

}
