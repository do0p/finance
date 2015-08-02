package at.brandl.finance.core.svm;

import org.junit.Before;

import at.brandl.finance.core.AbstractMaschineLearningTest;

public abstract class AbstractSvmTest extends
		AbstractMaschineLearningTest<SvmModel> {

	private static final double UPPER = 1;
	private static final double LOWER = -1 * UPPER;
	private SvmCore core;

	@Before
	public void setUp() {
		core = new SvmCore();
	}

	protected SvmCore getCore() {
		return core;
	}

	protected double getUpper() {
		return UPPER;
	}

	protected double getLower() {
		return LOWER;
	}

}