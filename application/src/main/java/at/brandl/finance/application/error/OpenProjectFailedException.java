package at.brandl.finance.application.error;

import java.io.IOException;

public class OpenProjectFailedException extends RuntimeException {

	private static final long serialVersionUID = 1413875317548231771L;

	public OpenProjectFailedException(IOException e) {
		super(e);
	}
}
