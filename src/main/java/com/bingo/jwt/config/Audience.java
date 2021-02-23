package com.bingo.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "audience")
@Component
public class Audience {
	private String clientId;
	private String base64Secret;
	private String name;
	private int expiresSecond;
}
