package com.bar.osi.video.service.impl;

import com.bar.osi.video.config.aws.AwsConfig;
import com.bar.osi.video.model.pkg.MediaPackageChannel;
import com.bar.osi.video.model.pkg.MediaPackageRequest;
import com.bar.osi.video.service.exceptions.VideoServiceException;
import com.bar.osi.video.service.exceptions.VideoValidationException;
import com.bar.osi.video.test.VideoTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.mediapackage.model.CreateOriginEndpointResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@SpringBootTest
class CloudFrontServiceTests extends VideoTest {

	@Autowired
	AwsConfig awsConfig;

	@Autowired
	CloudFrontServiceImpl cloudFrontService;

	@Autowired
	MediaPackageServiceImpl mediaPackageService;

	@Test
	void whenCreateDistribution_givenPackage_thenCreate(TestInfo testInfo) throws VideoServiceException {
		logStarting(testInfo, log);

		MediaPackageChannel packageChannel = null;
		Distribution actualDistribution = null;
		try {

			//given
			MediaPackageRequest packageRequest = buildMediaPackageRequest();
			packageChannel = mediaPackageService.createChannel(packageRequest);
			assertNotNull(packageChannel);
			assertNotNull(packageChannel.getCreateChannelResponse());
			assertNotNull(packageChannel.getCreateOriginEndpointResponse());
			CreateOriginEndpointResponse givenOriginEndpoint = packageChannel.getCreateOriginEndpointResponse();
			assertNotNull(givenOriginEndpoint.url());
			//when
			actualDistribution = cloudFrontService.createDistribution(givenOriginEndpoint);
			//then
			assertNotNull(actualDistribution);
			assertNotNull(actualDistribution.distributionConfig());
			assertNotNull(actualDistribution.distributionConfig().origins());


		} catch (VideoValidationException | VideoServiceException e) {
			log.error(e.getMessage());
			fail(e.getMessage());
		} finally {
			if (packageChannel != null) {
				mediaPackageService.deleteChannel(packageChannel);
			}
			if (actualDistribution != null) {
				cloudFrontService.deleteDistribution(actualDistribution.id());
			}
			logEnded(testInfo, log);
		}
	}

	@Test
	void whenFindDistribution_givenValidId_thenReturnDistribution(TestInfo testInfo) throws VideoServiceException {
		logStarting(testInfo, log);
		MediaPackageChannel packageChannel = null;
		Distribution givenDistribution = null;
		try {
			//given
			MediaPackageRequest packageRequest = buildMediaPackageRequest();
			packageChannel = mediaPackageService.createChannel(packageRequest);
			assertNotNull(packageChannel);
			assertNotNull(packageChannel.getCreateChannelResponse());
			assertNotNull(packageChannel.getCreateOriginEndpointResponse());

			CreateOriginEndpointResponse givenOriginEndpoint = packageChannel.getCreateOriginEndpointResponse();
			assertNotNull(givenOriginEndpoint.url());
			givenDistribution = cloudFrontService.createDistribution(givenOriginEndpoint);
			assertNotNull(givenDistribution);
			assertNotNull(givenDistribution.distributionConfig());
			assertNotNull(givenDistribution.distributionConfig().origins());

			//when
			Distribution actualDistribution = cloudFrontService.findDistribution(givenDistribution.id());
			//then
			assertNotNull(actualDistribution);
			assertEquals(givenDistribution.id(), actualDistribution.id());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		} finally {
			if (packageChannel != null) {
				mediaPackageService.deleteChannel(packageChannel);
			}
			if (givenDistribution != null) {
				cloudFrontService.deleteDistribution(givenDistribution.id());
			}
			logEnded(testInfo, log);
		}
	}
}
