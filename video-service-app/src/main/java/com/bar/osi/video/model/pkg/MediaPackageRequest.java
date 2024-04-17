package com.bar.osi.video.model.pkg;

import com.bar.osi.video.service.exceptions.VideoValidationException;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MediaPackageRequest {

	/** MediaPackage Channel Request.*/
	private final PackageChannelRequest channelRequest;

	/** MediaPackage Origin Endpoint Request. */
	private final PackageOriginEndpointRequest originEndpointRequest;


	public MediaPackageRequest(MediaPackageRequestBuilder builder) {
		this.channelRequest = builder.channelRequest;
		this.originEndpointRequest = builder.originEndpointRequest;
	}


	public static class MediaPackageRequestBuilder {
		/** The Name of the inbound Stream.*/
		private final String streamName;

		/** MediaPackage Channel Request.*/
		private PackageChannelRequest channelRequest;

		/** MediaPackage Origin Endpoint Request. */
		private PackageOriginEndpointRequest originEndpointRequest;

		public MediaPackageRequestBuilder(String streamName) {
			this.streamName = streamName;
		}

		public MediaPackageRequestBuilder channelRequest(PackageChannelRequest channelRequest) throws VideoValidationException {
			if (this.channelRequest == null) {
				this.channelRequest = new PackageChannelRequest.PackageChannelRequestBuilder(this.streamName)
					.id(null)
					.build();
			} else {
				this.channelRequest = channelRequest;
			}
			return this;
		}

		public MediaPackageRequestBuilder originEndpointRequest(
			PackageOriginEndpointRequest originEndpointRequest
		) throws VideoValidationException {
			if (originEndpointRequest == null) {
				this.originEndpointRequest = new PackageOriginEndpointRequest
					.PackageOriginEndpointRequestBuilder(this.streamName).build();
			} else {
				this.originEndpointRequest = originEndpointRequest;
			}
			return this;
		}

		public MediaPackageRequest build() {
			return new MediaPackageRequest(this);
		}
	}
}
