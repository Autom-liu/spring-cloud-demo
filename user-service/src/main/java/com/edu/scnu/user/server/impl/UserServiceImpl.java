package com.edu.scnu.user.server.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edu.scnu.user.bean.User;
import com.edu.scnu.user.mapper.UserMapper;
import com.edu.scnu.user.server.UserService;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserMapper userMapper;

	@Override
	public User queryById(String id) {
		return userMapper.selectByPrimaryKey(id);
	}
	
}
