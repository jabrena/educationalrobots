package vp.data;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;

import vp.mapping.Offset;

public class GpsData {
	private TimeData time;
	private ArrayList<GpsDatum> data = new ArrayList<GpsDatum>();

	public GpsData(
		TimeData timeGps,
		BufferedReader latIn,
		BufferedReader lonIn)
		throws IOException {
		time = timeGps;
//		data = new GpsSample[timeGps.size()];
		int i = 0;
		GpsDatum lastGps = null;
		for( int j = 0; j < timeGps.size(); j++ ) {
			String lat = latIn.readLine();
			String lon = lonIn.readLine();
			data.add(new GpsDatum( lat, lon, lastGps ));
			lastGps = data.get(i);
			i++;
		}
	}
	
	public GpsData(double time) {
		this.time = new TimeData();
		this.time.add(time);
		this.data.add(new GpsDatum(0,0,0));
	}
	
	public void addNext( double time, Offset offset ) {
		this.time.add(time);
		GpsDatum prev = data.get(data.size()-1);
		this.data.add(new GpsDatum(prev, offset));
	}

	public int size() {
		return data.size()-1;
	}

	public GpsDatum getSample(int i) {
		return data.get(i+1);
	}

	public double getTS(int t) {
		return time.getTS(t+1);
	}

	public String getCarmenFormat(int i, NumberFormat nf ) {
		return nf.format(getTS(i)) + " " + getSample(i).getCarmenFormat(nf);
	}

	public double getMaxSpeed() {
		double rc = 0;
		GpsDatum last = data.get(0);
		for( int i = 1; i < data.size(); i++ ) {
			GpsDatum next = data.get(i);
			double dx = next.x - last.x;
			double dy = next.y - last.y;
			double dt = time.getTS(i) - time.getTS(i-1);
			rc = Math.max( rc, Math.sqrt(dx*dx + dy*dy)/dt );
		}
		return rc;
	}

	public double getMaxTurn() {
		double rc = 0;
		GpsDatum last = data.get(0);
		for( int i = 1; i < data.size(); i++ ) {
			GpsDatum next = data.get(i);
			double dr = next.theta - last.theta;
			dr = GpsDatum.normalizeAngle(dr);
			double dt = time.getTS(i) - time.getTS(i-1);
			rc = Math.max( rc, dr/dt );
		}
		return rc;
	}

	public GpsDatum estimateSample( int t ) {
		int i = this.time.getTimeIndex(t);
	//	System.out.println("Estimate index " + i + " for time " + t);
		if( i < data.size() ) {
			GpsDatum s1 = data.get(i);
			if( i-1 < data.size() ) {
				GpsDatum s2 = data.get(i-1);
//				System.out.println("Estimating gps @ " + t + " between " + this.time.getTS(i) + " " + this.time.getTS(i-1) );
				double dt1 = t/1000.0-time.getTS(i-1);
				double dt2 = time.getTS(i)-time.getTS(i-1);
//				System.out.println("" + dt1 + " " + dt2 + " " + dt1/dt2 );
				return new GpsDatum(s2, s1, dt1/dt2);
			} else {
				return s1;
			}
		} else {
			return data.get(data.size()-1);
		}
	}

	public int getTimeIndex(int time) {
		return this.time.getTimeIndex(time);
	}
}
