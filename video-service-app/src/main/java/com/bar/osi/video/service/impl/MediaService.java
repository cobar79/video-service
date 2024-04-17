package com.bar.osi.video.service.impl;

import java.net.URI;
import java.net.URISyntaxException;

import com.bar.osi.video.config.aws.AwsConfig;
import com.bar.osi.video.config.aws.AwsCredentialsConfig;
import com.bar.osi.video.service.exceptions.ErrorReason;
import com.bar.osi.video.service.exceptions.VideoServiceRuntimeException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.medialive.MediaLiveClient;
import software.amazon.awssdk.services.mediapackage.MediaPackageClient;

public class MediaService {

	MediaPackageClient buildMediaPackageClient(AwsConfig awsConfig, AwsCredentialsConfig awsCredentialsConfig) {
		if (awsConfig.getLocalstackUrl() != null) {
			return MediaPackageClient
				.builder()
				.region(Region.of(awsConfig.getRegion()))
				.endpointOverride(buildUri(awsConfig.getLocalstackUrl()))
				.credentialsProvider(awsCredentialsConfig.testingCredentialProvider())
				.build();
		} else {
			return MediaPackageClient
				.builder()
				.region(Region.of(awsConfig.getRegion()))
				.credentialsProvider(awsCredentialsConfig.environmentCredentialProvider())
				.build();
		}
	}

	MediaLiveClient buildMediaLiveClient(AwsConfig awsConfig, AwsCredentialsConfig awsCredentialsConfig) {
		if (awsConfig.getLocalstackUrl() != null) {
			return MediaLiveClient
				.builder()
				.region(Region.of(awsConfig.getRegion()))
				.endpointOverride(buildUri(awsConfig.getLocalstackUrl()))
				.credentialsProvider(awsCredentialsConfig.testingCredentialProvider())
				.build();
		} else {
			return MediaLiveClient
				.builder()
				.region(Region.of(awsConfig.getRegion()))
				.credentialsProvider(awsCredentialsConfig.environmentCredentialProvider())
				.build();
		}
	}


	URI buildUri(String url) {
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			throw new VideoServiceRuntimeException(e.getMessage(), ErrorReason.SERVICE_EXCEPTION);
		}
	}

}
