package com.jpa2.global.login.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.jpa2.domain.member.repository.MemberRepository;
import com.jpa2.global.jwt.service.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessJWTProvideHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	private final JwtService jwtService;
	private final MemberRepository memberRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		String username = extractUsername(authentication);
		String accessToken = jwtService.createAccessToken(username);
		String refreshToken = jwtService.createRefreshToken();
		
		jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
		
		memberRepository.findByUsername(username).ifPresent(
				member -> member.updateRefreshToken(refreshToken)
		);
		
		log.info("로그인에 성공합니다. username: {}", username);
		log.info("AccessToken 을 발급합니다. AccessToken: {}", accessToken);
		log.info("RefreshToken 을 발급합니다. RefreshToken: {}", refreshToken);
	}
	
	private String extractUsername(Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		
		return userDetails.getUsername();
	}
}