package vp.dataassoc;

public class AssocPair {
	final int current;
	final int previous;
	
	public AssocPair(int current, int previous) {
		this.current = current;
		this.previous = previous;
	}
	
	public String toString() {
		return "<" + current + ", " + previous + ">";
	}
}
