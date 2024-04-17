package com.bar.osi.video.model.live;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class LiveInputDestination {

	/** Input Destination Application Name.*/
	private final String appName;

	/** Input Destination Application Instance.*/
	private final String appInstance;

	private LiveInputDestination(LiveInputDestinationBuilder builder) {
		this.appName = builder.appName;
		this.appInstance = builder.appInstance;
	}

	public static class LiveInputDestinationBuilder {

		/** The Name of the inbound Stream.*/
		private final String streamName;

		/** Input Destination Application Name.*/
		private String appName;

		/** Input Destination Application Instance.*/
		private String appInstance;

		public LiveInputDestinationBuilder(String streamName) {
			this.streamName = streamName;
		}

		public LiveInputDestinationBuilder appName(String appName) {
			if (appName == null) {
				this.appName = LivePrefix.DESTINATION_APP + streamName;
			} else {
				this.appName = LivePrefix.DESTINATION_APP + LivePrefix.SEPARATOR.getPrefix() + appName;
			}
			return this;
		}

		public LiveInputDestinationBuilder appInstance(String appInstance) {
			if (appInstance == null) {
				this.appInstance = LivePrefix.DESTINATION_APP_INSTANCE + streamName;
			} else {
				this.appInstance = LivePrefix.DESTINATION_APP_INSTANCE.getPrefix() + appInstance;
			}
			return this;
		}

		public LiveInputDestination build() {
			LiveInputDestination inputDestination = new LiveInputDestination(this);
			validate();
			return inputDestination;
		}

		private void validate() {
			if (this.appName == null) {
				appName(null);
			}
			if (this.appInstance == null) {
				appInstance(null);
			}
		}

	}

}
