package com.bar.osi.video.model.pkg;

import java.util.Objects;

import lombok.ToString;

@ToString
public final class HlsPackageRequest {

	/** Segments are rounded to the nearest multiple of the source segment duration.*/
	private Integer segmentDurationSeconds;

	/** Time window (in seconds) contained in each parent manifest.*/
	private Integer playlistWindowSeconds;

	public Integer getSegmentDurationSeconds() {
		if (this.segmentDurationSeconds == null) {
			this.segmentDurationSeconds = 6;
		}
		return this.segmentDurationSeconds;
	}

	public Integer getPlaylistWindowSeconds() {
		if (this.playlistWindowSeconds == null) {
			this.playlistWindowSeconds = 60;
		}
		return this.playlistWindowSeconds;
	}

	private HlsPackageRequest(HlsPackageRequestBuilder builder) {
		this.segmentDurationSeconds = builder.segmentDurationSeconds;
		this.playlistWindowSeconds = builder.playlistWindowSeconds;
	}

	public static class HlsPackageRequestBuilder {

		/** Segments are rounded to the nearest multiple of the source segment duration.*/
		private Integer segmentDurationSeconds;

		/** Time window (in seconds) contained in each parent manifest.*/
		private Integer playlistWindowSeconds;

		public HlsPackageRequestBuilder segmentDurationSeconds(Integer segmentDurationSeconds) {
			this.segmentDurationSeconds = Objects.requireNonNullElse(segmentDurationSeconds, 6);
			return this;
		}

		public HlsPackageRequestBuilder playlistWindowSeconds(Integer playlistWindowSeconds) {
			this.playlistWindowSeconds = Objects.requireNonNullElse(playlistWindowSeconds, 60);
			return this;
		}

		public HlsPackageRequest build() {
			return new HlsPackageRequest(this);
		}

	}


}
