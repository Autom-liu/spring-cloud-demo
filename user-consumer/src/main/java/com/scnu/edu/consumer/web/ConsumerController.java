package com.scnu.edu.consumer.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
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
	private DiscoveryClient discoverClient;
	
	@GetMapping("/{id}")
	public User queryById(@PathVariable("id") String id) {
		// 根据服务id获取实例列表
		List<ServiceInstance> instances = discoverClient.getInstances("user-service");
		// 从实例列表中拿到实例
		ServiceInstance instance = instances.get(0);
		String url = "http://"+ instance.getHost() +":"+ instance.getPort() +"/user/" + id;
		User user = restTemplate.getForObject(url, User.class);
		return user;
	}
	
}
