package dev.mortus.voronoi.exception;

public class UnfinishedStateException extends RuntimeException {

	private static final long serialVersionUID = -5521256507536428244L;

	public UnfinishedStateException() {
		super();
	}

	public UnfinishedStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnfinishedStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnfinishedStateException(String message) {
		super(message);
	}

	public UnfinishedStateException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		return "Can't getResult() until Worker isDone()!";
	}
	
}
