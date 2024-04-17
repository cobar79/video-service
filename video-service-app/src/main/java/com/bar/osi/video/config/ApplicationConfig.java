package com.bar.osi.video.config;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationConfig {

	/** Phrases. */
	private List<String> phrases;
}
