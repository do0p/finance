package at.brandl.finance.core.linear;

import org.junit.Before;

import at.brandl.finance.core.AbstractMaschineLearningTest;

public abstract class AbstractLinearTest extends
		AbstractMaschineLearningTest<LinearModel> {

	private static final double UPPER = 1;
	private static final double LOWER = -1 * UPPER;
	private LinearCore core;

	@Before
	public void setUp() {
		core = new LinearCore();
	}

	protected LinearCore getCore() {
		return core;
	}

	protected double getUpper() {
		return UPPER;
	}

	protected double getLower() {
		return LOWER;
	}

}