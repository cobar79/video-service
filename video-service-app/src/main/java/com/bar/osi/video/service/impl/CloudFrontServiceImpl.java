package com.bar.osi.video.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import com.bar.osi.video.config.aws.AwsConfig;
import com.bar.osi.video.config.aws.AwsCredentialsConfig;
import com.bar.osi.video.config.aws.CloudFrontConfig;
import com.bar.osi.video.service.CloudFrontService;
import com.bar.osi.video.service.exceptions.ErrorReason;
import com.bar.osi.video.service.exceptions.VideoServiceException;
import com.bar.osi.video.service.exceptions.VideoServiceRuntimeException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateDistributionRequest;
import software.amazon.awssdk.services.cloudfront.model.CreateDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.DeleteDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.cloudfront.model.DistributionConfig;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionRequest;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.Method;
import software.amazon.awssdk.services.cloudfront.model.Origin;
import software.amazon.awssdk.services.cloudfront.model.OriginProtocolPolicy;
import software.amazon.awssdk.services.cloudfront.model.SslProtocol;
import software.amazon.awssdk.services.cloudfront.model.ViewerProtocolPolicy;
import software.amazon.awssdk.services.cloudfront.waiters.CloudFrontWaiter;
import software.amazon.awssdk.services.mediapackage.model.CreateOriginEndpointResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CloudFrontServiceImpl implements CloudFrontService {

	/** Cloud Front Client */
	private CloudFrontClient cloudFrontClient;

	/** AWS Configurations. */
	private final AwsConfig awsConfig;

	/** AWS Credential Configurations.*/
	private final AwsCredentialsConfig awsCredentialsConfig;

	/** AWS Cloud Front Configurations.*/
	private final CloudFrontConfig cloudFrontConfig;

	@Autowired
	CloudFrontServiceImpl(
		AwsConfig awsConfig,
		AwsCredentialsConfig awsCredentialsConfig,
		CloudFrontConfig cloudFrontConfig
	) {
		this.awsConfig = awsConfig;
		this.awsCredentialsConfig = awsCredentialsConfig;
		this.cloudFrontConfig = cloudFrontConfig;
		this.cloudFrontClient = buildCloudFrontClient();
	}


	@Override
	public Distribution createDistribution(CreateOriginEndpointResponse originEndpointResponse) throws VideoServiceException {
		try {
			String originUrl = originEndpointResponse.url();
			String protocol = "https://";
			String domainName = originUrl.substring(originUrl.indexOf(protocol) + protocol.length(), originUrl.indexOf("/out"));
			log.info("domainName {}", domainName);

			//origin.id must match distributionRequest.defaultCacheBehavior.targetId
			Origin origin = Origin.builder()
				.id(domainName)
				.domainName(domainName)
				.originShield(originShield -> originShield
					.enabled(false)
					.build())
				.connectionAttempts(3)
				.connectionTimeout(10)
				.customOriginConfig(customOriginConfig -> customOriginConfig
					.httpsPort(443)
					.httpPort(80)
					.originReadTimeout(30)
					.originKeepaliveTimeout(5)
					.originProtocolPolicy(OriginProtocolPolicy.HTTPS_ONLY)
					.originSslProtocols(originSslProtocols -> originSslProtocols
						.items(SslProtocol.TLS_V1_2)
						.quantity(1)
						.build())
					.build())
				.build();

			CreateDistributionRequest distributionRequest = CreateDistributionRequest.builder()
				.distributionConfig(distributionConfig -> distributionConfig
					.enabled(true)
					.comment(originEndpointResponse.description())
					// A value that you specify to uniquely identify an invalidation request.
					// CloudFront uses the value to prevent you from accidentally resubmitting an identical request.
					.callerReference(UUID.randomUUID().toString())
					.origins(origins -> origins
						.items(origin)
						.quantity(1)
						.build())
					.defaultCacheBehavior(defaultCacheBehavior -> defaultCacheBehavior
						.compress(true)
						.viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
						.allowedMethods(allowedMethods -> allowedMethods
							.items(Method.GET, Method.HEAD)
							.quantity(2)
							.build())
						.smoothStreaming(false)
						.targetOriginId(domainName)
						.cachePolicyId(cloudFrontConfig.getCachePolicyId())
						.build())
					.build())
				.build();

			CreateDistributionResponse response = cloudFrontClient.createDistribution(distributionRequest);

			//TODO Do we wait till it is deployed?? Possibly create the package, distribution, channel and then wait.
			// Same applies for delete: disable, wait, delete.
//			long startTime = System.currentTimeMillis();
//			GetDistributionResponse distributionResponse = waitForDistribution(response.distribution().id(), "create");
//			Distribution distribution = distributionResponse.distribution();
//			log.info("{}-{}:: {}. Took {} millis to deploy",
//				distributionResponse.eTag(),
//				distribution.id(),
//				distribution.status(),
//				System.currentTimeMillis() - startTime);

			return response.distribution();
		} catch (AwsServiceException e) {
			log.error(e.getMessage(), e);
			throw new VideoServiceException(e.getMessage(), ErrorReason.SERVICE_EXCEPTION);
		}
	}

	@Override
	public void deleteDistribution(String distributionId) throws VideoServiceException {
		// First, disable the distribution by updating it.
		GetDistributionResponse response = cloudFrontClient.getDistribution(b -> b
			.id(distributionId));
		String eTag = response.eTag();
		DistributionConfig distConfig = response.distribution().distributionConfig();
		cloudFrontClient.updateDistribution(builder -> builder
			.id(distributionId)
			.distributionConfig(builder1 -> builder1
				.cacheBehaviors(distConfig.cacheBehaviors())
				.defaultCacheBehavior(distConfig.defaultCacheBehavior())
				.enabled(false)
				.origins(distConfig.origins())
				.comment(distConfig.comment())
				.callerReference(distConfig.callerReference())
				.defaultCacheBehavior(distConfig.defaultCacheBehavior())
				.priceClass(distConfig.priceClass())
				.aliases(distConfig.aliases())
				.logging(distConfig.logging())
				.defaultRootObject(distConfig.defaultRootObject())
				.customErrorResponses(distConfig.customErrorResponses())
				.httpVersion(distConfig.httpVersion())
				.isIPV6Enabled(distConfig.isIPV6Enabled())
				.restrictions(distConfig.restrictions())
				.viewerCertificate(distConfig.viewerCertificate())
				.webACLId(distConfig.webACLId())
				.originGroups(distConfig.originGroups()))
			.ifMatch(eTag));

		log.info("Distribution [{}] is DISABLED, waiting for deployment before deleting ...", distributionId);
		GetDistributionResponse distributionResponse = waitForDistribution(distributionId, "disable");
		DeleteDistributionResponse deleteDistributionResponse = cloudFrontClient
			.deleteDistribution(builder -> builder
				.id(distributionId)
				.ifMatch(distributionResponse.eTag()));
		if (deleteDistributionResponse.sdkHttpResponse().isSuccessful()) {
			log.info("{}:: DELETED", distributionId);
		} else {
			throw new VideoServiceException(distributionId + ":: failed to delete", ErrorReason.SERVICE_EXCEPTION);
		}
	}

	public Distribution findDistribution(String distributionId) {

		GetDistributionRequest request = GetDistributionRequest.builder()
			.id(distributionId)
			.build();

		GetDistributionResponse response = cloudFrontClient.getDistribution(request);

		return response.distribution();
	}


	private GetDistributionResponse waitForDistribution(String distributionId, String action) {
		GetDistributionResponse distributionResponse;
		try (CloudFrontWaiter cfWaiter = CloudFrontWaiter.builder().client(cloudFrontClient).build()) {
			ResponseOrException<GetDistributionResponse> responseOrException = cfWaiter
				.waitUntilDistributionDeployed(builder -> builder.id(distributionId)).matched();
			distributionResponse = responseOrException.response()
				.orElseThrow(() ->
					new VideoServiceRuntimeException(distributionId + ":: Could not " + action + " distribution", ErrorReason.SERVICE_EXCEPTION));
			return distributionResponse;
		}
	}

	CloudFrontClient buildCloudFrontClient() {
		if (awsConfig.getLocalstackUrl() != null) {
			return CloudFrontClient
				.builder()
				.region(Region.of(awsConfig.getRegion()))
				.endpointOverride(buildUri(awsConfig.getLocalstackUrl()))
				.credentialsProvider(awsCredentialsConfig.testingCredentialProvider())
				.build();
		} else {
			return CloudFrontClient
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
