/*
 * Created on Mar 16, 2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package oursland.naming;

import java.util.TreeMap;

public class UniqueNameGenerator {
	private final String	base;

	private long			count	= 0;

	private TreeMap<String, UniqueName>			set		= new TreeMap<String, UniqueName>();

	public UniqueNameGenerator(String base) {
		this.base = base;
	}

	public synchronized UniqueName create() {
		UniqueName rc = new UniqueName(nextDescription());
		add(rc);
		return rc;
	}

	public UniqueName getName(String name) {
		UniqueName rc = get(name);
		if(rc == null) {
			rc = new UniqueName(name);
			add(rc);
		}
		return rc;
	}

	public boolean exists(String name) {
		return set.containsKey(name);
	}

	public void add(UniqueName name) {
		if(!exists(name.getNameDesc())) {
			set.put(name.getNameDesc(), name);
			count = Math.max(count, Integer.parseInt(name.getNameDesc().substring(base.length())));
		} else {
			throw new Error("Tried to create a duplicate name.");
		}
	}

	public String nextDescription() {
		String rc = base + (++count);
		while(exists(rc)) {
			rc = base + (++count);
		}
		return rc;
	}

	public void remove(UniqueName name) {
		name.setClosed();
		set.remove(name.getNameDesc());
	}

	public UniqueName get(String name) {
		return set.get(name);
	}
}