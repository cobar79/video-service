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
