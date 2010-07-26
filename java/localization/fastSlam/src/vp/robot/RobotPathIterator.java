
package vp.robot;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RobotPathIterator implements Iterator {
	private RobotPath path;
	
	public RobotPathIterator(RobotPath path) {
		this.path = path;
	}


	public boolean hasNext() {
		return (path!=null);
	}

	public Object next() {
		if( path == null ) {
			throw new NoSuchElementException();
		}
		RobotPath rc = path;
		path = path.previousPath();
		return rc;
	}

	public void remove() {
		throw new UnsupportedOperationException ();
	}
}
