package vp.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.Random;

import oursland.RandomSingleton;
import vp.VPConstant;
import vp.model.ActionModel;
import vp.model.SimpleActionModel;

public final class RobotPose extends Point2D implements Cloneable {
	private static final Shape s1 = new Line2D.Double( 0, -5,  0, 8);
	private static final Shape s2 = new Line2D.Double(-5,  0, 5, 0);
	
	public double x;
	public double y;
	public double theta;
	
	public Object clone() {
		return new RobotPose(this);
	}
	public RobotPose(RobotPose copy) {
		this(copy.getX(), copy.getY(), copy.getTheta());
	}
	
	private static final double PI2 = 2*Math.PI;
	public RobotPose(double x, double y, double theta) {
		this.x = x;
		this.y = y;
		while(theta > Math.PI) {
			theta -= PI2;
		}
		while(theta < -Math.PI) {
			theta += PI2;
		}
		this.theta = theta;
	}
	
	public RobotPose(RobotPose pose, ActionModel action) {
		this.x = pose.x + action.getX();
		this.y = pose.y + getY();
		this.theta = pose.theta + getTheta();
	}

	public AffineTransform getTransform() {
		AffineTransform xform = new AffineTransform();
		xform.translate(x, y);
//		xform.rotate(theta);
		return xform;
	}
	
	public void paint(Graphics2D g, double scale) {
		AffineTransform xform = getTransform();
		
		AffineTransform lastXform = g.getTransform();
		g.transform(xform);
//		g.translate(x, y);
//		g.rotate(theta);
		g.setColor(color);
		double c = Math.cos(theta);
		double s = Math.sin(theta);
		Shape s1 = new Line2D.Double( -5*c, -5*s, +8*c, +8*s);
		Shape s2 = new Line2D.Double( -5*s, +5*c, +5*s, -5*c);
		g.scale(scale, scale);
		g.draw(s1);
		g.draw(s2);
//		System.out.println("Drawing robot at " + theta);
		g.setTransform(lastXform);
	}

	public void paintTiny(Graphics2D g) {
		AffineTransform xform = getTransform();
		
		AffineTransform lastXform = g.getTransform();
		g.transform(xform);
		
		g.setColor(color);
		double c = Math.cos(theta);
		double s = Math.sin(theta);
		Shape s1 = new Line2D.Double( -5*c, +5*s, +8*c, -8*s);
		Shape s2 = new Line2D.Double( -5*s, -5*c, +5*s, +5*c);
		g.scale(VPConstant.scaleDown/2, VPConstant.scaleDown/2);
		g.draw(s1);
		g.draw(s2);
		
		g.setTransform(lastXform);
	}

	public Point2D getLocation() {
		return new Point2D.Double(x,y);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getTheta() {
		return theta;
	}
	
	public SimpleActionModel getActionFrom(RobotPose previous, double dt) {
		// Robot actions are in the coordinate space of the previous pose
		// +x is the front of the robot
		// +y is the left of the robot
		double da = this.theta - previous.theta;
		while(da > Math.PI ) {
			da -= 2*Math.PI;
		}
		while(da < -Math.PI ) {
			da += 2*Math.PI;
		}
		final double c = Math.cos(previous.theta);
		final double s = Math.sin(previous.theta);
		final double dx = this.x - previous.x;
		final double dy = this.y - previous.y;
		// action is in global coordinate system
		return new SimpleActionModel( c*dx+s*dy, s*dx-c*dy, da, dt );
	}


	private static final NumberFormat nf = NumberFormat.getInstance();
	static {
		nf.setGroupingUsed(false);
//		nf.setMinimumIntegerDigits(5);
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
	}
	public String toString() {
		return "(" + nf.format(x) + ", " + nf.format(y) + ", " + theta + ")";
	}

	public boolean isValid() {
//		Double.isNaN(x)
		return !java.lang.Double.isNaN(x) && !java.lang.Double.isNaN(y) && !java.lang.Double.isNaN(theta);
	}

	public RobotPose getNoisyPose(double varx, double vary, double vartheta) {
		Random r = RandomSingleton.instance;
		return new RobotPose(x+varx*r.nextGaussian(), y+vary*r.nextGaussian(), theta+vartheta*r.nextGaussian());
	}
	public void setLocation(double x, double y) {
		throw new UnsupportedOperationException("RobotPose is immutable. setLocation not allowed.");
	}
	
	private Color color = Color.BLACK;
	public void setColor(Color color) {
		this.color = color;
	}
}
