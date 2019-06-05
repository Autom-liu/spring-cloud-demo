package com.edu.scnu.cloud.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.edu.scnu.cloud.common.Result;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class LoginFilter extends ZuulFilter {

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext context = RequestContext.getCurrentContext();
		HttpServletRequest request = context.getRequest();
		
		String token = request.getParameter("token");
		
		if(StringUtils.isBlank(token)) {
			// 不存在，则拦截
			context.setSendZuulResponse(false);
			HttpServletResponse response = context.getResponse();
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setContentType("text/json;charset=UTF-8");
			context.setResponse(response);
//			context.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
			
			context.setResponseBody(JSON.toJSONString(Result.error("没有权限")));
		}
		
		return null;
	}

	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
	}

}
