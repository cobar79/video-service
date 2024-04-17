package com.bar.osi.video.service;

import software.amazon.awssdk.services.s3.S3Uri;

public interface S3Service {

	S3Uri createArchiveFolder(String folder);
}
