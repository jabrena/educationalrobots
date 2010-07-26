package oursland.kalman;

import java.util.TreeMap;

public class KalmanActionModel {

	private TreeMap<String, Double> propertyMap = new TreeMap<String, Double>();

	public KalmanActionModel(double delta) {
		setValue("delta", delta);
	}

	// gets the a posteriori estimate of the model
	public double getPrioriEstimate(double lastPosteriori) {
		return lastPosteriori + getDelta();
	}

	public double getDelta() {
		return getValue("delta");
	}

	public void setValue(String key, double value) {
		propertyMap.put(key, value);
	}

	public double getValue(String key) {
		Double d = propertyMap.get(key);
		if( d != null ) {
			return d.doubleValue();
		}
		return 0.0;
	}
}
