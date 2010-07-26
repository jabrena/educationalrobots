package oursland.parse;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import oursland.collection.IntegerRingBuffer;


/**
 * Provides look-ahead capability to aid in tokenizer implementations.
 * @author oursland
 */
public abstract class BaseTokenizer {
	private InputStream in;
	private IntegerRingBuffer buf = new IntegerRingBuffer();
	
	public BaseTokenizer( InputStream in ) {
		this.in = in;
	}
	
	public abstract boolean hasNextToken() throws IOException;
	public abstract String nextToken() throws IOException;
	
	protected void consume(char c) throws IOException {
		int ci = peekNoEOF(0);
		if( c == ci ) {
			buf.pop();
		} else {
			throw new Error("Value mismatch in Tokenizer.consume()");
		}
	}

	protected char peek(int n) throws IOException {
		int rc = peekNoEOF(n);
		if( rc == -1 ) {
			throw new EOFException();
		}
		return (char)rc;
	}
	
	protected int peekNoEOF(int n) throws IOException {
		while( n >= buf.size() ) {
			int ci = in.read();
			if( ci == -1 ) {
				return -1;
			}
			buf.push(ci);
		}
		return (char)buf.get(n);
	}
}
