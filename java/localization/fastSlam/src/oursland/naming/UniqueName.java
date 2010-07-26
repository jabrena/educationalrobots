package oursland.naming;

public class UniqueName implements Comparable {
	//	private static UniqueNameGenerator man = new UniqueNameGenerator();
	private boolean			closed	= false;

	private final String	name;

	UniqueName(String name) {
		this.name = name;
	}

	String getNameDesc() {
		return name;
	}

	public String toString() {
		if(closed) {
			throw new Error();
		}
		return name;
	}

	public int compareTo(Object arg0) {
		UniqueName that = (UniqueName) arg0;
		return name.compareTo(that.name);
	}

	void setClosed() {
		closed = true;
	}
}