/*******************************************************************************
 * SBIR DATA RIGHTS
 *
 * Funding Agreement No. 21436572
 * Award Date: 1 Aug 2019
 * SBIR Protection Period: 1 Aug 2019 - 31 July 2044
 * FEDSIM Project No. 47QFCA21Z1045
 * Contract No. 47QFCA-19C-0016
 * SBIR Awardee: Bluestaq LLC
 * Contractor Address: 2 N Cascade Ave, Colorado Springs, CO 80903
 *
 * The Government's rights to use, modify, reproduce, release, perform, display,
 * or disclose technical data or computer software marked with this legend are
 * restricted during the period shown as provided in Article 12 of the listed
 * agreement-Small Business Innovation Research (SBIR) Data Rights. No
 * restrictions apply after the expiration date shown above. Any reproduction of
 * technical data, computer software, or portions thereof marked with this legend
 * must also reproduce the markings.
 *
 * Article 12 of the listed agreement states (in part) that the Government will
 * refrain from disclosing SBIR data outside the Government (except support
 * contractors working in an advice and assistance role) and especially to
 * competitors of the Contractor, or from using the information to produce
 * future technical procurement specifications that could harm the Contractor.
 ******************************************************************************/

package com.bar.osi.video.service.impl;

import com.bar.osi.video.config.aws.AwsConfig;
import com.bar.osi.video.model.pkg.MediaPackageChannel;
import com.bar.osi.video.model.pkg.MediaPackageRequest;
import com.bar.osi.video.model.pkg.PackageChannelRequest;
import com.bar.osi.video.model.pkg.PackageOriginEndpointRequest;
import com.bar.osi.video.service.exceptions.VideoValidationException;
import com.bar.osi.video.service.exceptions.VideoServiceException;
import com.bar.osi.video.test.VideoTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import software.amazon.awssdk.services.mediapackage.model.CreateChannelResponse;
import software.amazon.awssdk.services.mediapackage.model.CreateOriginEndpointResponse;
import software.amazon.awssdk.services.mediapackage.model.HlsIngest;
import software.amazon.awssdk.services.mediapackage.model.HlsPackage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@SpringBootTest
class MediaPackageServiceTests extends VideoTest {


	@Autowired
	AwsConfig awsConfig;

	@Autowired
	MediaPackageServiceImpl mediaPackageService;

	@Test
	void whenCreateChannel_givenChannelRequest_thenCreateChannel(TestInfo testInfo) throws VideoServiceException {
		logStarting(testInfo, log);
		MediaPackageChannel packageChannel = null;
		try {

			//given
			MediaPackageRequest packageRequest = buildMediaPackageRequest();
			PackageChannelRequest packageChannelRequest = packageRequest.getChannelRequest();
			PackageOriginEndpointRequest pkgOriginEndpointRequest = packageRequest.getOriginEndpointRequest();

			//when
			packageChannel = mediaPackageService.createChannel(packageRequest);

			//then
			assertNotNull(packageChannel);
			assertNotNull(packageChannel.getCreateChannelResponse());
			CreateChannelResponse actualChannelResponse = packageChannel.getCreateChannelResponse();
			assertEquals(packageChannelRequest.getId(), actualChannelResponse.id());
			assertEquals(packageChannelRequest.getDescription(), actualChannelResponse.description());
			assertNotNull(actualChannelResponse.hlsIngest());
			HlsIngest actualHlsIngest = actualChannelResponse.hlsIngest();
			assertNotNull(actualHlsIngest.ingestEndpoints());
			assertTrue(actualHlsIngest.hasIngestEndpoints());

			assertNotNull(packageChannel.getCreateOriginEndpointResponse());
			CreateOriginEndpointResponse actualOriginEndpoint = packageChannel.getCreateOriginEndpointResponse();
			assertEquals(pkgOriginEndpointRequest.getId(), actualOriginEndpoint.id());
			assertEquals(pkgOriginEndpointRequest.getDescription(), actualOriginEndpoint.description());
			assertEquals(pkgOriginEndpointRequest.getManifestName(), actualOriginEndpoint.manifestName());
			assertEquals(172800, actualOriginEndpoint.startoverWindowSeconds());
			assertEquals(0, actualOriginEndpoint.timeDelaySeconds());
			assertNotNull(actualOriginEndpoint.url());
			assertTrue(actualOriginEndpoint.url().contains(actualOriginEndpoint.manifestName()));

			assertNotNull(actualOriginEndpoint.hlsPackage());
			HlsPackage actualHlsPackage = actualOriginEndpoint.hlsPackage();
			assertEquals(6, actualHlsPackage.segmentDurationSeconds());
			assertEquals(60, actualHlsPackage.playlistWindowSeconds());

		} catch (VideoValidationException | VideoServiceException e) {
			log.error(e.getMessage());
			fail(e.getMessage());
		} finally {
			if (packageChannel != null) {
				mediaPackageService.deleteChannel(packageChannel);
			}
			logEnded(testInfo, log);
		}
	}

	@Test
	void whenDeleteChannel_givenChannel_thenDeleteChannel(TestInfo testInfo) {
		logStarting(testInfo, log);
		try {
			//given
			MediaPackageRequest packageRequest = buildMediaPackageRequest();
			MediaPackageChannel channel = mediaPackageService.createChannel(packageRequest);
			assertNotNull(channel);
			//TODO Add List, assert it exists.

			//when
			mediaPackageService.deleteChannel(channel);

			//then
			//TODO After adding List then assert it is gone.
		} catch (VideoValidationException | VideoServiceException e) {
			log.error(e.getMessage());
			fail(e.getMessage());
		}
		logEnded(testInfo, log);
	}
}
