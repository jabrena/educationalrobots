package vp.mapping;
import java.util.Random;

public class Offset implements Comparable {
	public double x = 0.0;
	public double y = 0.0;
	public double t = 0.0;
	public double fit = Double.MAX_VALUE;
	
	public Offset() {}
	
	public Offset(Random r, double varx, double vary, double vart) {
		x = varx*(2*r.nextDouble()-1);
		y = vary*(2*r.nextDouble()-1);
		t = vart*(2*r.nextDouble()-1);
	}
	
	public Offset(Offset o1, Offset o2, Random r) {
		x = (o1.x+o2.x)/2;
		y = (o1.y+o2.y)/2;
		t = (o1.t+o2.t)/2;
		x += (o1.x-o2.x)*(3*r.nextDouble()-1);
		y += (o1.y-o2.y)*(3*r.nextDouble()-1);
		t += (o1.t-o2.t)*(3*r.nextDouble()-1);
	}

	public int compareTo(Object arg0) {
		Offset o = (Offset)arg0;
		if( fit < o.fit) {
			return -1;		
		} else if( fit > o.fit) {
			return 1;
		}
		return 0;
	}

}
