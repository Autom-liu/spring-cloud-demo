package com.edu.scnu.user.server;

import com.edu.scnu.user.bean.User;

public interface UserService {
	
	User queryById(String id);
	
}
