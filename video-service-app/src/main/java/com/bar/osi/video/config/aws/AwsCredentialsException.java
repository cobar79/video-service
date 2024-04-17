package com.bar.osi.video.config.aws;

import com.bar.osi.video.service.exceptions.ErrorReason;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwsCredentialsException extends RuntimeException {

	/** Error reason. */
	private final ErrorReason errorReason;

	public AwsCredentialsException(String message, ErrorReason errorReason) {
		super(message);
		this.errorReason = errorReason;
	}
}
