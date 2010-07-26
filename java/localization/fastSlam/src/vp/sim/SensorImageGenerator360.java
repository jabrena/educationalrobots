package vp.sim;

import java.awt.geom.Rectangle2D;

public class SensorImageGenerator360 implements SensorImageGenerator {
	private final double radius;
	private final double radius2;
	
	public SensorImageGenerator360(double radius) {
		this.radius = radius;
		this.radius2 = Math.pow(radius,2);
	}
	
	public boolean isSensed(double x, double y) {
		return (x*x)+(y*y) < radius2;
	}

	public Rectangle2D getOrientationFreeBounds(double x, double y) {
		return new Rectangle2D.Double(x-radius, y-radius, 2*radius, 2*radius);
	}
}
