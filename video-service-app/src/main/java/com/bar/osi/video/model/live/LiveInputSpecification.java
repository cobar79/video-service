package com.bar.osi.video.model.live;

import java.util.Objects;

import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.services.medialive.model.InputCodec;
import software.amazon.awssdk.services.medialive.model.InputResolution;

@Getter
@ToString
public final class LiveInputSpecification {

	/** MediaLive Input Codec.*/
	private final InputCodec inputCodec;

	/** MediaLive Input Bitrate.*/
	private final MaximumInputBitrate maximumInputBitrate;

	/** MediaLive Input Resolution. */
	private final InputResolution inputResolution;

	private LiveInputSpecification(LiveInputSpecificationBuilder builder) {
		this.inputCodec = builder.inputCodec;
		this.maximumInputBitrate = builder.maximumInputBitrate;
		this.inputResolution = builder.inputResolution;
	}

	public static class LiveInputSpecificationBuilder {

		/** MediaLive Input Codec.*/
		private InputCodec inputCodec;

		/** MediaLive Input Bitrate.*/
		private MaximumInputBitrate maximumInputBitrate;

		/** MediaLive Input Resolution. */
		private InputResolution inputResolution;

		public LiveInputSpecificationBuilder inputCodec(InputCodec inputCodec) {
			this.inputCodec = Objects.requireNonNullElse(inputCodec, InputCodec.AVC);
			return this;
		}

		public LiveInputSpecificationBuilder maximumInputBitrate(MaximumInputBitrate maximumInputBitrate) {
			this.maximumInputBitrate = Objects.requireNonNullElse(maximumInputBitrate, MaximumInputBitrate.MAX_20_MBPS);
			return this;
		}

		public LiveInputSpecificationBuilder inputResolution(InputResolution inputResolution) {
			this.inputResolution = Objects.requireNonNullElse(inputResolution, InputResolution.HD);
			return this;
		}

		public LiveInputSpecification build() {
			LiveInputSpecification liveInputSpecification =  new LiveInputSpecification(this);
			validate();
			return liveInputSpecification;
		}

		private void validate() {
			this.inputCodec = Objects.requireNonNullElse(inputCodec, InputCodec.AVC);
			this.maximumInputBitrate = Objects.requireNonNullElse(maximumInputBitrate, MaximumInputBitrate.MAX_20_MBPS);
			this.inputResolution = Objects.requireNonNullElse(inputResolution, InputResolution.HD);
		}

	}

}
