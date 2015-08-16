package at.brandl.finance.application.error;

import java.io.IOException;

public class SaveProjectFailedException extends RuntimeException {

	private static final long serialVersionUID = 2657426077372096407L;

	public SaveProjectFailedException(IOException e) {
		super(e);
	}
}
