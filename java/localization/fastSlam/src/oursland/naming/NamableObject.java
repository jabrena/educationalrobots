package oursland.naming;

/**
 * @author oursland
 */
public class NamableObject implements Namable {
	private final String name;
	public NamableObject(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}
