package com.bar.osi.video.model.live;

public enum LivePrefix {

	LIVE_INPUT("mli-"),
	DESTINATION_APP("mlia-"),
	DESTINATION_APP_INSTANCE("mlii-"),
	CHANNEL_NAME("mlc-"),
	SEPARATOR("-")
	;

	/** Package Prefix. */
	private final String prefix;

	LivePrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() { return this.prefix; }

}
