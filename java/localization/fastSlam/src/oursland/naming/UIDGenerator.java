package oursland.naming;

/**
 * @author oursland
 */
public class UIDGenerator {
	private int nextId = 0;
	public synchronized int next() {
		return nextId++;
	}
}
