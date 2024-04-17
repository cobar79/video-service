package com.bar.osi.video.service;

import com.bar.osi.video.model.pkg.MediaPackageChannel;
import com.bar.osi.video.model.pkg.MediaPackageRequest;
import com.bar.osi.video.service.exceptions.VideoServiceException;

public interface MediaPackageService {

	MediaPackageChannel createChannel(MediaPackageRequest packageRequest) throws VideoServiceException;

	void deleteChannel(MediaPackageChannel packageChannel) throws VideoServiceException;
}
