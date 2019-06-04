package com.scnu.edu.consumer.bean;

import java.util.Date;

import lombok.Data;

@Data
public class User {

	private String id;
	
	private String bzId;
	
	private String username;
	
	private String password;
	
	private String nickname;
	
	private String avatar;
	
	private Date createTime;
	
	private Date updateTime;
	
}
