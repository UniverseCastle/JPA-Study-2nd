package com.jpa2.global.jwt.service;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface JwtService {

	String createAccessToken(String username);
	String createRefreshToken();
	
	void updateRefreshToken(String username, String refreshToken);
	
	void destroyRefreshToken(String username);
	
	void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken);
	
	void sendAccessToken(HttpServletResponse response, String accessToken);
	
	Optional<String> extractAccessToken(HttpServletRequest request);
	
	Optional<String> extractRefreshToken(HttpServletRequest request);
	
	Optional<String> extractUsername(String accessToken);
	
	void setAccessTokenHeader(HttpServletResponse response, String accessToken);
	void setRefreshTokenHeader(HttpServletResponse response, String refreshToken);
	
	boolean isTokenValid(String token);
}