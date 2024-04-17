package com.bar.osi.video.model.live;

import java.util.Objects;

import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.services.medialive.model.ChannelClass;

@Getter
@ToString
public final class ChannelRequest {

	/** Channel Name.*/
	private final String name;

	/** A standard channel provides two redundant encoder pipelines, a single pipeline channel creates only a single encoder pipeline.*/
	private final ChannelClass channelClass;

	private ChannelRequest(ChannelRequestBuilder builder) {
		this.name = builder.name;
		this.channelClass = builder.channelClass;
	}


	public static class ChannelRequestBuilder {
		/** The Name of the inbound Stream.*/
		private final String streamName;

		/** Channel Name.*/
		private String name;

		/** A standard channel provides two redundant encoder pipelines, a single pipeline channel creates only a single encoder pipeline.*/
		private ChannelClass channelClass;

		public ChannelRequestBuilder(String streamName) {
			this.streamName = streamName;
		}

		public ChannelRequestBuilder name(String name) {
			if (name == null) {
				this.name = LivePrefix.CHANNEL_NAME.getPrefix() + streamName;
			} else {
				this.name = LivePrefix.CHANNEL_NAME.getPrefix() + streamName + LivePrefix.SEPARATOR.getPrefix() + name;
			}
			return this;
		}

		public ChannelRequestBuilder channelClass(ChannelClass channelClass) {
			this.channelClass = Objects.requireNonNullElse(channelClass, ChannelClass.SINGLE_PIPELINE);
			return this;
		}

		public ChannelRequest build() {
			ChannelRequest request = new ChannelRequest(this);
			validate();
			return request;
		}

		private void validate() {
			if (this.name == null) {
				name(null);
			}
			if (this.channelClass == null) {
				this.channelClass = ChannelClass.SINGLE_PIPELINE;
			}
		}
	}
}
