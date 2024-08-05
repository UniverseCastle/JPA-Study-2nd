package com.jpa2.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpa2.domain.member.repository.MemberRepository;
import com.jpa2.domain.member.serivce.LoginService;
import com.jpa2.global.jwt.filter.JwtAuthenticationProcessingFilter;
import com.jpa2.global.jwt.service.JwtService;
import com.jpa2.global.login.filter.JsonUsernamePasswordAuthenticationFilter;
import com.jpa2.global.login.handler.LoginFailureHandler;
import com.jpa2.global.login.handler.LoginSuccessJWTProvideHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final LoginService loginService;
	private final ObjectMapper objectMapper;
	private final MemberRepository memberRepository;
	private final JwtService jwtService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
					.requestMatchers("/", "login", "/signUp").permitAll()
					.anyRequest().authenticated())
			;
		http.addFilterAfter(jsonUsernamePasswordAuthenticationFilter(), LogoutFilter.class); // 로그아웃 이후에 필터 실행
		http.addFilterBefore(jwtAuthenticationProcessingFilter(), JsonUsernamePasswordAuthenticationFilter.class); // JSON 기반 사용자 인증 필터보다 먼저 JWT인증 필터 실행
			
		return http.build();
	}
	
	// 패스워드 암호화
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	
	@Bean
	public AuthenticationManager authenticationManager() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); // FormLogin과 동일하게 DaoAuthenticationProvider 사용
		provider.setPasswordEncoder(passwordEncoder()); // passwordEncoder로는 PasswordEncoderFactories.createDelegatingPasswordEncoder() 사용
		provider.setUserDetailsService(loginService);
		
		return new ProviderManager(provider);
	}
	
	@Bean
	public LoginSuccessJWTProvideHandler loginSuccessJWTProvideHandler() {
		return new LoginSuccessJWTProvideHandler(jwtService, memberRepository);
	}
	
	@Bean
	public LoginFailureHandler loginFailureHandler() {
		return new LoginFailureHandler();
	}
	
	/**
	 * AuthenticationManager 등록하지 않으면 오류 발생
	 * authenticationManager must be specified => authenticationManager를 지정해 주어야 한다는 오류
	 */
	@Bean
	public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter() {
		JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter = new JsonUsernamePasswordAuthenticationFilter(objectMapper);
		
        jsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
        jsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessJWTProvideHandler());
        jsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());//변경
        
        return jsonUsernamePasswordLoginFilter;
	}
	
	@Bean
	public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
		JwtAuthenticationProcessingFilter jsonusernamePasswordLoginFilter = new JwtAuthenticationProcessingFilter(jwtService, memberRepository);
		
		return jsonusernamePasswordLoginFilter;
	}
}