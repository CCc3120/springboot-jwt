package com.bingo.jwt.util;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bingo.jwt.config.Audience;
import com.bingo.jwt.exception.CustomException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtTokenUtil {
	private static Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

	public static final String AUTH_HEADER_KEY = "Authorization";

	public static final String TOKEN_PREFIX = "Bearer ";

	public static void main(String[] args) {
		String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2"
				+ "xlIjoiYWRtaW4iLCJ1c2VySWQiOiJaV1JpWm1SbFlXUXRZV1"
				+ "F5WkMwME1HVTBMVGhpTlRNdFpEQmpaVFpoTW1KbVpEbGsiLC"
				+ "Jpc3MiOiIwOThmNmJjZDQ2MjFkMzczY2FkZTRlODMyNjI3Yj"
				+ "RmNiIsImlhdCI6MTU5MzU3MjQxMiwiYXVkIjoicmVzdGFwaX"
				+ "VzZXIiLCJleHAiOjE1OTM1NzI0MTUsIm5iZiI6MTU5MzU3Mj"
				+ "QxMn0.bWZODpUpzUlMfmwLp_xJ-NUMGINtz5ypByLRwJwP7Qk";
		Claims claims = parseJWT(token, "MDk4ZjZiY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjY=");
		System.out.println(claims.getId());
		System.out.println(claims.getIssuer());
		System.out.println(claims.getSubject());
		System.out.println(claims.getIssuedAt());
		System.out.println(claims.get("userId", String.class));
		System.out.println(claims.get("role", String.class));
		System.out.println(claims.getAudience());
	}

	/**
	 * 解析jwt
	 * 
	 * @param jsonWebToken
	 * @param base64Security
	 * @return
	 */
	public static Claims parseJWT(String jsonWebToken, String base64Security) {
		try {
			Claims claims = Jwts.parser()
					.setSigningKey(DatatypeConverter.parseBase64Binary(base64Security))
					.parseClaimsJws(jsonWebToken).getBody();
			return claims;
		} catch (ExpiredJwtException eje) {
			log.error("===== Token过期 =====", eje);
			throw new CustomException(ResultCode.PERMISSION_TOKEN_EXPIRED);
		} catch (Exception e) {
			log.error("===== token解析异常 =====", e);
			throw new CustomException(ResultCode.PERMISSION_TOKEN_INVALID);
		}
	}

	/**
	 * 构建jwt
	 * 
	 * @param userId
	 * @param username
	 * @param role
	 * @param audience
	 * @return
	 */
	public static String createJWT(String userId, String username, String role, Audience audience) {
		try {
			// 使用HS256加密算法
			SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

			long nowMillis = System.currentTimeMillis();
			Date now = new Date(nowMillis);

			// 生成签名密钥
			byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(audience.getBase64Secret());
			Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

			// userId是重要信息，进行加密下
			String encryId = Base64Util.encode(userId);

			// 添加构成JWT的参数
			JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
					// 可以将基本不重要的对象信息放到claims
					.claim("role", role)
					.claim("userId", encryId)
					.setSubject(username) // 代表这个JWT的主体，即它的所有人
					.setIssuer(audience.getClientId()) // 代表这个JWT的签发主体；
					.setIssuedAt(new Date()) // 是一个时间戳，代表这个JWT的签发时间；
					.setAudience(audience.getName()) // 代表这个JWT的接收对象；
					.signWith(signatureAlgorithm, signingKey);
			// 添加Token过期时间
			int TTLMillis = audience.getExpiresSecond();
			if (TTLMillis >= 0) {
				long expMillis = nowMillis + TTLMillis;
				Date exp = new Date(expMillis);
				builder.setExpiration(exp) // 是一个时间戳，代表这个JWT的过期时间；
						.setNotBefore(now); // 是一个时间戳，代表这个JWT生效的开始时间，意味着在这个时间之前验证JWT是会失败的
			}

			// 生成JWT
			return builder.compact();
		} catch (Exception e) {
			log.error("签名失败", e);
			throw new CustomException(ResultCode.PERMISSION_SIGNATURE_ERROR);
		}
	}

	/**
	 * 从token中获取用户名
	 * 
	 * @param token
	 * @param base64Security
	 * @return
	 */
	public static String getUsername(String token, String base64Security) {
		return parseJWT(token, base64Security).getSubject();
	}

	/**
	 * 从token中获取用户ID
	 * 
	 * @param token
	 * @param base64Security
	 * @return
	 */
	public static String getUserId(String token, String base64Security) {
		String userId = parseJWT(token, base64Security).get("userId", String.class);
		return Base64Util.decode(userId);
	}

	/**
	 * 是否已过期
	 * 
	 * @param token
	 * @param base64Security
	 * @return
	 */
	public static boolean isExpiration(String token, String base64Security) {
		return parseJWT(token, base64Security).getExpiration().before(new Date());
	}
}