package com.bar.osi.video.model.live;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LiveRequest {

	/** MediaLive Input request.*/
	private LiveInputRequest liveInputRequest;

	/** MediaLive Channel request.*/
	private ChannelRequest channelRequest;

	/** MediaPackage channel ID.*/
	private String mediaPackageChannelArn;
}
