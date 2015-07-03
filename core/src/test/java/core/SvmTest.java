package core;

import java.io.IOException;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import org.junit.Assert;
import org.junit.Test;

import at.brandl.finance.core.SvmData;
import at.brandl.finance.core.SvmScale;

public class SvmTest {

	private String dataFilename = "C:\\Users\\Dominik\\git\\finance\\core\\src\\test\\java\\core\\test.txt";
	private String saveFilename = dataFilename + ".scale";

	@Test
	public void svm() throws IOException {

		SvmData data = scale();
		svm_model model = train(data);

		svm_node[] input = createInput();
		double result = predict(model, input);
		Assert.assertEquals(2, result, 0);
	}

	private svm_node[] createInput() {
		svm_node[] input = new svm_node[3];
		input[0] = createNode(1, -1);
		input[1] = createNode(2, 1);
		input[2] = createNode(3, -1);
		return input;
	}

	private svm_node createNode(int index, double value) {
		svm_node node = new svm_node();
		node.index = index;
		node.value = value;
		return node;
	}

	private double predict(svm_model model, svm_node[] input) {
		return svm.svm_predict(model, input);
	}

	private svm_model train(SvmData data) {
		svm_problem problem = data.toProblem();
		svm_parameter params = createParams();
		svm.svm_check_parameter(problem, params);
		return svm.svm_train(problem, params);
	}

	private SvmData scale() throws IOException {

		return new SvmScale().scale(dataFilename, saveFilename, null, -1.0d,
				+1.0d, 0.0d, 0.0d, false);
	}

	private svm_parameter createParams() {
		svm_parameter params = new svm_parameter();
		params.C = 10;
		params.kernel_type = 3;
		params.svm_type = 0;
		params.gamma = 0.1;
		return params;
	}

}
