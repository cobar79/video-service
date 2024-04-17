package com.bar.osi.video.model.pkg;

public enum PackagePrefix {
	CHANNEL("mpc-"),
	ORIGIN_ENDPOINT("mpoe-"),
	MANIFEST_NAME("-index"),
	SEPARATOR("-")
	;

	/** Package Prefix. */
	private final String prefix;

	PackagePrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() { return this.prefix; }
}
