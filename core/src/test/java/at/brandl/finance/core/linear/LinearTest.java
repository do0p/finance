package at.brandl.finance.core.linear;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.finance.common.Node;
import at.brandl.finance.common.NodeSet;

public class LinearTest {

	private LinearCore core;

	@Before
	public void setUp() {
		core = new LinearCore();
	}

	@Test
	public void testPredict() {

		LinearModel model = createModel();
		Assert.assertEquals(10, core.predict(model, createNodeSet(1d, 3d))[0], 0);
		Assert.assertEquals(4, core.predict(model, createNodeSet(1d, 1d))[0], 0);
		Assert.assertEquals(8, core.predict(model, createNodeSet(-1d, -3d))[0], 0);
	}

	private LinearModel createModel() {

		double[][] thetaData = new double[3][];
		thetaData[0] = new double[] { 0, 10, -1 };
		thetaData[1] = new double[] { 10, -1, -1 };
		thetaData[2] = new double[] { 0, -1, 10 };

		RealMatrix theta = MatrixUtils.createRealMatrix(thetaData);
		return new LinearModel(new Double[] { 4d, 8d, 10d }, theta);
	}

	private NodeSet createNodeSet(double... values) {

		NodeSet dataSet = new NodeSet();
		for (int i = 0; i < values.length; i++) {
			dataSet.addNode(new Node(i + i, values[i]));
		}
		return dataSet;
	}
}
