package vp.data;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import vp.mapping.LandmarkObservation;
import vp.mapping.LandmarkObservationSet;

public class LandmarkDatum {
	private Point2D[] data;
	
	public LandmarkDatum(LaserDatum ls) {
		ArrayList<Point2D> list = new ArrayList<Point2D>();
		int max = ls.getMaxCutoff();
		int min = 300;
		boolean ascending = false;	// assume we start off descending from max range
		int lastLimit = ls.getReading(0);
		int lastIndex = 0;
		for( int i = 1; i < ls.size(); i++ ) {
			int reading = ls.getReading(i);
			if( ascending ) {
				if( reading < 0.9*lastLimit ) {
					// treat lastLimit as a landmark
					addFilteredLandmark(lastIndex, ls, list, max, min);
					ascending = false;
					i = lastIndex;
//					System.out.println(lastIndex + "\tLANDMARK\t" + lastLimit);
				} else {
					lastLimit = reading;
					lastIndex = i;
//					System.out.println(i + "\tASCENDING\t" + reading);
				}
			} else {
				if( reading > 1.1*lastLimit ) {
					// treat lastLimit as a landmark.
					addFilteredLandmark(lastIndex, ls, list, max, min);
					ascending = true;
					i = lastIndex;
//					System.out.println(lastIndex + "\tLANDMARK\t" + lastLimit);
				} else if (reading < lastLimit ) {
					lastLimit = reading;
					lastIndex = i;
//					System.out.println(i + "\tDESCENDING\t" + reading);
				}
			}
		}
		data = new Point2D[list.size()];
		list.toArray(data);
	}
	
	public int size() {
		return data.length;
	}
	
	public Point2D getLandmark(int i) {
		return data[i];
	}
	
	public Point2D[] getData() {
		return data;
	}
	
	private static void addFilteredLandmark(int i, LaserDatum ls, ArrayList<Point2D> list, int max, int min) {
		int reading = ls.getReading(i);
		if( reading < max && reading > min ) {
			Point2D p = ls.getReadingLocation(i);
			list.add(p);
		}		
	}

	public Point2D getClosestLandmark(Point2D p) {
		Point2D rc = null;
		double d = Double.MAX_VALUE;
		for (int i = 0; i < data.length; i++) {
			double dprime = data[i].distanceSq(p);
			if( d > dprime ) {
				d = dprime;
				rc = data[i];
			}
		}
		return rc;
	}

	public LandmarkObservationSet getSet(double time) {
		LandmarkObservation[] set = new LandmarkObservation[size()];
		for (int i = 0; i < set.length; i++) {
			set[i]  = new LandmarkObservation(data[i], null);
		}
		return new LandmarkObservationSet(set, time);
	}
}
