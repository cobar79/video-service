package com.bar.osi.video.service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoServiceRuntimeException extends RuntimeException {

	/** Exception reason. */
	private final ErrorReason errorReason;

	public VideoServiceRuntimeException(String msg, ErrorReason errorReason) {
		super(msg);
		this.errorReason = errorReason;
	}
}
