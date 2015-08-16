package at.brandl.finance.application.error;

public class TrainingFailedException extends RuntimeException {

	private static final long serialVersionUID = 5012525202533371539L;

	public TrainingFailedException(Exception e) {
		super(e);
	}
}
