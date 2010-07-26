package oursland.math;

/**
 * Constants that I find useful.
 */
public final class UsefulConstants {
	/**
	 * The natural log of two. Useful for binary power calculations. Log_n(k) =
	 * Ln(k)/Ln(n).
	 */
	public static final double	NLOG2		= Math.log(2);

	/**
	 * Two PI. 360 degrees. One full circle.
	 */
	public static final double	TWOPI		= 2 * Math.PI;

	/**
	 * PI divided by two. 90 degrees. One quarter circle.
	 */
	public static final double	PIOVERTWO	= Math.PI / 2;

	private UsefulConstants() {
	}
}