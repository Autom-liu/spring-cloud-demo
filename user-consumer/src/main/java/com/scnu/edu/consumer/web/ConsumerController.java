package com.scnu.edu.consumer.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.scnu.edu.consumer.bean.User;
import com.scnu.edu.consumer.bean.common.Result;

@RestController
@RequestMapping("consumer")
@DefaultProperties(defaultFallback = "defaultFailback")
public class ConsumerController {
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private LoadBalancerClient ribbonClient;
	
	@GetMapping("/{id}")
	@HystrixCommand(commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "6000")
	})
	public Result<User> queryById(@PathVariable("id") String id) {
		ServiceInstance instance = ribbonClient.choose("user-service");
		String host = instance.getHost();
		int port = instance.getPort();
		
		String url = String.format("http://%s:%d/user/%s", host, port, id);
		System.out.println(url);
		User user = restTemplate.getForObject(url, User.class);
		return Result.success(user);
	}
	
	public Result<User> defaultFailback() {
		return Result.error("服务器繁忙，请稍后再试试....");
	}
	
}
