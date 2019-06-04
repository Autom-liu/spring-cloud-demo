package com.scnu.edu.consumer.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.scnu.edu.consumer.bean.User;
import com.scnu.edu.consumer.bean.common.Result;
import com.scnu.edu.consumer.client.UserClient;

@RestController
@RequestMapping("consumer")
@DefaultProperties(defaultFallback = "defaultFailback")
public class ConsumerController {
	
	@Autowired
	private UserClient userClient;
	
	@GetMapping("/{id}")
	@HystrixCommand(commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "6000"),
			@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
			@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),
	})
	public Result<User> queryById(@PathVariable("id") String id) {
		User user = userClient.queryById(id);
		return Result.success(user);
	}
	
	public Result<User> defaultFailback() {
		return Result.error("服务器繁忙，请稍后再试试....");
	}
	
}
