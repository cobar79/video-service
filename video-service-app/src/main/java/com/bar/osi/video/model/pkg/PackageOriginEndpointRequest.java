package com.bar.osi.video.model.pkg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.bar.osi.video.service.exceptions.VideoValidationException;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PackageOriginEndpointRequest {

	/** The ID is the endpoint identifier that you use for API and console interactions. */
	private final String id;

	/** A short text description of the endpoint.. */
	private final String description;

	/** The manifest name is a short string that is appended to the endpoint URL to create a unique path to this endpoint.*/
	private final String manifestName;

	/** A start over window is a portion of a live stream that is made available for on-demand viewing.*/
	private final Integer startOverWindowSeconds;

	/** A time delay specifies when live content is available for playback.*/
	private final Integer timeDelaySeconds;

	/** Packager Settings. */
	private final HlsPackageRequest hlsPackageRequest;

	public PackageOriginEndpointRequest(PackageOriginEndpointRequestBuilder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.manifestName = builder.manifestName;
		this.startOverWindowSeconds = builder.startOverWindowSeconds;
		this.timeDelaySeconds = builder.timeDelaySeconds;
		this.hlsPackageRequest = builder.hlsPackageRequest;
	}



	public static class PackageOriginEndpointRequestBuilder {

		/** The Name of the inbound Stream.*/
		private final String streamName;

		/** The ID is the endpoint identifier that you use for API and console interactions. */
		private String id;

		/** A short text description of the endpoint.. */
		private String description;

		/** The manifest name is a short string that is appended to the endpoint URL to create a unique path to this endpoint.*/
		private String manifestName;

		/** A start over window is a portion of a live stream that is made available for on-demand viewing.*/
		private Integer startOverWindowSeconds;

		/** A time delay specifies when live content is available for playback.*/
		private Integer timeDelaySeconds;

		/** Packager Settings. */
		private HlsPackageRequest hlsPackageRequest;

		public PackageOriginEndpointRequestBuilder(String streamName) {
			this.streamName = streamName;
		}

		public PackageOriginEndpointRequestBuilder id(String id) {
			if (id == null) {
				this.id = PackagePrefix.ORIGIN_ENDPOINT.getPrefix() + streamName;
			} else {
				this.id = PackagePrefix.ORIGIN_ENDPOINT.getPrefix() + id;
			}
			return this;
		}

		public PackageOriginEndpointRequestBuilder description(String description) {
			this.description = description;
			return this;
		}

		public PackageOriginEndpointRequestBuilder manifestName(String manifestName) {
			if (manifestName == null) {
				this.manifestName =
					PackagePrefix.ORIGIN_ENDPOINT.getPrefix() + this.streamName + PackagePrefix.MANIFEST_NAME.getPrefix();
			} else {
				this.manifestName = PackagePrefix.ORIGIN_ENDPOINT.getPrefix() + streamName + "-" + manifestName;
			}
			return this;
		}

		//The maximum start over window is 14 days (1,209,600 seconds).
		public PackageOriginEndpointRequestBuilder startOverWindowSeconds(Integer startOverWindowSeconds) {
			//2 Days
			this.startOverWindowSeconds = Objects.requireNonNullElse(startOverWindowSeconds, 172800);
			return this;
		}

		public PackageOriginEndpointRequestBuilder timeDelaySeconds(Integer timeDelaySeconds) {
			this.timeDelaySeconds = Objects.requireNonNullElse(timeDelaySeconds, 0);
			return this;
		}

		public PackageOriginEndpointRequestBuilder hlsPackageRequest(HlsPackageRequest hlsPackageRequest) {
			this.hlsPackageRequest = hlsPackageRequest;
			return this;
		}

		public PackageOriginEndpointRequest build() throws VideoValidationException {
			PackageOriginEndpointRequest originEndpointRequest = new PackageOriginEndpointRequest(this);
			validate(originEndpointRequest);
			return originEndpointRequest;
		}

		private void validate(PackageOriginEndpointRequest originEndpointRequest) throws VideoValidationException {
			List<String> errors = new ArrayList<>();
			if (originEndpointRequest.id == null) {
				errors.add("id is required");
			}
			if (originEndpointRequest.manifestName == null) {
				errors.add("manifest name is required");
			}
			if (originEndpointRequest.startOverWindowSeconds == null) {
				errors.add("Start Over Windows Second is required. Max 14 days, default 2 days.");
			}
			if (originEndpointRequest.timeDelaySeconds == null) {
				errors.add("Time Delay Seconds is required. Default 0 days.");
			}

			if (!errors.isEmpty()) {
				throw new VideoValidationException(PackageOriginEndpointRequest.class.getSimpleName() + " is invalid!", errors);
			}
		}

	}
}
