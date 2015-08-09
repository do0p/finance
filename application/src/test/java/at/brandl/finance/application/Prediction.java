package at.brandl.finance.application;

public class Prediction {

	private final String label;
	private final double confidence;
	
	public Prediction(String label, double confidence) {
		this.label = label;
		this.confidence = confidence;
	}
	
	public String getLabel() {
		return label;
	}
	
	public double getConfidence() {
		return confidence;
	}
	
}
