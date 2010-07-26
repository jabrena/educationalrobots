package vp.data;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.NumberFormat;

import vp.robot.RobotPath;

public class LaserData {
	private TimeData time;
	private LaserDatum[] data;

	public LaserData( TimeData Tlsr, BufferedReader laserIn ) throws IOException {
		time = Tlsr;
		data = new LaserDatum[Tlsr.size()];
		int i = 0;
		for( String line = laserIn.readLine(); line != null; line = laserIn.readLine() ) {
			data[i] = new LaserDatum( line );
			i++;
		}
	}

	public int size() {
		return data.length;
	}

	public LaserDatum getSample(int i) {
		return data[i];
	}

	public double getTS(int t) {
		return time.getTS(t);
	}

	public String getCarmenFormat(int i, NumberFormat nf ) {
		return nf.format(time.getTS(i)) + " " + getSample(i).getCarmenFormat(nf);
	}

	public int getTimeIndex(int time) {
		return this.time.getTimeIndex(time);
	}

	public TimeData getTimeData() {
		return time;
	}

	public RobotPath createPath() {
		RobotPath rc = new RobotPath(new LandmarkDatum(getSample(0)).getSet(time.getTS(0)), null);
		for( int i = 1; i < time.size(); i++ ) {
			rc = new RobotPath(new LandmarkDatum(getSample(i)).getSet(time.getTS(i)), null, rc);
		}
		return rc;
	}
}
