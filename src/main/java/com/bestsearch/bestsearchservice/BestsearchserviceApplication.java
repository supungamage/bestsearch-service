package com.bestsearch.bestsearchservice;

import com.bestsearch.bestsearchservice.order.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class BestsearchserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BestsearchserviceApplication.class, args);
	}

}
