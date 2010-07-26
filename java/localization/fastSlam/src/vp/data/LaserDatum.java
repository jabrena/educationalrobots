package vp.data;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class LaserDatum {
	private final ArrayList<Integer> list = new ArrayList<Integer>();
	
	private LaserDatum() {
	}
	
	public LaserDatum( String data ) {
		StringTokenizer st = new StringTokenizer(data);
		while( st.hasMoreTokens() ) {
			list.add(new Integer(st.nextToken()));
		}
//		System.out.println("Read " + list.size() + " laser values.");
	}
	
	public static double getRadians( int k ) {
		return Math.PI*(k-180.0)/360.0;
	}
	
	public int getMaxCutoff() {
		return 8000;
	}

	public String getUnit() {
		return "cm";
	}
	
	public String getCarmenFormat(NumberFormat nf ) {
		StringBuffer rc = new StringBuffer();
		for( int i = 0; i < list.size(); i++ ) {
			rc.append(getCarmenFormat(i, nf));
		}
		return rc.toString();
	}

	private String getCarmenFormat( int k, NumberFormat nf ) {
		if( list.get(k).intValue() > 100 ) {
			Point2D p = getReadingLocation(k);
			return "0 0 " + nf.format(p.getX()/100.0) + " " + nf.format(p.getY()/100.0) + " ";
		} else {
			double r = getRadians(k);
			double sin = Math.sin(r);
			double cos = Math.cos(r);
			Point2D p = getReadingLocation(k);
			return "0 0 " + nf.format(90*sin) + " " + nf.format(90*cos) + " ";
		}
	}

	public int size() {
		return list.size();
	}

	public Point2D getReadingLocation(int k) {
		Integer D = list.get(k);
		int d = D.intValue();
//		if( d > getMaxCutoff() ) {
//			d = 0;
//		}
		double r = getRadians(k);
		double sin = Math.sin(r);
		double cos = Math.cos(r);
		return new Point2D.Double(d*sin, d*cos);
	}
	
	public Point2D getTransformedLocation( int k, double theta, double xOffset, double yOffset ) {
		LaserDatum rc = new LaserDatum();
		AffineTransform xform = new AffineTransform();
		xform.rotate(-theta);
		xform.translate(-xOffset, -yOffset);
		Point2D p = getReadingLocation(k);
		return xform.transform(p, null);
	}
	
	public double getError( LaserDatum sample, double theta, double xOffset, double yOffset ) {
		double rc = 0.0;
		for( int i = 0; i < sample.size(); i++ ) {
			double minDist = Double.MAX_VALUE;
			Point2D p1 = sample.getTransformedLocation(i, theta, xOffset, yOffset );
			int start = Math.max(0, i-20);
			int end = Math.min(size(), i+20);
			for( int j = start; j < end; j++ ) {
				Point2D p2 = this.getReadingLocation(j);
				minDist = Math.min(minDist, p1.distance(p2));
			}
			rc += minDist;
		}
		return rc;
	}

	public int getReading(int i) {
		return (list.get(i)).intValue();
	}
}
