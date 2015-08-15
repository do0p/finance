package at.brandl.finance.application.error;

public class UnknownProjectFileFormatException extends RuntimeException {

	private static final long serialVersionUID = 6267647826696464141L;

	public UnknownProjectFileFormatException(Exception e) {
		super(e);
	}


}
