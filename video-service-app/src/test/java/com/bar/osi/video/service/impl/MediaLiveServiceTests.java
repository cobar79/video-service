package com.bar.osi.video.service.impl;

import com.bar.osi.video.config.aws.AwsConfig;
import com.bar.osi.video.model.live.LiveRequest;
import com.bar.osi.video.model.pkg.MediaPackageChannel;
import com.bar.osi.video.model.pkg.MediaPackageRequest;
import com.bar.osi.video.service.MediaLiveService;
import com.bar.osi.video.service.MediaPackageService;
import com.bar.osi.video.service.exceptions.VideoServiceException;
import com.bar.osi.video.service.exceptions.VideoValidationException;
import com.bar.osi.video.test.VideoTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import software.amazon.awssdk.services.medialive.model.Channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@SpringBootTest
class MediaLiveServiceTests extends VideoTest {


	@Autowired
	AwsConfig awsConfig;

	@Autowired
	private MediaLiveService mediaLiveService;

	@Autowired
	private MediaPackageService mediaPackageService;

	@Test
	void whenCreateChannel_givenChannelRequest_thenCreateChannel(TestInfo testInfo) throws VideoServiceException {
		logStarting(testInfo, log);
		MediaPackageChannel pkgChannel = null;
		Channel liveChannel = null;
		try {
			//given
			MediaPackageRequest packageRequest = buildMediaPackageRequest();
			pkgChannel = mediaPackageService.createChannel(packageRequest);
			LiveRequest liveRequest = buildMediaLiveRequest();
			liveRequest.setMediaPackageChannelArn(pkgChannel.getCreateChannelResponse().arn());

			//when
			liveChannel = mediaLiveService.createChannel(liveRequest, pkgChannel);
			assertNotNull(liveChannel);

		} catch (VideoValidationException | VideoServiceException e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		} finally {
			if (pkgChannel != null) {
				mediaPackageService.deleteChannel(pkgChannel);
			}
			if (liveChannel != null) {
				mediaLiveService.deleteChannel(liveChannel);
			}
			logEnded(testInfo, log);
		}
	}

}
