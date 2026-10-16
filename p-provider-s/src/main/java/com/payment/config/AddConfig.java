package com.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration 
public class AddConfig {

	@Bean
	RestClient restClient() {
		return RestClient.create();
	}
	
}
