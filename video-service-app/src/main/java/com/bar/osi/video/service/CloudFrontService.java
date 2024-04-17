package com.bar.osi.video.service;

import com.bar.osi.video.service.exceptions.VideoServiceException;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.mediapackage.model.CreateOriginEndpointResponse;

public interface CloudFrontService {

	Distribution createDistribution(CreateOriginEndpointResponse originEndpointResponse) throws VideoServiceException;

	Distribution findDistribution(String distributionId);

	void deleteDistribution(String distributionId) throws VideoServiceException;

}
