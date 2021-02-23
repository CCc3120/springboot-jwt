package com.bingo.jwt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 拦截路径可自行配置多个 可用 ，分隔开
		registry.addInterceptor(new JwtInterceptor())
				// 需拦截的路径
				.addPathPatterns("/**")
				// 需放行的路径
				.excludePathPatterns("/**/login");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// 配置可以被跨域的路径，可以任意配置，可以具体到直接请求路径。
		registry.addMapping("/**")
				// 允许所有的请求域名访问我们的跨域资源，可以固定单条或者多条内容，如："http://www.baidu.com"，只有百度可以访问我们的跨域资源。
				.allowedOrigins("*")
				.allowCredentials(true)
				// 允许所有的请求方法访问该跨域资源服务器，如：POST、GET、PUT、DELETE等。
				.allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS", "HEAD")

				.maxAge(3600 * 24)
				// 允许所有的请求header访问，可以自定义设置任意请求头信息，如："X-YAUTH-TOKEN"
				.allowedHeaders("*");
	}

}
