package com.edu.scnu.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringBootApplication
public class GatewayApplication {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
