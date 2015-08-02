package at.brandl.finance.core.linear;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.BaseOptimizer;
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

import at.brandl.finance.common.Data;
import at.brandl.finance.common.Node;
import at.brandl.finance.common.NodeSet;
import at.brandl.finance.core.Core;

public class LinearCore implements Core<LinearModel> {

	private static final int INITIAL_GUESS = 1;
	private static final double TRESHHOLD = 0.0001;
	private static final double LAMBDA = 0.0000001;
	private final Sigmoid sigmoid = new Sigmoid();
	private final Executor executor = Executors.newFixedThreadPool(8);

	@Override
	public LinearModel train(Data data) {

		Double[] labels = data.getUniqueLabels();
		Future<double[]>[] futures = train(data, labels);
		RealMatrix theta = createTheta(futures, labels.length);
		return new LinearModel(labels, theta);
	}

	@Override
	public double[] predict(LinearModel model, NodeSet dataSet) {

		RealMatrix theta = model.getTheta();
		RealMatrix data = createDataMatrix(dataSet);

		RealMatrix result = data.multiply(theta.transpose());
		double[] row = result.getRow(0);

		double maxValue = -1;
		int indexOfMax = 0;
		for (int i = 0; i < row.length; i++) {

			double value = sigmoid.value(row[i]);
			if (value > maxValue) {

				indexOfMax = i;
				maxValue = value;
			}
		}

		Double predictedLabel = model.getLabels()[indexOfMax];
		Double value = maxValue;
		return new double[]{predictedLabel, value};
	}

	private RealMatrix createTheta(Future<double[]>[] futures, int numLabels) {

		try {

			double[][] theta = new double[numLabels][];

			for (int i = 0; i < numLabels; i++) {

				theta[i] = futures[i].get();
			}

			return MatrixUtils.createRealMatrix(theta);

		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private Future<double[]>[] train(final Data data, Double[] labels) {

		@SuppressWarnings("unchecked")
		Future<double[]>[] futures = new Future[labels.length];

		for (int i = 0; i < labels.length; i++) {

			final Double label = labels[i];
			FutureTask<double[]> futureTask = new FutureTask<double[]>(
					new Callable<double[]>() {

						@Override
						public double[] call() throws Exception {
							double[] point = train(data, label);
							System.out.println(label + ": "
									+ Arrays.toString(point));
							return point;
						}
					});

			executor.execute(futureTask);
			futures[i] = futureTask;
		}
		return futures;
	}

	private double[] train(Data data, Double label) {

		BaseOptimizer<PointValuePair> optimizer = createOptimizer();
		OptimizationData[] problem = createProblem(data, label);

		PointValuePair optimum = optimizer.optimize(problem);
		return optimum.getPoint();
	}

	private BaseOptimizer<PointValuePair> createOptimizer() {

		ConvergenceChecker<PointValuePair> checker = new SimpleValueChecker(
				TRESHHOLD, -1);
		return new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES,
				checker, TRESHHOLD, TRESHHOLD, TRESHHOLD);
	}

	private OptimizationData[] createProblem(Data data, Double label) {

		OptimizationData[] problem = new OptimizationData[7];
		problem[0] = createCostFunction(data, label);
		problem[1] = createGradientFunction(data, label);
		problem[2] = GoalType.MINIMIZE;
		problem[3] = createInitialGuess(data.getNumFeatures());
		problem[5] = new MaxEval(10000);
		problem[6] = new MaxIter(10000);
		return problem;
	}

	private InitialGuess createInitialGuess(int numFeatures) {

		double[] point = new double[numFeatures + 1];
		Arrays.fill(point, INITIAL_GUESS);
		return new InitialGuess(point);
	}

	private ObjectiveFunctionGradient createGradientFunction(final Data data,
			final Double label) {

		return new ObjectiveFunctionGradient(new MultivariateVectorFunction() {

			@Override
			public double[] value(double[] point) {

				int numFeatures = point.length;
				double[] result = new double[numFeatures];

				double[] expected = calcExpected(label, data.getLabels());
				RealMatrix thetaMatrix = createThetaMatrix(point);

				List<NodeSet> dataList = data.getNodeSets();

				int m = dataList.size();
				for (int i = 0; i < m; i++) {

					RealMatrix dataMatrix = createDataMatrix(dataList.get(i));

					RealMatrix value = dataMatrix.multiply(thetaMatrix
							.transpose());

					double h = sigmoid.value(value.getRow(0)[0]);
					double y = expected[i];

					for (int j = 0; j < numFeatures; j++) {

						result[j] += (h - y) * dataMatrix.getEntry(0, j) / m;
						if (j > 0) {
							result[j] += LAMBDA / m * thetaMatrix.getRow(0)[j];
						}
					}
				}

				return result;
			}
		});
	}

	private ObjectiveFunction createCostFunction(final Data data,
			final Double label) {

		return new ObjectiveFunction(new MultivariateFunction() {

			@Override
			public double value(double[] point) {

				RealMatrix thetaMatrix = createThetaMatrix(point);
				List<NodeSet> dataList = data.getNodeSets();
				double[] expected = calcExpected(label, data.getLabels());
				int m = dataList.size();

				double result = 0;
				for (int i = 0; i < m; i++) {

					RealMatrix dataMatrix = createDataMatrix(dataList.get(i));
					double h = calcRealValue(thetaMatrix, dataMatrix);

					if (expected[i] == 0) {
						result -= FastMath.log(1 - h);
					} else {
						result -= FastMath.log(h);
					}
				}

				double thetaSquaredSum = thetaMatrix.multiply(
						thetaMatrix.transpose()).getRow(0)[0];
				double regularization = LAMBDA / 2 * m * thetaSquaredSum;

				return result / m + regularization;
			}

		});
	}

	private double calcRealValue(RealMatrix thetaMatrix, RealMatrix dataMatrix) {

		RealMatrix value = dataMatrix.multiply(thetaMatrix.transpose());
		return sigmoid.value(value.getRow(0)[0]);
	}

	private RealMatrix createThetaMatrix(double[] point) {

		double[][] theta = new double[1][];
		theta[0] = point;
		return MatrixUtils.createRealMatrix(theta);
	}

	private RealMatrix createDataMatrix(NodeSet data) {

		List<Node> nodes = data.getNodes();
		int numFeatures = nodes.size();
		double[][] array = new double[1][];
		array[0] = new double[numFeatures + 1];
		array[0][0] = 1;
		for (int i = 0; i < numFeatures; i++) {
			array[0][i + 1] = nodes.get(i).getValue();
		}

		return MatrixUtils.createRealMatrix(array);
	}

	private double[] calcExpected(Double label, List<Double> labels) {

		double[] values = new double[labels.size()];
		for (int i = 0; i < labels.size(); i++) {
			values[i] = labels.get(i).equals(label) ? 1 : 0;
		}
		return values;
	}
}
