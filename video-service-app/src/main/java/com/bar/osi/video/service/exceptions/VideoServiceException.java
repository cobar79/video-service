package com.bar.osi.video.service.exceptions;

public class VideoServiceException extends Exception {

	/** Exception reason. */
	private final ErrorReason errorReason;

	public VideoServiceException(String message, ErrorReason errorReason) {
		super(message);
		this.errorReason = errorReason;
	}
}
