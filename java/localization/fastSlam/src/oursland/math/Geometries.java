package oursland.math;

import java.awt.geom.Point2D;

public class Geometries {
	public static double getAngleCosine(Point2D p1, Point2D p2, Point2D p3) {
		final double dx1 = p1.getX() - p2.getX();
		final double dy1 = p1.getY() - p2.getY();
		final double dx2 = p3.getX() - p2.getX();
		final double dy2 = p3.getY() - p2.getY();
		final double value = dy1 * dx2 - dx1 * dy2;
		return value;
	}

	public static double getAngle(double dx, double dy) {
		double h = Math.sqrt(dx * dx + dy * dy);
		double theta;
		// use the smaller value for the more precise angle
		if(Math.abs(dx) > Math.abs(dy)) {
			theta = Math.asin(dy / h);
			if(dx < 0) {
				theta = Math.PI - theta;
			}
		} else {
			theta = Math.acos(dx / h);
			if(dy < 0) {
				theta = UsefulConstants.TWOPI - theta;
			}
		}
		if(theta > Math.PI) {
			theta -= UsefulConstants.TWOPI;
		}
		if(theta < -Math.PI) {
			theta += UsefulConstants.TWOPI;
		}
		return theta;
	}
}