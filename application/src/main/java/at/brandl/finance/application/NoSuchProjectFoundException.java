package at.brandl.finance.application;

public class NoSuchProjectFoundException extends IllegalArgumentException {

	public NoSuchProjectFoundException(String projectName) {
		super(projectName);
	}

	private static final long serialVersionUID = 5772931151298877799L;

}
