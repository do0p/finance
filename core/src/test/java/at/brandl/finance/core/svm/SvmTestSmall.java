package at.brandl.finance.core.svm;


public class SvmTestSmall extends AbstractSvmTest {

	private static final String DATA_FILENAME = DIR + "core\\test.txt";
	private static final String INPUT_FILENAME = DIR + "core\\cv.txt";
	private static final String SAVE_FILENAME = DATA_FILENAME + ".scale";

	protected String getValidationFileName() {
		return INPUT_FILENAME;
	}

	protected String getSaveFileName() {
		return SAVE_FILENAME;
	}

	protected String getDataFileName() {
		return DATA_FILENAME;
	}

}
