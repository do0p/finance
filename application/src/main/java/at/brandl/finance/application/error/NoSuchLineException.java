package at.brandl.finance.application.error;

public class NoSuchLineException extends IllegalArgumentException {

	public NoSuchLineException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 8947226858225716594L;

}
