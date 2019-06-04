package com.scnu.edu.consumer.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
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
	private RibbonLoadBalancerClient ribbonClient;
	
	@GetMapping("/{id}")
	public User queryById(@PathVariable("id") String id) {
		// 使用ribbon负载均衡器可以直接id实例
		ServiceInstance instance = ribbonClient.choose("user-service");
		String url = "http://"+ instance.getHost() +":"+ instance.getPort() +"/user/" + id;
		User user = restTemplate.getForObject(url, User.class);
		return user;
	}
	
}
