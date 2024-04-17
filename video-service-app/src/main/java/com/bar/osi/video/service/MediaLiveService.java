package com.bar.osi.video.service;

import java.util.Optional;

import com.bar.osi.video.model.live.LiveRequest;
import com.bar.osi.video.model.pkg.MediaPackageChannel;
import com.bar.osi.video.service.exceptions.VideoServiceException;
import software.amazon.awssdk.services.medialive.model.Channel;
import software.amazon.awssdk.services.medialive.model.DeleteInputSecurityGroupResponse;
import software.amazon.awssdk.services.medialive.model.DescribeInputResponse;

public interface MediaLiveService {

	Channel createChannel(LiveRequest liveRequest, MediaPackageChannel pkgChannel) throws VideoServiceException;

	void deleteChannel(Channel channel);

	Optional<DeleteInputSecurityGroupResponse> deleteInputSecurityGroup(String securityGroupId);

	DescribeInputResponse deleteInput(String inputId);

	DescribeInputResponse waitTillInputDetached(String inputId);
}
