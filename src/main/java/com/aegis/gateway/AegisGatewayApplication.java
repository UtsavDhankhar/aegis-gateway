package com.aegis.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AegisGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(AegisGatewayApplication.class, args);
	}

}
