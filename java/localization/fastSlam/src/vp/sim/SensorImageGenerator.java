package vp.sim;

import java.awt.geom.Rectangle2D;

public interface SensorImageGenerator {
	public abstract boolean isSensed(double x, double y);
	public abstract Rectangle2D getOrientationFreeBounds(double x, double y);
}