package com.bar.osi.video.test;

import java.util.List;

import com.bar.osi.video.model.live.ChannelRequest;
import com.bar.osi.video.model.live.LiveInputDestination;
import com.bar.osi.video.model.live.LiveInputRequest;
import com.bar.osi.video.model.live.LiveRequest;
import com.bar.osi.video.model.pkg.HlsPackageRequest;
import com.bar.osi.video.model.pkg.MediaPackageRequest;
import com.bar.osi.video.model.pkg.PackageChannelRequest;
import com.bar.osi.video.model.pkg.PackageOriginEndpointRequest;
import com.bar.osi.video.service.exceptions.VideoValidationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import software.amazon.awssdk.services.medialive.model.ChannelClass;
import software.amazon.awssdk.services.medialive.model.InputType;

import org.springframework.test.context.TestPropertySource;

@Slf4j
@TestPropertySource(properties = {"aws.region=us-east-1"})
public class VideoTest {

	public static final String STARTING = "STARTING:: ";
	public static final String ENDED = "ENDED:: ";

	public static final String SHOULD_HAVE_THROWN_EXCEPTION = "This test should have thrown exception";

	public String TEST_STREAM_NAME = "test-01";

	public void logStarting(TestInfo testInfo, Logger log) {
		testInfo.getTestMethod().ifPresent(value -> log.info(STARTING + value.getName()));
	}

	public void logEnded(TestInfo testInfo, Logger log) {
		testInfo.getTestMethod().ifPresent(value -> log.info(ENDED + value.getName()));
	}


	public MediaPackageRequest buildMediaPackageRequest() throws VideoValidationException {

		PackageChannelRequest packageChannelRequest = new PackageChannelRequest
			.PackageChannelRequestBuilder(TEST_STREAM_NAME)
			.id(TEST_STREAM_NAME)
			.description("TEST-channel-description")
			.build();

		HlsPackageRequest hlsPackageRequest = new HlsPackageRequest
			.HlsPackageRequestBuilder()
			.playlistWindowSeconds(null)
			.segmentDurationSeconds(null)
			.build();

		PackageOriginEndpointRequest pkgOriginEndpointRequest =
			new PackageOriginEndpointRequest.PackageOriginEndpointRequestBuilder(TEST_STREAM_NAME)
				.id(TEST_STREAM_NAME)
				.description("TEST:: origin endpoint description")
				.startOverWindowSeconds(null)
				.timeDelaySeconds(null)
				.manifestName(null)
				.hlsPackageRequest(hlsPackageRequest)
				.build();

		return new MediaPackageRequest
			.MediaPackageRequestBuilder(TEST_STREAM_NAME)
			.channelRequest(packageChannelRequest)
			.originEndpointRequest(pkgOriginEndpointRequest)
			.build();
	}

	public LiveRequest buildMediaLiveRequest() {


		LiveInputDestination mliDestination = new LiveInputDestination
			.LiveInputDestinationBuilder(TEST_STREAM_NAME)
			.appName(TEST_STREAM_NAME)
			.appInstance(TEST_STREAM_NAME)
			.build();

		LiveInputRequest liveInputRequest = new LiveInputRequest
			.LiveInputRequestBuilder(TEST_STREAM_NAME)
			.name(TEST_STREAM_NAME)
			.inputType(InputType.RTMP_PUSH)
			.networkModeVpc(Boolean.FALSE)
			.inputSecurityGroups(List.of("0.0.0.0/0"))
			.inputDestinations(List.of(mliDestination))
			.build();

		ChannelRequest channelRequest = new ChannelRequest
			.ChannelRequestBuilder(TEST_STREAM_NAME)
			.name(TEST_STREAM_NAME)
			.channelClass(ChannelClass.SINGLE_PIPELINE)
			.build();

		return new LiveRequest(liveInputRequest, channelRequest, "mediaPkgChannelArn");
	}
}
