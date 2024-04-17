package com.bar.osi.video;

import com.bar.osi.video.config.ApplicationConfig;
import com.bar.osi.video.config.aws.CloudFrontConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(value = {
	ApplicationConfig.class,
	CloudFrontConfig.class
})
@SpringBootApplication
public class VideoServiceAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoServiceAppApplication.class, args);
	}

}
