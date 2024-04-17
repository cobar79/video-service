package com.bar.osi.video.service.impl;

import java.net.URI;
import java.net.URISyntaxException;

import com.bar.osi.video.config.aws.AwsConfig;
import com.bar.osi.video.config.aws.AwsCredentialsConfig;
import com.bar.osi.video.service.S3Service;
import com.bar.osi.video.service.exceptions.ErrorReason;
import com.bar.osi.video.service.exceptions.VideoServiceRuntimeException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class S3ServiceImpl implements S3Service {


	private static final String S3_PREFIX = "s3://";

	private String archiveBucket;

	private S3Client s3Client;

	/** AWS Configurations. */
	private final AwsConfig awsConfig;

	/** AWS Credential Configurations.*/
	private final AwsCredentialsConfig awsCredentialsConfig;


	@Autowired
	public S3ServiceImpl(AwsConfig awsConfig, AwsCredentialsConfig awsCredentialsConfig) {
		this.awsConfig = awsConfig;
		this.awsCredentialsConfig = awsCredentialsConfig;
	}

	@PostConstruct
	public void postConstruct() {
		buildS3Client();
		checkArchiveBucket();
	}

	private void checkArchiveBucket() {
		archiveBucket = awsConfig.getArchiveBucket();
		log.info("Checking bucket {}", archiveBucket);
		try {
			ListBucketsResponse bucketsResponse = s3Client.listBuckets(ListBucketsRequest.builder().build());

			if (bucketsResponse.buckets().stream().noneMatch(bucket -> bucket.name().equals(archiveBucket))) {
				createArchiveBucket();
			}
		} catch (S3Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	private void createArchiveBucket() {
		log.info("Creating bucket {}", archiveBucket);
		try {
			CreateBucketResponse bucketResponse = s3Client.createBucket(createBucketRequest -> createBucketRequest.bucket(archiveBucket));
			log.info("CREATED {}", bucketResponse.toString());
		} catch (AwsServiceException e) {
			log.error(e.getMessage(), e);
			throw e;
		}

	}


	@Override
	public S3Uri createArchiveFolder(String folder) {
		if (!folderExists(folder)) {
			return putArchiveFolder(folder);
		} else {
			return s3Client.utilities().parseUri(URI.create(buildArchiveFolderUri(folder)));
		}
	}

	private boolean folderExists(String folder) {
		try {
			ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
				.bucket(archiveBucket)
				.build();
			ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjectsRequest);
			return listObjectsResponse.contents().stream().anyMatch(f -> f.key().equals(folder));
		} catch (AwsServiceException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	private S3Uri putArchiveFolder(String folder) {
		PutObjectRequest folderRequest = PutObjectRequest.builder()
			.bucket(archiveBucket)
			.key(folder)
			.build();
		s3Client.putObject(folderRequest, RequestBody.empty());
		S3Waiter waiter = s3Client.waiter();
		HeadObjectRequest requestWait = HeadObjectRequest.builder()
			.bucket(archiveBucket).key(folder)
			.build();

		WaiterResponse<HeadObjectResponse> waiterResponse = waiter.waitUntilObjectExists(requestWait);

		waiterResponse.matched().response().ifPresent(headObjectResponse -> log.info(headObjectResponse.toString()));
		return s3Client.utilities().parseUri(URI.create(buildArchiveFolderUri(folder)));
	}

	private String buildArchiveFolderUri(String folder) {
		return S3_PREFIX + archiveBucket + '/' + folder;
	}


	private void buildS3Client() {
		if (s3Client == null) {
			if (awsConfig.getLocalstackUrl() != null) {
				s3Client = S3Client
					.builder()
					.region(Region.of(awsConfig.getRegion()))
					.endpointOverride(buildUri(awsConfig.getLocalstackUrl()))
					.credentialsProvider(awsCredentialsConfig.testingCredentialProvider())
					.build();
			} else {
				s3Client = S3Client.builder()
					.region(Region.of(awsConfig.getRegion()))
					.credentialsProvider(awsCredentialsConfig.environmentCredentialProvider())
					.build();
			}
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
