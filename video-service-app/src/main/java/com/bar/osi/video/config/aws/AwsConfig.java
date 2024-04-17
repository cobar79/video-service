package com.bar.osi.video.config.aws;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aws")
public class AwsConfig implements InitializingBean {

	/** Integration Test URL. */
	private String localstackUrl;

	/** AWS region. */
	private String region;

	/** Video Archive bucket */
	private String archiveBucket;

	@Override
	public void afterPropertiesSet() {
		log.info(this.toString());
	}

	@Override
	public String toString() {
		return "AwsConfig{" +
			"region='" + region + '\'' +
			'}';
	}
}
