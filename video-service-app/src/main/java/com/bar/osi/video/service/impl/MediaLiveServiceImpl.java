package com.bar.osi.video.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.bar.osi.video.config.aws.AwsConfig;
import com.bar.osi.video.config.aws.AwsCredentialsConfig;
import com.bar.osi.video.config.aws.MediaLiveConfig;
import com.bar.osi.video.model.live.ChannelRequest;
import com.bar.osi.video.model.live.LiveInputRequest;
import com.bar.osi.video.model.live.LiveRequest;
import com.bar.osi.video.model.live.MaximumInputBitrate;
import com.bar.osi.video.model.pkg.MediaPackageChannel;
import com.bar.osi.video.service.MediaLiveService;
import com.bar.osi.video.service.S3Service;
import com.bar.osi.video.service.exceptions.ErrorReason;
import com.bar.osi.video.service.exceptions.VideoServiceException;
import com.bar.osi.video.service.exceptions.VideoServiceRuntimeException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.services.mediaconvert.model.AacRawFormat;
import software.amazon.awssdk.services.medialive.MediaLiveClient;
import software.amazon.awssdk.services.medialive.model.AacCodingMode;
import software.amazon.awssdk.services.medialive.model.AacInputType;
import software.amazon.awssdk.services.medialive.model.AacProfile;
import software.amazon.awssdk.services.medialive.model.AacRateControlMode;
import software.amazon.awssdk.services.medialive.model.AacSettings;
import software.amazon.awssdk.services.medialive.model.AacSpec;
import software.amazon.awssdk.services.medialive.model.AfdSignaling;
import software.amazon.awssdk.services.medialive.model.AudioCodecSettings;
import software.amazon.awssdk.services.medialive.model.AudioDescription;
import software.amazon.awssdk.services.medialive.model.AudioDescriptionAudioTypeControl;
import software.amazon.awssdk.services.medialive.model.AudioDescriptionLanguageCodeControl;
import software.amazon.awssdk.services.medialive.model.Channel;
import software.amazon.awssdk.services.medialive.model.CreateChannelRequest;
import software.amazon.awssdk.services.medialive.model.CreateChannelResponse;
import software.amazon.awssdk.services.medialive.model.CreateInputRequest;
import software.amazon.awssdk.services.medialive.model.CreateInputResponse;
import software.amazon.awssdk.services.medialive.model.CreateInputSecurityGroupRequest;
import software.amazon.awssdk.services.medialive.model.CreateInputSecurityGroupResponse;
import software.amazon.awssdk.services.medialive.model.DeleteChannelRequest;
import software.amazon.awssdk.services.medialive.model.DeleteInputSecurityGroupRequest;
import software.amazon.awssdk.services.medialive.model.DeleteInputSecurityGroupResponse;
import software.amazon.awssdk.services.medialive.model.DescribeInputResponse;
import software.amazon.awssdk.services.medialive.model.EncoderSettings;
import software.amazon.awssdk.services.medialive.model.H264AdaptiveQuantization;
import software.amazon.awssdk.services.medialive.model.H264ColorMetadata;
import software.amazon.awssdk.services.medialive.model.H264EntropyEncoding;
import software.amazon.awssdk.services.medialive.model.H264FlickerAq;
import software.amazon.awssdk.services.medialive.model.H264ForceFieldPictures;
import software.amazon.awssdk.services.medialive.model.H264FramerateControl;
import software.amazon.awssdk.services.medialive.model.H264GopBReference;
import software.amazon.awssdk.services.medialive.model.H264GopSizeUnits;
import software.amazon.awssdk.services.medialive.model.H264Level;
import software.amazon.awssdk.services.medialive.model.H264LookAheadRateControl;
import software.amazon.awssdk.services.medialive.model.H264ParControl;
import software.amazon.awssdk.services.medialive.model.H264Profile;
import software.amazon.awssdk.services.medialive.model.H264RateControlMode;
import software.amazon.awssdk.services.medialive.model.H264ScanType;
import software.amazon.awssdk.services.medialive.model.H264SceneChangeDetect;
import software.amazon.awssdk.services.medialive.model.H264SpatialAq;
import software.amazon.awssdk.services.medialive.model.H264SubGopLength;
import software.amazon.awssdk.services.medialive.model.H264Syntax;
import software.amazon.awssdk.services.medialive.model.H264TemporalAq;
import software.amazon.awssdk.services.medialive.model.H264TimecodeInsertionBehavior;
import software.amazon.awssdk.services.medialive.model.InputAttachment;
import software.amazon.awssdk.services.medialive.model.InputCodec;
import software.amazon.awssdk.services.medialive.model.InputDeblockFilter;
import software.amazon.awssdk.services.medialive.model.InputDenoiseFilter;
import software.amazon.awssdk.services.medialive.model.InputDestinationRequest;
import software.amazon.awssdk.services.medialive.model.InputFilter;
import software.amazon.awssdk.services.medialive.model.InputResolution;
import software.amazon.awssdk.services.medialive.model.InputSecurityGroup;
import software.amazon.awssdk.services.medialive.model.InputSettings;
import software.amazon.awssdk.services.medialive.model.InputSourceEndBehavior;
import software.amazon.awssdk.services.medialive.model.InputSpecification;
import software.amazon.awssdk.services.medialive.model.InputType;
import software.amazon.awssdk.services.medialive.model.InputWhitelistRuleCidr;
import software.amazon.awssdk.services.medialive.model.ListInputSecurityGroupsRequest;
import software.amazon.awssdk.services.medialive.model.ListInputSecurityGroupsResponse;
import software.amazon.awssdk.services.medialive.model.M2tsAbsentInputAudioBehavior;
import software.amazon.awssdk.services.medialive.model.M2tsArib;
import software.amazon.awssdk.services.medialive.model.M2tsAribCaptionsPidControl;
import software.amazon.awssdk.services.medialive.model.M2tsAudioBufferModel;
import software.amazon.awssdk.services.medialive.model.M2tsAudioInterval;
import software.amazon.awssdk.services.medialive.model.M2tsAudioStreamType;
import software.amazon.awssdk.services.medialive.model.M2tsBufferModel;
import software.amazon.awssdk.services.medialive.model.M2tsCcDescriptor;
import software.amazon.awssdk.services.medialive.model.M2tsEbifControl;
import software.amazon.awssdk.services.medialive.model.M2tsEbpPlacement;
import software.amazon.awssdk.services.medialive.model.M2tsEsRateInPes;
import software.amazon.awssdk.services.medialive.model.M2tsKlv;
import software.amazon.awssdk.services.medialive.model.M2tsNielsenId3Behavior;
import software.amazon.awssdk.services.medialive.model.M2tsPcrControl;
import software.amazon.awssdk.services.medialive.model.M2tsRateMode;
import software.amazon.awssdk.services.medialive.model.M2tsScte35Control;
import software.amazon.awssdk.services.medialive.model.M2tsSegmentationMarkers;
import software.amazon.awssdk.services.medialive.model.M2tsSegmentationStyle;
import software.amazon.awssdk.services.medialive.model.M2tsTimedMetadataBehavior;
import software.amazon.awssdk.services.medialive.model.MediaLiveException;
import software.amazon.awssdk.services.medialive.model.MediaPackageOutputDestinationSettings;
import software.amazon.awssdk.services.medialive.model.Output;
import software.amazon.awssdk.services.medialive.model.OutputDestination;
import software.amazon.awssdk.services.medialive.model.OutputGroup;
import software.amazon.awssdk.services.medialive.model.TimecodeConfigSource;
import software.amazon.awssdk.services.medialive.model.VideoDescription;
import software.amazon.awssdk.services.medialive.model.VideoDescriptionRespondToAfd;
import software.amazon.awssdk.services.medialive.model.VideoDescriptionScalingBehavior;
import software.amazon.awssdk.services.medialive.waiters.MediaLiveWaiter;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.utils.builder.SdkBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MediaLiveServiceImpl extends MediaService implements MediaLiveService {

	private static final String VIDEO_1080P_30 = "video_1080p30";
	private static final String VIDEO_720P_30 = "video_720p30";
	private static final String VIDEO_480P_30 = "video_480p30";
	private static final String VIDEO_240P_30 = "video_240p30";
	private static final String AUDIO_1 = "audio_1";
	private static final String AUDIO_2 = "audio_2";
	private static final String AUDIO_3 = "audio_3";
	private static final String AUDIO_4 = "audio_4";
	private static final String LOG_FORMAT = "{}:: Package Output ChannelId {}";

	/** MediaLive Client.*/
	private MediaLiveClient mediaLiveClient;

	/** AWS Configurations. */
	private final AwsConfig awsConfig;

	/** AWS Credential Configurations. */
	private final AwsCredentialsConfig awsCredentialsConfig;

	/** MediaLive Configurations. */
	private final MediaLiveConfig mediaLiveConfig;

	/** S3Service. */
	private final S3Service s3Service;

	@Autowired
	public MediaLiveServiceImpl(
		AwsConfig awsConfig,
		AwsCredentialsConfig awsCredentialsConfig,
		MediaLiveConfig mediaLiveConfig,
		S3Service s3Service
	) {
		this.awsConfig = awsConfig;
		this.awsCredentialsConfig = awsCredentialsConfig;
		this.mediaLiveConfig = mediaLiveConfig;
		this.s3Service = s3Service;
	}

	@Override
	public Channel createChannel(LiveRequest liveRequest, MediaPackageChannel pkgChannel) throws VideoServiceException {
		CreateInputSecurityGroupResponse securityGroupResponse = null;
		CreateInputResponse inputResponse = null;
		try {
			buildMediaLiveClient();
			//Create Input
			securityGroupResponse = mediaLiveClient.createInputSecurityGroup(buildInputSecurityGroup(liveRequest.getLiveInputRequest()));
			log.info(securityGroupResponse.toString());

			LiveInputRequest liveInputRequest = liveRequest.getLiveInputRequest();
			inputResponse = mediaLiveClient.createInput(buildCreateInputRequest(liveInputRequest, securityGroupResponse.securityGroup()));

			log.info(inputResponse.toString());

			CreateChannelRequest channelRequest = buildCreateChannelRequest(liveRequest, inputResponse, pkgChannel);
			log.debug(channelRequest.toString());

			CreateChannelResponse channelResponse = mediaLiveClient.createChannel(channelRequest);
			log.info(channelResponse.channel().toString());

			return channelResponse.channel();
		} catch (MediaLiveException e) {
			log.error(e.getMessage(), e);
			cleanUp(securityGroupResponse);
			throw new VideoServiceException(e.getMessage(), ErrorReason.SERVICE_EXCEPTION);
		}
	}

	@Override
	public DescribeInputResponse deleteInput(String inputId) {
		//Deleting it without wait works, but leaves security group blowing up since it is not idle and there isn't
		//a waiter for deleting security group until idle.
//		mediaLiveClient.deleteInput(DeleteInputRequest.builder().inputId(inputId).build());

		DescribeInputResponse describeInputResponse = waitTillInputDetached(inputId);
		log.info("{}:: input state {}", describeInputResponse.id(), describeInputResponse.state());
		DescribeInputResponse deleteResponse;
		try (MediaLiveWaiter mlWaiter = MediaLiveWaiter.builder()
			.client(mediaLiveClient)
			.overrideConfiguration(override -> override
				.waitTimeout(Duration.ofSeconds(120))
				.maxAttempts(21)
				.build())
			.build()
		) {
			ResponseOrException<DescribeInputResponse> responseOrException = mlWaiter
				.waitUntilInputDeleted(builder -> builder.inputId(inputId))
				.matched();
			deleteResponse = responseOrException.response()
				.orElseThrow(() -> new VideoServiceRuntimeException(inputId + ":: not deleted", ErrorReason.SERVICE_EXCEPTION));
			log.info("{}:: input deleted", inputId);
			return deleteResponse;
		}
	}

	@Override
	public DescribeInputResponse waitTillInputDetached(String inputId) {
		log.info("Detaching Input {}", inputId);
		DescribeInputResponse detachInputResponse;
		try (MediaLiveWaiter mlWaiter = MediaLiveWaiter.builder().client(mediaLiveClient).build()) {
			ResponseOrException<DescribeInputResponse> responseOrException = mlWaiter
				.waitUntilInputDetached(builder -> builder.inputId(inputId)).matched();
			detachInputResponse = responseOrException.response()
				.orElseThrow(() -> new VideoServiceRuntimeException(inputId + ":: not be detached", ErrorReason.SERVICE_EXCEPTION));
			log.info("Detached Input {} state {}", detachInputResponse.id(), detachInputResponse.state());
			return detachInputResponse;
		}
	}

	@Override
	public Optional<DeleteInputSecurityGroupResponse> deleteInputSecurityGroup(String securityGroupId) {
		Optional<InputSecurityGroup> optSecurityGroup = findSecurityGroup(securityGroupId);
		if (optSecurityGroup.isPresent() && optSecurityGroup.get().hasInputs()) {
			InputSecurityGroup inputSecurityGroup = optSecurityGroup.get();
			log.info("Deleting {} inputs", inputSecurityGroup.inputs().size());
			inputSecurityGroup.inputs().forEach(this::deleteInput);
			DeleteInputSecurityGroupRequest deleteInputSecurityGroupRequest = DeleteInputSecurityGroupRequest.builder()
				.inputSecurityGroupId(securityGroupId)
				.build();
			DeleteInputSecurityGroupResponse deleteSecurityGroupResponse = mediaLiveClient.deleteInputSecurityGroup(deleteInputSecurityGroupRequest);
			log.info("{}:: deleted", securityGroupId);
			return Optional.of(deleteSecurityGroupResponse);
		} else {
			return Optional.empty();
		}
	}

	private void cleanUp(CreateInputSecurityGroupResponse securityGroupResponse) {
		if (securityGroupResponse != null) {
			log.info("DELETE Security {}", securityGroupResponse.securityGroup().id());
			Optional<DeleteInputSecurityGroupResponse> optSecurityGroup = deleteInputSecurityGroup(securityGroupResponse.securityGroup().id());
			if (optSecurityGroup.isPresent()) {
				log.info("Removed security {}", securityGroupResponse.securityGroup().id());
			}
		}
	}

	private CreateInputSecurityGroupRequest buildInputSecurityGroup(LiveInputRequest liveInputRequest) {
		List<InputWhitelistRuleCidr> whitelistRuleCidrList =
			liveInputRequest.getInputSecurityGroups()
				.stream()
				.map(cidr -> InputWhitelistRuleCidr.builder().cidr(cidr).build())
				.toList();
		return CreateInputSecurityGroupRequest.builder().whitelistRules(whitelistRuleCidrList).build();
	}

	//CLI: --input-security-groups (list) A list of security groups referenced by IDs to attach to the input.(string)
	private CreateInputRequest buildCreateInputRequest(
		LiveInputRequest liveInputRequest,
		InputSecurityGroup inputSecurityGroup
	) throws VideoServiceException {

		return CreateInputRequest
			.builder()
			.name(liveInputRequest.getName())
			.type(liveInputRequest.getInputType())
			.inputSecurityGroups(inputSecurityGroup.id())
			.destinations(buildDestination(liveInputRequest))
			.build();
	}

	private List<InputDestinationRequest> buildDestination(LiveInputRequest liveInputRequest) throws VideoServiceException {
		if (liveInputRequest.getInputType() == InputType.RTMP_PUSH) {
			return buildRtmpInputDestination(liveInputRequest);
		} else {
			throw new VideoServiceException(liveInputRequest.getInputType().name() + ":: Unsupported", ErrorReason.SERVICE_EXCEPTION);
		}
	}

	private List<InputDestinationRequest> buildRtmpInputDestination(LiveInputRequest liveInputRequest) {
		return liveInputRequest.getInputDestinations()
			.stream()
			.map(liveInputDestination -> InputDestinationRequest
				.builder()
				.streamName(liveInputDestination.getAppName() + "/" + liveInputDestination.getAppInstance())
				.build())
			.toList();
	}

	//TODO - Add automation of creating the MediaLiveAccessRole if it doesn't exist.
	private CreateChannelRequest buildCreateChannelRequest(
		LiveRequest liveRequest,
		CreateInputResponse inputResponse,
		MediaPackageChannel pkgChannel
	) {
		ChannelRequest channelRequest = liveRequest.getChannelRequest();
		String liveName = liveRequest.getLiveInputRequest().getName();
		String mediaPackageChannelId = pkgChannel.getCreateChannelResponse().id();
		return CreateChannelRequest
			.builder()
			.name(channelRequest.getName())
			.roleArn(mediaLiveConfig.getAccessRoleArn())
			.channelClass(liveRequest.getChannelRequest().getChannelClass())
			.inputAttachments(buildInputAttachment(inputResponse))
			.inputSpecification(buildInputSpecifications())
			.encoderSettings(buildMediaPackageOutput(liveName, mediaPackageChannelId))
			.destinations(buildOutputDestination(liveName, mediaPackageChannelId))  //Output Groups? usually from template.
			.build();
	}


	//Console MediaPackage outputs. Should be 4 outputs for HD (Media Package)
	//Each output should have audio and video encoding
	private EncoderSettings buildMediaPackageOutput(String liveName, String mediaPackageChannelId) {
		log.info(LOG_FORMAT, liveName, mediaPackageChannelId);
		return EncoderSettings.builder()
			.audioDescriptions(
				buildAudioDescription(AUDIO_1),
				buildAudioDescription(AUDIO_2),
				buildAudioDescription(AUDIO_3),
				buildAudioDescription(AUDIO_4)
			)
			.videoDescriptions(
				buildVideoDescription(VIDEO_1080P_30, 1920, 1080, 5000000, 50),
				buildVideoDescription(VIDEO_720P_30, 1280, 720, 3000000, 100),
				buildVideoDescription(VIDEO_480P_30, 640, 480, 1500000, 100),
				buildVideoDescription(VIDEO_240P_30, 320, 240, 750000, 100)
			)
			.outputGroups(
				buildMediaPackageOutputGroup(liveName, mediaPackageChannelId),
				buildArchiveOutputGroup(liveName, mediaPackageChannelId)
			)
			.timecodeConfig(timecode -> timecode.source(TimecodeConfigSource.EMBEDDED).build())
			.build();
	}

	private VideoDescription buildVideoDescription(
		String name,
		int width,
		int height,
		int bitrate,
		int sharpness
	) {
		return VideoDescription.builder()
			.name(name)
			.width(width)
			.height(height)
			.codecSettings(videoCodecSettings -> videoCodecSettings
				.h264Settings(h264Settings -> h264Settings
					.parControl(H264ParControl.SPECIFIED)
					.afdSignaling(AfdSignaling.NONE)
					.rateControlMode(H264RateControlMode.CBR)
					.bitrate(bitrate)
					.framerateControl(H264FramerateControl.SPECIFIED)
					.framerateNumerator(30)
					.framerateDenominator(1)
					.scanType(H264ScanType.PROGRESSIVE)
					.forceFieldPictures(H264ForceFieldPictures.DISABLED)
					.gopSize((double) 60)
					.gopSizeUnits(H264GopSizeUnits.FRAMES)
					.gopNumBFrames(3)
					.gopClosedCadence(1)
					.numRefFrames(3)
					.gopBReference(H264GopBReference.ENABLED)
					.sceneChangeDetect(H264SceneChangeDetect.ENABLED)
					.subgopLength(H264SubGopLength.FIXED)
					.profile(H264Profile.HIGH)
					.level(H264Level.H264_LEVEL_AUTO)
					.entropyEncoding(H264EntropyEncoding.CABAC)
					.slices(1)
					.adaptiveQuantization(H264AdaptiveQuantization.HIGH)
					.spatialAq(H264SpatialAq.ENABLED)
					.temporalAq(H264TemporalAq.ENABLED)
					.syntax(H264Syntax.DEFAULT)
					.flickerAq(H264FlickerAq.ENABLED)
					.colorMetadata(H264ColorMetadata.INSERT)
					.lookAheadRateControl(H264LookAheadRateControl.HIGH)
					.timecodeInsertion(H264TimecodeInsertionBehavior.DISABLED)
					.build())
				.build()
			)
			.scalingBehavior(VideoDescriptionScalingBehavior.DEFAULT)
			.sharpness(sharpness)
			.respondToAfd(VideoDescriptionRespondToAfd.NONE)
			.build();
	}

	private AudioDescription buildAudioDescription(String name) {
		return AudioDescription.builder()
			.name(name)
			.audioSelectorName("default")
			.codecSettings(buildAudioCodecSettings())
			.audioNormalizationSettings(SdkBuilder::build)
			.languageCodeControl(AudioDescriptionLanguageCodeControl.FOLLOW_INPUT)
			.audioTypeControl(AudioDescriptionAudioTypeControl.FOLLOW_INPUT)
			.build();
	}

	private AudioCodecSettings buildAudioCodecSettings() {
		return AudioCodecSettings.builder()
			.aacSettings(buildAacSettings())
			.build();
	}

	private AacSettings buildAacSettings() {
		return AacSettings.builder()
			.codingMode(AacCodingMode.CODING_MODE_2_0)
			.profile(AacProfile.LC)
			.bitrate((double) 192000)
			.sampleRate((double) 48000)
			.inputType(AacInputType.NORMAL)
			.rawFormat(AacRawFormat.NONE.name())
			.spec(AacSpec.MPEG4)
			.rateControlMode(AacRateControlMode.CBR)
			.build();
	}

	private OutputGroup buildMediaPackageOutputGroup(String liveName, String mediaPackageChannelId) {
		log.info(LOG_FORMAT, liveName, mediaPackageChannelId);

		Output output1 = Output.builder()
			.outputName("1080p30").videoDescriptionName(VIDEO_1080P_30).audioDescriptionNames(AUDIO_1)
			.outputSettings(outputSettings -> outputSettings.mediaPackageOutputSettings(SdkBuilder::build))
			.build();
		Output output2 = Output.builder()
			.outputName("720p30").videoDescriptionName(VIDEO_720P_30).audioDescriptionNames(AUDIO_2)
			.outputSettings(outputSettings -> outputSettings.mediaPackageOutputSettings(SdkBuilder::build))
			.build();
		Output output3 = Output.builder()
			.outputName("480p30").videoDescriptionName(VIDEO_480P_30).audioDescriptionNames(AUDIO_3)
			.outputSettings(outputSettings -> outputSettings.mediaPackageOutputSettings(SdkBuilder::build))
			.build();
		Output output4 = Output.builder()
			.outputName("240p30").videoDescriptionName(VIDEO_240P_30).audioDescriptionNames(AUDIO_4)
			.outputSettings(outputSettings -> outputSettings.mediaPackageOutputSettings(SdkBuilder::build))
			.build();
		return OutputGroup.builder()
			.name("HD")
			.outputGroupSettings(outputGroupSettings ->
				outputGroupSettings.mediaPackageGroupSettings(mediaPackageGroupSettings ->
						mediaPackageGroupSettings.destination(outputLocationRef -> outputLocationRef.destinationRefId(mediaPackageChannelId).build())
							.build())
					.build())
			.outputs(output1, output2, output3, output4)
			.build();
	}

	private OutputGroup buildArchiveOutputGroup(String liveName, String mediaPackageChannelId) {
		log.info(LOG_FORMAT, liveName, mediaPackageChannelId);

		S3Uri s3Uri = s3Service.createArchiveFolder(liveName);
		//Add liveName to simulate s3://osint-media-archive/liveName/liveName_NameModifier.segment#.ts)
		String destinationUri = s3Uri.uri().toString() + '/' + liveName;
		String[] finalDestinationUri = {destinationUri.replaceFirst("s3:", "s3ssl:")};
		log.info(LOG_FORMAT + " S3 {}", liveName, mediaPackageChannelId, destinationUri);
		Output archiveOutput = Output.builder()
			.outputName("p4csa7")
			.outputSettings(outputSettings ->
				outputSettings
					.archiveOutputSettings(archiveOutputSettings -> archiveOutputSettings
						.nameModifier("_Live")
						.containerSettings(archiveContainerSettings ->
							archiveContainerSettings.m2tsSettings(m2tsSettings -> m2tsSettings
									.bufferModel(M2tsBufferModel.MULTIPLEX)
									.audioBufferModel(M2tsAudioBufferModel.ATSC)
									.rateMode(M2tsRateMode.CBR)
									.programNum(1)
									.segmentationMarkers(M2tsSegmentationMarkers.NONE)
									.segmentationStyle(M2tsSegmentationStyle.MAINTAIN_CADENCE)
									.ebpPlacement(M2tsEbpPlacement.VIDEO_AND_AUDIO_PIDS)
									.ebpAudioInterval(M2tsAudioInterval.VIDEO_INTERVAL)
									.audioStreamType(M2tsAudioStreamType.DVB)
									.absentInputAudioBehavior(M2tsAbsentInputAudioBehavior.ENCODE_SILENCE)
									.audioFramesPerPes(2)
									.esRateInPes(M2tsEsRateInPes.EXCLUDE)
									.ccDescriptor(M2tsCcDescriptor.DISABLED)
									.patInterval(100)
									.pmtPid("480")
									.pmtInterval(100)
									.videoPid("481")
									.pcrControl(M2tsPcrControl.PCR_EVERY_PES_PACKET)
									.pcrPeriod(40)
									.audioPids("482-498")
									.dvbTeletextPid("499")
									.dvbSubPids("460-479")
									.scte27Pids("450-479")
									.scte35Control(M2tsScte35Control.NONE)
									.scte35Pid("500")
									.klv(M2tsKlv.NONE)
									.klvDataPids("501")
									.timedMetadataBehavior(M2tsTimedMetadataBehavior.NO_PASSTHROUGH)
									.timedMetadataPid("502")
									.ebif(M2tsEbifControl.NONE)
									.etvPlatformPid("504")
									.etvSignalPid("505")
									.arib(M2tsArib.DISABLED)
									.aribCaptionsPidControl(M2tsAribCaptionsPidControl.AUTO)
									.aribCaptionsPid("507")
									.nielsenId3Behavior(M2tsNielsenId3Behavior.NO_PASSTHROUGH)
									.build())
								.build())
						.build())
					.build())
			.videoDescriptionName("video_apmblz")
			.audioDescriptionNames("audio_fbtpg")
			.build();

		//Object references undefined destination "s3://osint-media-archive/mli-test-01-test-01" (Service: MediaLive, Status Code: 422, Request ID: 4f6c13d7-e6a9-41ab-a25f-f52cc232ad26)
		return OutputGroup.builder()
			.name("archive-s3")
			.outputGroupSettings(outputGroupSettings -> outputGroupSettings
				.archiveGroupSettings(archiveGroupSettings -> archiveGroupSettings
					.rolloverInterval(300)
					.destination(destination -> destination.destinationRefId(finalDestinationUri[0]))
					.build())
				.build())
			.outputs(archiveOutput)
			.build();
	}

	private InputAttachment buildInputAttachment(CreateInputResponse inputResponse) {
		return InputAttachment
			.builder()
			.inputId(inputResponse.input().id())
			.inputAttachmentName(inputResponse.input().name())
			.inputSettings(buildInputSettings())
			.build();
	}

	private InputSettings buildInputSettings() {
		return InputSettings
			.builder()
			.sourceEndBehavior(InputSourceEndBehavior.valueOf("CONTINUE").name())
			.inputFilter(InputFilter.valueOf("AUTO").name())
			.filterStrength(1)
			.deblockFilter(InputDeblockFilter.valueOf("DISABLED").name())
			.denoiseFilter(InputDenoiseFilter.valueOf("DISABLED").name())
			.smpte2038DataPreference("IGNORE")
			.build();
	}

	private InputSpecification buildInputSpecifications() {
		return InputSpecification.builder()
			.codec(InputCodec.AVC)
			.resolution(InputResolution.HD)
			.maximumBitrate(MaximumInputBitrate.MAX_10_MBPS.name())
			.build();
	}

	private OutputDestination buildOutputDestination(String liveName, String mediaPackageChannelId) {
		log.info(LOG_FORMAT, liveName, mediaPackageChannelId);
		return OutputDestination.builder()
			.id(mediaPackageChannelId)  //Console MediaPackage destination - MediaPackage channel ID
			.mediaPackageSettings(buildMediaPackageOutputDestinationSettings(liveName, mediaPackageChannelId))
			.build();
	}

	private MediaPackageOutputDestinationSettings buildMediaPackageOutputDestinationSettings(String liveName, String mediaPackageChannelId) {
		log.info(LOG_FORMAT, liveName, mediaPackageChannelId);
		return MediaPackageOutputDestinationSettings.builder()
			.channelId(mediaPackageChannelId)
			.build();
	}

	@Override
	public void deleteChannel(Channel channel) {
		DeleteChannelRequest deleteChannelRequest = DeleteChannelRequest.builder()
			.channelId(channel.id()).build();
		mediaLiveClient.deleteChannel(deleteChannelRequest);
	}

	private Optional<InputSecurityGroup> findSecurityGroup(String securityGroupId) {
		ListInputSecurityGroupsResponse securityGroupsResponse = mediaLiveClient.listInputSecurityGroups(ListInputSecurityGroupsRequest.builder()
			.build());
		return securityGroupsResponse.inputSecurityGroups()
			.stream()
			.filter(isg -> isg.id().equals(securityGroupId))
			.findFirst();

	}

	private void buildMediaLiveClient() {
		if (mediaLiveClient == null) {
			this.mediaLiveClient = buildMediaLiveClient(awsConfig, awsCredentialsConfig);
		}
	}

}
