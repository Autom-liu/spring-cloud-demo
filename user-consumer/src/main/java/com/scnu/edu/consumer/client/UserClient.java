package com.scnu.edu.consumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scnu.edu.consumer.bean.User;

@FeignClient(value = "user-service", fallbackFactory = UserClientFallbackFactory.class)
@RequestMapping("/user")
public interface UserClient {
	
	@GetMapping("/{id}")
	User queryById(@PathVariable("id") String id);
}
