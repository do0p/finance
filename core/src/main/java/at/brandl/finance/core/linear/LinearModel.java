package at.brandl.finance.core.linear;

import org.apache.commons.math3.linear.RealMatrix;

import at.brandl.finance.core.Model;

public class LinearModel implements Model{

	private final RealMatrix theta;
	private final Double[] labels;

	public LinearModel(Double[] labels, RealMatrix theta) {

		this.labels = labels;
		this.theta = theta;
	}
	
	public RealMatrix getTheta() {

		return theta;
	}

	public Double[] getLabels() {
		
		return labels;
	}
}
