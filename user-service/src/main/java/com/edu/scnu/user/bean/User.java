package com.edu.scnu.user.bean;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Table(name = "t_user")
@Data
public class User {
	
	@Id
	private String id;
	
	private String bzId;
	
	private String username;
	
	private String password;
	
	private String nickname;
	
	private String avatar;
	
	private Date createTime;
	
	private Date updateTime;
	
}
