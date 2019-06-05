package com.scnu.edu.consumer.client;

import org.springframework.stereotype.Component;

import com.scnu.edu.consumer.bean.User;

import feign.hystrix.FallbackFactory;

@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

	@Override
	public UserClient create(Throwable cause) {
		return new UserClient() {
			
			@Override
			public User queryById(String id) {
				return null;
			}
		};
	}

}
