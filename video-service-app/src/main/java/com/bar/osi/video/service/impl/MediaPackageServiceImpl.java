package com.bar.osi.video.service.impl;

import com.bar.osi.video.config.aws.AwsConfig;
import com.bar.osi.video.config.aws.AwsCredentialsConfig;
import com.bar.osi.video.model.pkg.HlsPackageRequest;
import com.bar.osi.video.model.pkg.MediaPackageChannel;
import com.bar.osi.video.model.pkg.MediaPackageRequest;
import com.bar.osi.video.model.pkg.PackageChannelRequest;
import com.bar.osi.video.model.pkg.PackageOriginEndpointRequest;
import com.bar.osi.video.model.pkg.PackagePrefix;
import com.bar.osi.video.service.MediaPackageService;
import com.bar.osi.video.service.exceptions.ErrorReason;
import com.bar.osi.video.service.exceptions.VideoServiceException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.mediapackage.MediaPackageClient;
import software.amazon.awssdk.services.mediapackage.model.CreateChannelRequest;
import software.amazon.awssdk.services.mediapackage.model.CreateChannelResponse;
import software.amazon.awssdk.services.mediapackage.model.CreateOriginEndpointRequest;
import software.amazon.awssdk.services.mediapackage.model.CreateOriginEndpointResponse;
import software.amazon.awssdk.services.mediapackage.model.DeleteChannelRequest;
import software.amazon.awssdk.services.mediapackage.model.DeleteChannelResponse;
import software.amazon.awssdk.services.mediapackage.model.DeleteOriginEndpointRequest;
import software.amazon.awssdk.services.mediapackage.model.DeleteOriginEndpointResponse;
import software.amazon.awssdk.services.mediapackage.model.HlsPackage;
import software.amazon.awssdk.services.mediapackage.model.MediaPackageException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MediaPackageServiceImpl extends MediaService implements MediaPackageService {

	/** AWS MediaPackage Client. */
	private MediaPackageClient packageClient;

	/** AWS Configurations. */
	private final AwsConfig awsConfig;

	/** AWS Credential Configurations.*/
	private final AwsCredentialsConfig awsCredentialsConfig;

	@Autowired
	MediaPackageServiceImpl(
		AwsConfig awsConfig,
		AwsCredentialsConfig awsCredentialsConfig
	) {
		this.awsConfig = awsConfig;
		this.awsCredentialsConfig = awsCredentialsConfig;
	}

	@Override
	public MediaPackageChannel createChannel(MediaPackageRequest packageRequest) throws VideoServiceException {
		buildMediaClient();
		try {
			CreateChannelResponse createChannelResponse =
				packageClient.createChannel(buildCreateRequest(packageRequest.getChannelRequest()));
			log.debug(createChannelResponse.toString());
			CreateOriginEndpointRequest createOriginEndpointRequest =
				buildOriginEndpoint(createChannelResponse.id(), packageRequest.getOriginEndpointRequest());

			CreateOriginEndpointResponse originEndpointResponse = packageClient.createOriginEndpoint(createOriginEndpointRequest);
			log.info(originEndpointResponse.toString());
			return new MediaPackageChannel(createChannelResponse, originEndpointResponse);
		} catch (MediaPackageException e) {
			log.error(e.getMessage(), e);
			throw new VideoServiceException(e.getMessage(), ErrorReason.SERVICE_EXCEPTION);
		}
	}

	@Override
	public void deleteChannel(MediaPackageChannel packageChannel) throws VideoServiceException {
		try {
			DeleteOriginEndpointResponse originEndpointResponse =
				deleteChannelEndpoints(packageChannel.getCreateOriginEndpointResponse());
			log.info(originEndpointResponse.toString());
			DeleteChannelResponse channelResponse = deleteChannel(packageChannel.getCreateChannelResponse());
			log.info(channelResponse.toString());
		} catch (MediaPackageException e) {
			log.error(e.getMessage(), e);
			throw new VideoServiceException(e.getMessage(), ErrorReason.SERVICE_EXCEPTION);
		}
	}

	private DeleteOriginEndpointResponse deleteChannelEndpoints(CreateOriginEndpointResponse originEndpoint) throws VideoServiceException {
		try {
			return packageClient.deleteOriginEndpoint(DeleteOriginEndpointRequest.builder().id(originEndpoint.id()).build());
		} catch (MediaPackageException e) {
			log.error(e.getMessage(), e);
			throw new VideoServiceException(e.getMessage(), ErrorReason.SERVICE_EXCEPTION);
		}
	}

	private DeleteChannelResponse deleteChannel(CreateChannelResponse channelResponse) throws VideoServiceException {
		try {
			return packageClient.deleteChannel(DeleteChannelRequest.builder().id(channelResponse.id()).build());
		} catch (MediaPackageException e) {
			log.error(e.getMessage(), e);
			throw new VideoServiceException(e.getMessage(), ErrorReason.SERVICE_EXCEPTION);
		}
	}

	private void buildMediaClient() {
		if (packageClient == null) {
			this.packageClient = buildMediaPackageClient(awsConfig, awsCredentialsConfig);
		}
	}

	private CreateChannelRequest buildCreateRequest(PackageChannelRequest channelRequest) {
		return CreateChannelRequest
			.builder()
			.id(channelRequest != null ? channelRequest.getId() : PackagePrefix.CHANNEL.getPrefix())
			.description(channelRequest != null ? channelRequest.getDescription() : "Media Package")
			.build();
	}

	private CreateOriginEndpointRequest buildOriginEndpoint(String channelId, PackageOriginEndpointRequest mpoeRequest) {
		return CreateOriginEndpointRequest
			.builder()
			.id(mpoeRequest.getId())
			.channelId(channelId)
			.description(mpoeRequest.getDescription())
			.manifestName(mpoeRequest.getManifestName())
			.startoverWindowSeconds(mpoeRequest.getStartOverWindowSeconds()) //2-days default if not passed it in.
			.timeDelaySeconds(mpoeRequest.getTimeDelaySeconds())
			.hlsPackage(buildHlsPackage(mpoeRequest.getHlsPackageRequest()))
			.build();
	}

	private HlsPackage buildHlsPackage(HlsPackageRequest hlsPackageRequest) {
		return HlsPackage
			.builder()
			.segmentDurationSeconds(hlsPackageRequest.getSegmentDurationSeconds())
			.playlistWindowSeconds(hlsPackageRequest.getPlaylistWindowSeconds())
			.build();
	}

}
