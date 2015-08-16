package at.brandl.finance.core.linear;

import static at.brandl.finance.utils.TestProperties.getTestFile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer.Formula;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import at.brandl.finance.common.Data;
import at.brandl.finance.common.Node;
import at.brandl.finance.common.NodeSet;
import at.brandl.finance.common.RewindableFileReader;
import at.brandl.finance.core.Scale;

public class MathTest {

	private static final String DATA_FILENAME = getTestFile("test2.txt");
	private static final String SAVE_FILENAME = DATA_FILENAME + ".scale";

	private static final double UPPER = 1;
	private static final double LOWER = -1 * UPPER;
	private Sigmoid sigmoid = new Sigmoid();

	@Test
	public void sigmoid() {
		Assert.assertEquals(0.5, sigmoid.value(0), 0);
		Assert.assertEquals(0.99, sigmoid.value(10), 0.01);
		Assert.assertEquals(0.00, sigmoid.value(-10), 0.01);
	}

	@Test
	public void multiply() {
		double[][] thetaData = new double[3][];
		thetaData[0] = new double[] { 1, 2, 3 };
		thetaData[1] = new double[] { 4, 5, 6 };
		thetaData[2] = new double[] { 7, 8, 9 };

		double[][] xData = new double[2][];
		xData[0] = new double[] { 1, 2, 3 };
		xData[1] = new double[] { 1, 4, 5 };

		double[][] expectedData = new double[2][];
		expectedData[0] = new double[] { 14, 32, 50 };
		expectedData[1] = new double[] { 24, 54, 84 };

		RealMatrix theta = MatrixUtils.createRealMatrix(thetaData);
		RealMatrix x = MatrixUtils.createRealMatrix(xData);
		RealMatrix expected = MatrixUtils.createRealMatrix(expectedData);

		RealMatrix result = x.multiply(theta.transpose());
		Assert.assertEquals(expected, result);
	}

	@Test
	@Ignore
	public void optimize() throws IOException {
		Data data = createData();
		List<Double> labels = data.getLabels();
		List<NodeSet> nodeSets = data.getNodeSets();

		for (Double label : new HashSet<Double>(labels)) {

			ConvergenceChecker<PointValuePair> checker = new SimpleValueChecker(
					0.01, -1);

			NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
					Formula.FLETCHER_REEVES, checker, 0.01, 0.01, 0.01);

			OptimizationData[] problem = new OptimizationData[7];
			problem[0] = createFunction(data, label);
			problem[1] = createFunctionGradient(data, label);
			problem[2] = GoalType.MINIMIZE;
			problem[3] = new InitialGuess(toArray(nodeSets.get(0)));

			problem[5] = new MaxEval(10000);
			problem[6] = new MaxIter(10000);
			PointValuePair optimum = optimizer.optimize(problem);
			System.out.println(label + ": "
					+ Arrays.toString(optimum.getPoint()));
		}
	}

	private ObjectiveFunction createFunction(Data data, Double label) {
		return new ObjectiveFunction(createValueFunction(data, label));
	}

	private ObjectiveFunctionGradient createFunctionGradient(Data data,
			Double label) {

		return new ObjectiveFunctionGradient(
				createJacobianFunction(data, label));
	}

	private double[] toArray(NodeSet nodeSet) {
		List<Node> nodes = nodeSet.getNodes();
		int numNodes = nodes.size();
		double[] result = new double[numNodes + 1];
		result[0] = 0;
		for (int i = 0; i < numNodes; i++) {
			result[i + 1] = 0;
		}
		return result;
	}

	private Data createData() throws IOException {

		return new Scale(new RewindableFileReader(DATA_FILENAME),
				new BufferedWriter(new FileWriter(SAVE_FILENAME)), null, LOWER,
				UPPER).scale();
	}

	private double[] calcExpected(Double label, List<Double> labels) {
		double[] values = new double[labels.size()];
		for (int i = 0; i < labels.size(); i++) {
			values[i] = labels.get(i).equals(label) ? 1 : 0;
		}
		return values;
	}

	private MultivariateVectorFunction createJacobianFunction(final Data data,
			final Double label) {

		return new MultivariateVectorFunction() {

			@Override
			public double[] value(double[] point)
					throws IllegalArgumentException {

				int numFeatures = point.length;
				double[] result = new double[numFeatures];

				double[] expected = calcExpected(label, data.getLabels());
				RealMatrix thetaMatrix = createTheatMatrix(point);

				List<NodeSet> dataList = data.getNodeSets();

				for (int j = 0; j < numFeatures; j++) {

					double gradient = 0;

					for (int i = 0; i < dataList.size(); i++) {

						NodeSet data = dataList.get(i);
						RealMatrix dataMatrix = createDataMatrix(data);

						RealMatrix value = dataMatrix.multiply(thetaMatrix
								.transpose());
						double h = sigmoid.value(value.getRow(0)[0]);

						double y = expected[i];

						gradient += (h - y) * dataMatrix.getEntry(0, j);

					}

					result[j] = gradient / dataList.size();

				}

				return result;
			}
		};
	}

	private MultivariateFunction createValueFunction(final Data data,
			final Double label) {

		return new MultivariateFunction() {

			@Override
			public double value(double[] point) throws IllegalArgumentException {

				double result = 0;

				double[] expected = calcExpected(label, data.getLabels());

				RealMatrix thetaMatrix = createTheatMatrix(point);
				List<NodeSet> dataList = data.getNodeSets();

				for (int i = 0; i < dataList.size(); i++) {

					NodeSet data = dataList.get(i);
					RealMatrix dataMatrix = createDataMatrix(data);

					RealMatrix value = dataMatrix.multiply(thetaMatrix
							.transpose());
					double h = sigmoid.value(value.getRow(0)[0]);

					double y = expected[i];

					if (y == 0) {
						result -= FastMath.log(1 - h);
					} else {
						result -= FastMath.log(h);
					}
				}
				return result / dataList.size();
			}
		};
	}

	private RealMatrix createTheatMatrix(double[] point) {

		double[][] theta = new double[1][];
		theta[0] = point;
		return MatrixUtils.createRealMatrix(theta);
	}

	private RealMatrix createDataMatrix(NodeSet data) {

		return MatrixUtils.createRealMatrix(toArray(data.getNodes()));
	}

	private double[][] toArray(List<Node> nodes) {

		double[][] data = new double[1][];
		data[0] = new double[nodes.size() + 1];
		data[0][0] = 1;
		for (int i = 0; i < nodes.size(); i++) {
			data[0][i + 1] = nodes.get(i).getValue();
		}
		return data;
	}
}
