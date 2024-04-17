package com.bar.osi.video.service.exceptions;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoValidationException extends Exception {

	/** Exception reason. */
	private final ErrorReason errorReason;

	/** list of errors. */
	@SuppressWarnings(value = "squid:S1165") //Can't be final, confused since it uses lombok getter/setter
	private final List<String> errors;

	public VideoValidationException(String message, List<String> errors) {
		super(message);
		this.errorReason = ErrorReason.VALIDATION_EXCEPTION;
		this.errors = errors;
	}

}
