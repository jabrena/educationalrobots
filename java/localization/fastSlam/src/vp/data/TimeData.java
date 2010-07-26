package vp.data;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class TimeData {
	private ArrayList<Integer> list = new ArrayList<Integer>();
	
	public TimeData() {
		
	}
	
	public TimeData( BufferedReader in ) throws NumberFormatException, IOException {
		for( String line = in.readLine(); line != null; line = in.readLine() ) {
			list.add(new Integer(line));
		}
	}
	public int size() {
		return list.size();
	}
	
	public double getTS(int i) {
		return (list.get(i)).doubleValue()/1000.0;
	}
	
	public int getTimeIndex(int time) {
		int rc = Collections.binarySearch(list, new Integer(time) );
		if( rc < 0 ) {
			rc +=  1;
			rc *= -1;
		}
		return rc;
	}

	public void add(double time) {
		list.add( new Integer((int)(time*1000)) );
	}
}
