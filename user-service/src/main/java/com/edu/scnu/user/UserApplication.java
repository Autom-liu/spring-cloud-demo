package com.edu.scnu.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@MapperScan("com.edu.scnu.user.mapper")
public class UserApplication {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(UserApplication.class, args);
	}

}
