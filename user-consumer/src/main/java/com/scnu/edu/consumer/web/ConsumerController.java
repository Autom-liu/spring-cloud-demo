package com.scnu.edu.consumer.web;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	@GetMapping("/{id}")
	public User queryById(@PathVariable("id") String id) {
		// 将服务id直接写进url上
		String url = "http://user-service/user/" + id;
		User user = restTemplate.getForObject(url, User.class);
		return user;
	}
	
}
