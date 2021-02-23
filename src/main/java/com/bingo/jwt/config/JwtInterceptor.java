package com.bingo.jwt.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.bingo.jwt.annotation.JwtIgnore;
import com.bingo.jwt.exception.CustomException;
import com.bingo.jwt.util.JwtTokenUtil;
import com.bingo.jwt.util.ResultCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private Audience audience;

	/**
	 * 这个方法将在请求处理之前进行调用。注意：如果该方法的返回值为false
	 * ，将视为当前请求结束，不仅自身的拦截器会失效，还会导致其他的拦截器也不再执行。
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 忽略带JwtIgnore注解的请求, 不做后续token认证校验
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			JwtIgnore jwtIgnore = handlerMethod.getMethodAnnotation(JwtIgnore.class);
			if (jwtIgnore != null) {
				return true;
			}
		}

		// 这里是个坑，因为带请求带headers时，ajax会发送两次请求，
		// 第一次会发送OPTIONS请求，第二次才会发生get/post请求，所以要放行OPTIONS请求
		// 如果是OPTIONS请求，让其响应一个 200状态码，说明可以正常访问
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
			return true;
		}

		// 获取请求头信息authorization信息
		String authHeader = request.getHeader(JwtTokenUtil.AUTH_HEADER_KEY);
		log.info("## authHeader= {}", authHeader);
		if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
			log.info("### 用户未登录，请先登录 ###");
			throw new CustomException(ResultCode.USER_NOT_LOGGED_IN);
		}

		// 获取token
		final String token = authHeader.substring(7);

		if (audience == null) {
			BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
			audience = (Audience) factory.getBean("audience");
		}

		// 验证token是否有效--无效已做异常抛出，由全局异常处理后返回对应信息
		JwtTokenUtil.parseJWT(token, audience.getBase64Secret());

		return true;
	}

	/**
	 * 只有在 preHandle() 方法返回值为true 时才会执行。会在Controller 中的方法调用之后，DispatcherServlet
	 * 返回渲染视图之前被调用。 有意思的是：postHandle() 方法被调用的顺序跟 preHandle() 是相反的，先声明的拦截器
	 * preHandle() 方法先执行，而postHandle()方法反而会后执行。
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}

	/**
	 * 只有在 preHandle() 方法返回值为true 时才会执行。在整个请求结束之后， DispatcherServlet
	 * 渲染了对应的视图之后执行
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);
	}

	@Override
	public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		super.afterConcurrentHandlingStarted(request, response, handler);
	}

}
