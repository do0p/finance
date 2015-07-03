package core;

import java.io.IOException;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import org.junit.Test;

import at.brandl.finance.core.SvmData;
import at.brandl.finance.core.SvmScale;

public class SvmTest {

	@Test
	public void svm() throws IOException {
		String dataFilename = "";
		SvmData data = scale(dataFilename);
		svm_model model = train(data);
		
		svm_node[] input = null;
		double result = predict(model, input);
		
	}

	private double predict(svm_model model, svm_node[] input) {
		return svm.svm_predict(model, input);
	}

	private svm_model train(SvmData data) {
		svm_problem problem = data.toProblem();
		svm_parameter params = createParams();
		svm.svm_check_parameter(problem, params);
		svm_model model = svm.svm_train(problem, params);
		return model;
	}

	private SvmData scale(String dataFilename) throws IOException {
		
		String saveFilename = dataFilename + ".scale";
		return new SvmScale().scale(dataFilename, saveFilename, null, -1.0d, +1.0d, 0.0d, 0.0d, false);
	}

	private svm_parameter createParams() {
		svm_parameter params = new svm_parameter();
		double c = 0d;
		params.C = c;
		return params;
	}


}
