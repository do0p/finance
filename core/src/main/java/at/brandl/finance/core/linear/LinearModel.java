package at.brandl.finance.core.linear;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import at.brandl.finance.core.Model;

public class LinearModel implements Model {

	private static final long serialVersionUID = -7371863885026692181L;
	private transient RealMatrix theta;
	private final Double[] labels;

	public LinearModel(Double[] labels, RealMatrix theta) {

		if(labels == null) {
			throw new IllegalArgumentException("lables is null");
		}
		if(theta == null) {
			throw new IllegalArgumentException("theta is null");
		}
		this.labels = labels;
		this.theta = theta;
	}

	public RealMatrix getTheta() {

		return theta;
	}

	public Double[] getLabels() {

		return labels;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LinearModel)) {
			return false;
		}

		LinearModel other = (LinearModel) obj;
		return Arrays.equals(labels, other.labels) && theta.equals(other.theta);
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(labels);
		result = prime * result + theta.hashCode();
		return result;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {

		out.defaultWriteObject();

		int numRows = theta.getRowDimension();
		int numCols = theta.getColumnDimension();
		double[][] data = theta.getData();

		out.writeInt(numRows);
		out.writeInt(numCols);
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				out.writeDouble(data[i][j]);
			}
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		in.defaultReadObject();

		int numRows = in.readInt();
		int numCols = in.readInt();
		double[][] data = new double[numRows][];

		for (int i = 0; i < numRows; i++) {
			data[i] = new double[numCols];
			for (int j = 0; j < numCols; j++) {
				data[i][j] = in.readDouble();
			}
		}

		theta = MatrixUtils.createRealMatrix(data);
	}

}
