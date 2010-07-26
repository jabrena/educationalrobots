package oursland.text;

import java.io.PrintStream;

/**
 * @author oursland
 */
public class PrintStreamColumnFormatter {
	final public PrintStream out;
	final private int[] columns;

	private int current = 0;
	private int length = 0;
	
	public PrintStreamColumnFormatter(PrintStream out, int[] columns) {
		this.out = out;
		this.columns = columns;
	}

	public void nextColumn(String str) {
		extendToColumn();
		out.print(str);
		length += str.length();
		current++;
	}
	public void nextLine() {
		length = 0;
		current = 0;
		out.println();
	}
	private void extendToColumn() {
		StringBuffer buf = new StringBuffer();
		while( length < columns[current] ) {
			buf.append(' ');
			length++;
		}
		out.print(buf.toString());
	}

	public int getNextColumnWidth() {
		if( current+1 < columns.length ) {
			return columns[current+1] - columns[current];
		}
		return Integer.MAX_VALUE;
	}
}
