package oursland;


public class Symbol {
	protected final String	label;

	protected final Integer	symbol;

	public Symbol(String label, int symbol) {
		this.label = label;
		this.symbol = new Integer(symbol);
	}

	public Symbol(String label) {
		this.label = label;
		this.symbol = null;
	}

	public boolean equals(Object o) {
		boolean rc = super.equals(o);
		if(!rc && o instanceof Symbol) {
			Symbol s = (Symbol) o;
			rc = (this.label.equals(s.label));
			if(symbol != null) {
				rc &= (this.symbol.equals(s.symbol));
			}
		}
		return rc;
	}

	public String toString() {
		return (symbol == null) ? label : label + symbol.toString();
	}

	public String getLabel() {
		return label;
	}

	public Integer getSymbol() {
		return symbol;
	}
}