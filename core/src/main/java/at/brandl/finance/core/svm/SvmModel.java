package at.brandl.finance.core.svm;

import libsvm.svm_model;
import at.brandl.finance.core.Model;

public class SvmModel implements Model {

	private final svm_model svm_train;

	public SvmModel(svm_model svm_train) {
		
		this.svm_train = svm_train;
	}

	public svm_model getSvmModel() {

		return svm_train;
	}

}
