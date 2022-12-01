package com.rpa.configuration;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

import com.rpa.util.UtilityFile;

@Configuration
public class ExternalConfiguration {

	@Bean
	public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
			throws UnsupportedEncodingException, URISyntaxException {

		PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();

		String jarBasePath = UtilityFile.getCodeBasePath();
		properties.setLocation(new FileSystemResource(jarBasePath + "/rpa_deploy/config/database.properties"));

		properties.setIgnoreResourceNotFound(false);

		return properties;

	}

}