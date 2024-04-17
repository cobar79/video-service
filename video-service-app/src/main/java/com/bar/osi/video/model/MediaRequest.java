package com.bar.osi.video.model;

import lombok.Data;

@Data
public class MediaRequest {

	/** The Name of the inbound Stream.*/
	private String streamName;

	/** IP of the publishing IP. */
	private String inputIp;

}
