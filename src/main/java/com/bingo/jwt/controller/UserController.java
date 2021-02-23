package com.bingo.jwt.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bingo.jwt.annotation.JwtIgnore;
import com.bingo.jwt.config.Audience;
import com.bingo.jwt.util.JwtTokenUtil;
import com.bingo.jwt.util.Result;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserController {

	@Autowired
	private Audience audience;

	@JwtIgnore
	@RequestMapping(value = "/login")
	public Result login(HttpServletResponse response, String username, String password) {
		String userId = UUID.randomUUID().toString();
		String role = "admin";
		String token = JwtTokenUtil.createJWT(userId, username, role, audience);
		log.info("### 登录成功, token={} ###", token);
		response.setHeader(JwtTokenUtil.AUTH_HEADER_KEY, JwtTokenUtil.TOKEN_PREFIX + token);
		// 将token响应给客户端
		// 将token响应给客户端
		JSONObject result = new JSONObject();
		result.put("token", token);
		return Result.SUCCESS(result);
	}

	@RequestMapping(value = "/list")
	public JSONObject list() {
		JSONObject result = new JSONObject();
		result.put("success", "ok");
		return result;
	}

}
