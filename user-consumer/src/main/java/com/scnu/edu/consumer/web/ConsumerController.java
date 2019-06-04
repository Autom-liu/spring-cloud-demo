package com.scnu.edu.consumer.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.scnu.edu.consumer.bean.User;

@RestController
@RequestMapping("consumer")
public class ConsumerController {
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private LoadBalancerClient ribbonClient;
	
	@GetMapping("/{id}")
	public User queryById(@PathVariable("id") String id) {
		ServiceInstance instance = ribbonClient.choose("user-service");
		String host = instance.getHost();
		int port = instance.getPort();
		
		
		String url = String.format("http://%s:%d/user/%s", host, port, id);
		System.out.println(url);
		User user = restTemplate.getForObject(url, User.class);
		return user;
	}
	
}
