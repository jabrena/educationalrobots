package oursland.collection;

/**
 * @author oursland
 */
public class IntegerPipe {
	private final int[] buffer;
	private int bufferRead = 0;
	private int bufferWrite = 0;
	private int available = 0;
	
	public IntegerPipe(int size) {
		this.buffer = new int[size];
	}
	
	public int put(int ch) {
		synchronized(buffer) {
			if( available == buffer.length ) {
				throw new Error("Parser.writeBuffer: buffer is full.");
			}
			buffer[bufferWrite] = ch;
			bufferWrite = (bufferWrite+1)%buffer.length;
			available++;
			return ch;
		}
	}
	
	public int peek() {
		synchronized(buffer) {
			if( available == 0 ) {
				throw new Error("Parser.readBuffer: buffer is empty.");
			}
			return buffer[bufferRead];
		}
	}
	
	public int get() {
		synchronized(buffer) {
			if( available == 0 ) {
				throw new Error("Parser.readBuffer: buffer is empty.");
			}
			int rc = buffer[bufferRead];
			bufferRead = (bufferRead+1)%buffer.length;
			available--;
			return rc;
		}
	}

	public int available() {
		return available;
	}

}
