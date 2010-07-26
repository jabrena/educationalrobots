package vp.data;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.NumberFormat;

import vp.mapping.Offset;

public class GpsDatum {
	public double x;
	public double y;
	public double theta;

	public GpsDatum(GpsDatum s1, GpsDatum s2, double ratio) {
		this.x = s1.x + (s2.x - s1.x)*ratio;
		this.y = s1.y + (s2.y - s1.y)*ratio;
		this.theta = s1.theta + (s2.theta - s1.theta)*ratio;
//		System.out.println(""+this.x +" "+this.y+" "+this.theta);
	}

	public GpsDatum(double x, double y, double theta ) {
		this.x = x;
		this.y = y;
		this.theta = theta;
	}
	
	public GpsDatum(String x, String y, GpsDatum lastSample) {
		this.x = Double.parseDouble(x);
		this.y = Double.parseDouble(y);
		
		if( lastSample != null ) {
			double dx = this.x - lastSample.x;
			double dy = this.y - lastSample.y;
			double h = Math.sqrt(dx*dx + dy*dy);
			this.theta = Math.asin(dx/h);
			this.theta = normalizeAngle(this.theta);
/*
			System.out.println(
				"From (" +
				lastSample.x +
				", " +
				lastSample.y +
				") to (" +
				this.x +
				", " +
				this.y +
				") is a change of (" +
				dx +
				", " +
				dy +
				") with distance " +
				h +
				"and " +
				theta +
				" radians."
			);
*/
		}
	}

	public GpsDatum( GpsDatum prev, Offset offset ) {
		this.theta = prev.theta+offset.t;
		double s = Math.sin(offset.t);
		double c = Math.cos(offset.t);
		this.x = prev.x + c*offset.y + s*offset.x;
		this.y = prev.y + c*offset.x + s*offset.y;
	}

	public Offset getOffset(GpsDatum prev) {
		Offset rc = new Offset();
		rc.t = this.theta - prev.theta;

		double dx = this.x - prev.x;
		double dy = this.y - prev.y;
		Point2D.Double p = new Point2D.Double(dx, dy);

		AffineTransform xform = new AffineTransform();
		xform.rotate(prev.theta);

		double h = Math.sqrt(dx*dx + dy*dy);
		rc.t = Math.acos(dx/h);
		rc.x = p.getX();
		rc.y = p.getY();

		return rc;
	}
	
	public String getCarmenFormat(NumberFormat nf) {
		return nf.format(x) + " " + nf.format(y) + " " + nf.format(theta);
	}

	private static final double PI2 = Math.PI/2;

	public static double normalizeAngle( double r ) {
		while( r > PI2 ) {
			r -= Math.PI;
		}
		while( r < -PI2 ) {
			r += Math.PI;
		}
		return r;
	}
}
