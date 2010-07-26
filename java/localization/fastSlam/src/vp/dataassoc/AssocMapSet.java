package vp.dataassoc;

public class AssocMapSet {
	private AssocMap[] maps;
	private double[] weights;
	
	public AssocMapSet(AssocMap[] maps, double[] weights) {
		this.maps = maps;
		this.weights = weights;
	}
	
	public int size() {
		return maps.length;
	}
	
	public double getWeight(int index) {
		return weights[index];
	}
	
	public AssocMap getMap(int index) {
		return maps[index];
	}
	
	public AssocMap selectMap(double value) {
		int i = 0;
		// throw an ArrayIndexOutOfBoundsException if the value is too high
		while(true) {
			value -= weights[i];
			if( value <= 0 ) {
				return maps[i];
			}
			i++;			
		}
	}

	public void normalizeWeights() {
		double sum = 0.0;
		for( int i = weights.length-1; i >= 0 ; i-- ) {
			sum += weights[i];
		}
		for (int i = 0; i < weights.length; i++) {
			weights[i] /= sum;
		}
	}
}
