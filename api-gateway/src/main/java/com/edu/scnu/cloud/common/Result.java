package com.edu.scnu.cloud.common;

import lombok.Data;

@Data
public class Result<T> {
	
	private Integer code;
	
	private String msg;
	
	private T data;
	
	public static<T> Result<T> success(T data) {
		Result<T> result = new Result<>();
		result.setCode(0);
		result.setMsg("success");
		result.setData(data);
		return result;
	}
	
	public static<T> Result<T> error(String msg) {
		Result<T> result = new Result<>();
		result.setCode(99999);
		result.setMsg(msg);
		result.setData(null);
		return result;
	}
	
}
