package com.bar.osi.video.config.aws;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aws.media-live")
public class MediaLiveConfig {

	private String accessRoleArn;


}
