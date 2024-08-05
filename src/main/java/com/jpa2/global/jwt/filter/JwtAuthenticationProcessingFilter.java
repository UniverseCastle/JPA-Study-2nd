package com.jpa2.global.jwt.filter;

import java.io.IOException;

import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jpa2.domain.member.Member;
import com.jpa2.domain.member.repository.MemberRepository;
import com.jpa2.global.jwt.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {
	
	private final JwtService jwtService;
	private final MemberRepository memberRepository;
	
	private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
	
	private final String NO_CHECK_URL = "/login"; // 경로로 들어오는 요청에 대해서는 작동하지 않음

	/**
     * 1. 리프레시 토큰이 오는 경우 -> 유효하면 AccessToken 재발급후, 필터 진행 X, 바로 튕기기
     *
     * 2. 리프레시 토큰은 없고 AccessToken만 있는 경우 -> 유저정보 저장후 필터 계속 진행
     */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) // 필터 체인을 통해 다음 필터로 요청 전달
			throws ServletException, IOException {
		
		if (request.getRequestURL().equals(NO_CHECK_URL)) {
			filterChain.doFilter(request, response);
			
			return; // 안해주면 아래로 내려가서 계속 필터를 진행함
		}
		
		String refreshToken = jwtService
				.extractRefreshToken(request)
				.filter(jwtService::isTokenValid) // 유효성 검사
				.orElse(null); // RefreshToken이 없거나 유효하지 않다면 null 반환
		
		if(refreshToken != null) {
			checkRefreshTokenAndReIssueAccessToken(response, refreshToken); // 유효하다면 유저정보 찾아오고 AccessToken 재발급
			
			return;
		}
		
		checkAccessTokenAndAuthentication(request, response, filterChain); // refreshToken 없는 경우 AccessToken 검사 로직 수행
	}
	
	private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		jwtService.extractAccessToken(request).filter(jwtService::isTokenValid).ifPresent( // 유효성 검사
				
				accessToken -> jwtService.extractUsername(accessToken).ifPresent( // username 추출
				
						username -> memberRepository.findByUsername(username).ifPresent( // 유저 조회
						
								this::saveAuthentication // 인증 정보 저장
						)
				)
		);
		
		filterChain.doFilter(request,response);
	}
	
	private void saveAuthentication(Member member) {
		UserDetails user = User.builder()
				.username(member.getUsername())
				.password(member.getPassword())
				.roles(member.getRole().name())
				.build();
	
		Authentication authentication = new UsernamePasswordAuthenticationToken(
				user, null,authoritiesMapper.mapAuthorities(user.getAuthorities()));
	
	
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}

	private void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {


		memberRepository.findByRefreshToken(refreshToken).ifPresent(
				
				member -> jwtService.sendAccessToken(response, jwtService.createAccessToken(member.getUsername()))
				
		);
	}
}