package at.brandl.finance.core.linear;


public class LinearTestReal extends AbstractLinearTest {

	private static final String DATA_FILENAME = DIR + "core\\test2.txt";
	private static final String INPUT_FILENAME = DIR + "core\\cv2.txt";
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
