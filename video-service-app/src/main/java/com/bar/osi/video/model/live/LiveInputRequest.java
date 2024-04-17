package com.bar.osi.video.model.live;

import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.services.medialive.model.InputType;

@Getter
@ToString
public class LiveInputRequest {

	/** Input name - required.*/
	private final String name;

	/** Input type - required.*/
	private final InputType inputType;

	/** Use Amazon VPC to specify subnets for RTP or RTMP_PUSH input types. */
	private final Boolean networkModeVpc;

	/** Choose an input security group to use with your RTP or RTMP_PUSH input types.*/
	private final List<String> inputSecurityGroups;

	/** MediaLive Channel Input Specifications. */
	private final LiveInputSpecification liveInputSpecification;

	/** STANDARD_INPUT for 2 redundant input pipelines. SINGLE_INPUT for one input pipeline.*/
	private final List<LiveInputDestination> inputDestinations;

	private LiveInputRequest(LiveInputRequestBuilder builder) {
		this.name = builder.name;
		this.inputType = builder.inputType;
		this.networkModeVpc = builder.networkModeVpc;
		this.inputSecurityGroups = builder.inputSecurityGroups;
		this.liveInputSpecification = builder.liveInputSpecification;
		this.inputDestinations = builder.inputDestinations;
	}

	public static class LiveInputRequestBuilder {
		/** The Name of the inbound Stream.*/
		private final String streamName;

		/** Input name - required.*/
		private String name;

		/** Input type - required.*/
		private InputType inputType;

		/** Use Amazon VPC to specify subnets for RTP or RTMP_PUSH input types. */
		private Boolean networkModeVpc;

		/** Choose an input security group to use with your RTP or RTMP_PUSH input types.*/
		private List<String> inputSecurityGroups;

		/** MediaLive Channel Input Specifications. */
		private LiveInputSpecification liveInputSpecification;

		/** STANDARD_INPUT for 2 redundant input pipelines. SINGLE_INPUT for one input pipeline.*/
		private List<LiveInputDestination> inputDestinations;

		public LiveInputRequestBuilder(String streamName) {
			this.streamName = streamName;
		}

		public LiveInputRequestBuilder name(String name) {
			if (name == null) {
				this.name = LivePrefix.LIVE_INPUT.getPrefix() + streamName;
			} else {
				this.name = LivePrefix.LIVE_INPUT.getPrefix() + streamName + LivePrefix.SEPARATOR.getPrefix() + name;
			}
			return this;
		}

		public LiveInputRequestBuilder inputType(InputType inputType) {
			this.inputType = Objects.requireNonNullElse(inputType, InputType.RTMP_PUSH);
			return this;
		}

		public LiveInputRequestBuilder networkModeVpc(Boolean networkModeVpc) {
			this.networkModeVpc = Objects.requireNonNullElse(networkModeVpc, Boolean.FALSE);
			return this;
		}

		public LiveInputRequestBuilder inputSecurityGroups(List<String> inputSecurityGroups) {
			this.inputSecurityGroups = inputSecurityGroups;
			return this;
		}

		public LiveInputRequestBuilder liveInputSpecification(LiveInputSpecification liveInputSpecification) {
			if (liveInputSpecification == null) {
				this.liveInputSpecification = new LiveInputSpecification.LiveInputSpecificationBuilder().build();
			} else {
				this.liveInputSpecification = liveInputSpecification;
			}
			return this;
		}

		public LiveInputRequestBuilder inputDestinations(List<LiveInputDestination> inputDestinations) {
			this.inputDestinations = inputDestinations;
			return this;
		}

		public LiveInputRequest build() {
			LiveInputRequest liveInputRequest = new LiveInputRequest(this);
			validate();
			return liveInputRequest;
		}

		private void validate() {
			if (name == null) {
				this.name = LivePrefix.LIVE_INPUT.getPrefix() + streamName;
			}
			this.inputType = Objects.requireNonNullElse(inputType, InputType.RTMP_PUSH);
			this.networkModeVpc = Objects.requireNonNullElse(networkModeVpc, Boolean.FALSE);
		}
	}
}