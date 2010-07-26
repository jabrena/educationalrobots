package vp.mapping;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.PrintStream;
import oursland.kalman.Matrix2;
import oursland.math.Geometries;
import oursland.naming.UniqueName;
import vp.VPConstant;

public final class LandmarkObservation extends Point2D implements GenericLandmark, Cloneable {
	//	private LandmarkObservation prevAssociation;
	private Point2D position;
	private UniqueName associationName;
	
	public LandmarkObservation(Point2D position, UniqueName association) {
		this.position = position;
		this.associationName = association;
		assert (position!=null);
	}
	
	public LandmarkObservation(LandmarkObservation old) {
		this.position = old.position;
		this.associationName = old.associationName;
		assert (position!=null);
	}
	
	public String toString() {
		return (associationName==null?"":associationName.toString()) + ": " + position.toString();
	}
	
	public LandmarkObservation(double x, double y, UniqueName association) {
		this(new Point2D.Double(x,y), association);
	}
	
	public LandmarkObservation clone() {
		return new LandmarkObservation(this);
	}

	public LandmarkObservation transform(AffineTransform xform) {
		return new LandmarkObservation(xform.transform(position, null), this.associationName );
	}

	public double distance(LandmarkObservation lm) {
		return position.distance(lm.position);
	}

	public double distanceSquare(LandmarkObservation lm) {
		return position.distanceSq(lm.position);
	}

	public void paint(Graphics2D g, Color fillColor, Color borderColor) {
		AffineTransform lastXform = g.getTransform();
		double x = position.getX();
		double y = position.getY();
		x /= VPConstant.scaleDown;
		y /= VPConstant.scaleDown;
//		g.translate(position.getX(), position.getY());
		g.scale(VPConstant.scaleDown, VPConstant.scaleDown);
		g.setColor(fillColor);
		g.fillOval((int) (-5+x), (int) (-5+y),10,10);
		g.setColor(borderColor);
		g.drawOval((int) (-5+x), (int) (-5+y),10,10);
		g.setTransform(lastXform);
	}

	public void paint(Graphics2D g, Color fillColor, Color borderColor, AffineTransform xform) {
		AffineTransform lastXform = g.getTransform();
		Point2D xformPos = xform.transform(position, null);
		double x = xformPos.getX()/VPConstant.scaleDown;
		double y = xformPos.getY()/VPConstant.scaleDown;
//		g.translate(position.getX(), position.getY());
		g.scale(VPConstant.scaleDown, VPConstant.scaleDown);
		g.setColor(fillColor);
		g.fillOval((int) (-5+x), (int) (-5+y),10,10);
		g.setColor(borderColor);
		g.drawOval((int) (-5+x), (int) (-5+y),10,10);
		g.setTransform(lastXform);
	}

	public double getX() {
		return position.getX();
	}

	public double getY() {
		return position.getY();
	}

	public double getMahalanobisDistance(LandmarkObservation lm, double[] covar) {
		return Matrix2.getMahalanobisDistance2(covar, lm.getX()-getX(), lm.getY()-getY());
	}
	
	public UniqueName getAssociationName() {
		return associationName;
	}

	public void write(PrintStream out) {
		if( getAssociationName() != null ) {
			out.print(getAssociationName() + " ");
		}
		out.print(getX() + " " + getY() + " ");
	}

	public void removeDataAssociation() {
		associationName = null;
	}

	public void setLocation(double x, double y) {
		throw new UnsupportedOperationException("LandmarkObservation is immutable. setLocation not allowed.");
	}

////	private ArrayList listeners = new ArrayList();
//	public void addUpdateListener(LandmarkListener l) {
//		// position never changes
////		listeners.add(l);
//	}
//	
//	public void removeUpdateListener(LandmarkListener l) {
//		// position never changes
////		listeners.remove(l);
//	}
//	
//	public void fireLandmarkUpdate(Point2D oldPosition) {
//		// position never changes
////		LandmarkListener[] la = new LandmarkListener[listeners.size()]; 
////		listeners.toArray(la);
////		for (int i = 0; i < la.length; i++) {
////			la[i].locationChange(this, oldPosition);
////		}
//	}
	public boolean equals(Object o) {
		boolean rc = false;
		if( o instanceof LandmarkObservation ) {
			LandmarkObservation other = (LandmarkObservation)o;
			if( associationName != null ) {
				rc = associationName.equals(other.associationName);
			} else {
				rc = super.equals(o);
			}
		} else {
			rc = super.equals(o);
		}
		return rc;
	}

	public double getDistance() {
		return position.distance(0,0);
	}

	public double getAngle() {
		final double x = position.getX();
		final double y = position.getY();
		return Geometries.getAngle(x,y);
	}
}
