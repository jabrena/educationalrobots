package oursland.collection;
/**
 * @author oursland
 */
public class IntegerRingBuffer {
	private int[] buffer = new int[10];
	private int start;
	private int next;
	private int count;

	public void push(int ch) {
		if( count == buffer.length ) {
			expandBuffer(2*buffer.length+1);
		}
		buffer[next] = ch;
		next = nextBufferIndex(next);
		count++;
	}
	
	public int pop() {
		if( count <= 0 ) {
			throw new ArrayIndexOutOfBoundsException(0);
		}
		int rc = buffer[start];
		start = nextBufferIndex(start);
		count--;
		return rc;
	}
	
	public int size() {
		return count;
	}
	
	public int get(int index) {
		if( index < 0 || index >= count ) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return buffer[(start+index)%buffer.length];		
	}

	private int nextBufferIndex(int current) {
		assert (current >= 0);
		current++;
		current %= buffer.length;
		return current;
	}
	
	private void expandBuffer(int size) {
		int[] temp = new int[size];
		if( next <= start ) {
			int size1 = buffer.length-start;
			System.arraycopy(buffer, start, temp, 0, size1);
			System.arraycopy(buffer, 0, temp, size1, count-size1);
		} else {
			System.arraycopy(buffer, start, temp, 0, count);
		}
		this.buffer = temp;
		this.start = 0;
		this.next = count;
	}

}
