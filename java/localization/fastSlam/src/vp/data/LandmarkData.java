package vp.data;

import java.awt.geom.Point2D;
import java.util.Arrays;

import vp.mapping.LandmarkObservationSet;
import vp.util.MeanCollector;

public class LandmarkData {
	private TimeData time;
	private LandmarkDatum[] data;
	private int[][] association;
	
	public LandmarkData(LaserData laser) {
		time = laser.getTimeData();
		data = new LandmarkDatum[time.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = new LandmarkDatum(laser.getSample(i));
		}
		association = new int[data.length][];	
			// index 0 uses same indexes as landmarks 0
			// index 0 values refer to landmarks 1 
		calculateAssociations(29, 30);
	}

	public LandmarkObservationSet getSet(int i) {
		return data[i].getSet(time.getTS(i));
	}

	public LandmarkDatum getSample(int i) {
		return data[i];
	}
	
	public int size() {
		return data.length;
	}
	
	public void calculateAssociations(int i, int j) {
		LandmarkDatum s1 = getSample(i);
		LandmarkDatum s2 = getSample(j);
		double[] distance = new double[s1.size()];
		for (int k = 0; k < distance.length; k++) {
			Point2D p1 = s1.getLandmark(k);
			Point2D p2 = s2.getClosestLandmark(p1);
			distance[k] = p1.distance(p2);
		}
		double[] weights = new double[distance.length];
		Arrays.fill(weights, 1.0);
		MeanCollector mean = new MeanCollector(distance, weights);
		for( int ii = 0; ii < 10; ii++ ) {
			mean.calculate();
			mean.updateWeights();
		}
	}
}
