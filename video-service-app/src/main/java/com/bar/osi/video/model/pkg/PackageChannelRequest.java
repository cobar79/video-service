package com.bar.osi.video.model.pkg;

import java.util.List;

import com.bar.osi.video.service.exceptions.VideoValidationException;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class PackageChannelRequest {

	/** The channel identifier that you use for API and console interactions. */
	private final String id;

	/** A short text description of the channel. */
	private final String description;

	private PackageChannelRequest(PackageChannelRequestBuilder builder) {
		this.id = builder.id;
		this.description = builder.description;
	}


	public static class PackageChannelRequestBuilder {

		/** The Name of the inbound Stream.*/
		private final String streamName;

		/** The channel identifier that you use for API and console interactions. */
		private String id;

		/** A short text description of the channel. */
		private String description;

		public PackageChannelRequestBuilder(String streamName) {
			this.streamName = streamName;
		}

		//TODO - Package Type? Shows in channel request, ends up in mpoe as package type?

		public PackageChannelRequestBuilder id(String id) {
			if (id == null) {
				this.id = PackagePrefix.CHANNEL.getPrefix() + streamName;
			} else {
				this.id = PackagePrefix.CHANNEL.getPrefix() + streamName + PackagePrefix.SEPARATOR.getPrefix() + id;
			}
			return this;
		}

		public PackageChannelRequestBuilder description(String description) {
			this.description = description;
			return this;
		}

		public PackageChannelRequest build() throws VideoValidationException {
			PackageChannelRequest packageChannelRequest = new PackageChannelRequest(this);
			validate(packageChannelRequest);
			return packageChannelRequest;
		}

		private void validate(PackageChannelRequest packageChannelRequest) throws VideoValidationException {
			if (packageChannelRequest.id == null) {
				throw new VideoValidationException("PackageChannelRequest is invalid", List.of("id is required"));
			}
		}
	}
}
