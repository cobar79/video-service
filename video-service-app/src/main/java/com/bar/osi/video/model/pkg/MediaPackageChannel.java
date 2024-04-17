package com.bar.osi.video.model.pkg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.mediapackage.model.CreateChannelResponse;
import software.amazon.awssdk.services.mediapackage.model.CreateOriginEndpointResponse;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaPackageChannel {

	/** The Package Channel create response. */
	private CreateChannelResponse createChannelResponse;

	/** The Package Origin Endpoint create response.*/
	private CreateOriginEndpointResponse createOriginEndpointResponse;
}
