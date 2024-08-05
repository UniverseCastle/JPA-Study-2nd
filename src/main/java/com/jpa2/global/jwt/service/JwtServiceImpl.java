package com.jpa2.global.jwt.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jpa2.domain.member.repository.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Setter(value = AccessLevel.PRIVATE)
@Transactional
@Slf4j
public class JwtServiceImpl implements JwtService {

	/**
	 * @Vaule
	 * yml 파일에 작성해둔 설정 값들을 가져와서 사용
	 * static으로 선언 시 값이 들어오지 않음
	 */
	@Value("${jwt.secret}")
	private String secret;
	@Value("${jwt.access.expiration}")
	private long accessTokenValidityInSeconds;
	@Value("${jwt.refresh.expiration}")
	private long refreshTokenValidityInSeconds;
	@Value("${jwt.access.header}")
	private String accessHeader;
	@Value("${jwt.refresh.header}")
	private String refreshHeader;
	
	/**
	 * Subject와, Claim으로 username을 사용할 것이기에
	 * 클레임의 name을 "username"으로 지정
	 * 'Authorization = Bearer [토큰]' 형식 -> 미리 지정해 줌
	 */
	private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
	private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
	private static final String USERNAME_CLAIM = "username";
	private static final String BEARER = "Bearer ";
	
	private final MemberRepository memberRepository;
	
	@Override
	public String createAccessToken(String username) {
		return JWT.create()
				.withSubject(ACCESS_TOKEN_SUBJECT)
				.withExpiresAt(new Date(System.currentTimeMillis() + accessTokenValidityInSeconds * 1000))
				.withClaim(USERNAME_CLAIM, username)
				.sign(Algorithm.HMAC512(secret));
	}
	
	@Override
	public String createRefreshToken() {
		return JWT.create()
				.withSubject(REFRESH_TOKEN_SUBJECT)
				.withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValidityInSeconds * 1000))
				.sign(Algorithm.HMAC512(secret));
	}
	
	@Override
	public void updateRefreshToken(String username, String refreshToken) {
		memberRepository.findByUsername(username)
			.ifPresentOrElse(
					member -> member.updateRefreshToken(refreshToken),
					() -> new Exception("회원이 없습니다.")
			);
		
	}
	@Override
	public void destroyRefreshToken(String username) {
		memberRepository.findByUsername(username)
			.ifPresentOrElse(
					member -> member.destroyRefreshToken(),
					() -> new Exception("회원이 없습니다."));
	}
	
	@Override
	public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
		response.setStatus(HttpServletResponse.SC_OK);
		
		setAccessTokenHeader(response, accessToken);
		setRefreshTokenHeader(response, refreshToken);
		
		Map<String, String> tokenMap = new HashMap<>();
		tokenMap.put(ACCESS_TOKEN_SUBJECT, accessToken);
		tokenMap.put(REFRESH_TOKEN_SUBJECT, refreshToken);
	}
	
	@Override
    public void sendAccessToken(HttpServletResponse response, String accessToken){
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);


        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(ACCESS_TOKEN_SUBJECT, accessToken);
    }
	
	@Override
	public Optional<String> extractAccessToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(accessHeader)).filter(
				
				accessToken -> accessToken.startsWith(BEARER)
		// filter 통과한 엑세스 토큰에서 BEARER 문자열 제거
		).map(accessToken -> accessToken.replace(BEARER, "")); // 조건 만족하는 경우에만 변환 작업 수행
	}
	
	@Override
	public Optional<String> extractRefreshToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(refreshHeader)).filter(
				
				refreshToken -> refreshToken.startsWith(BEARER)
				
		).map(refreshtoken -> refreshtoken.replace(BEARER, ""));
	}
	
	@Override
	public Optional<String> extractUsername(String accessToken) {
		
		try {
			return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secret)).build().verify(accessToken).getClaim(USERNAME_CLAIM).asString());
		} catch (Exception e) {
			log.error(e.getMessage());
			return Optional.empty();
		}
	}
	
	@Override
	public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
		response.setHeader(accessHeader, accessToken);
	}
	
	@Override
	public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
		response.setHeader(refreshHeader, refreshToken);
	}
	
	@Override
    public boolean isTokenValid(String token){
        try {
            JWT.require(Algorithm.HMAC512(secret)).build().verify(token);
            return true;
        }catch (Exception e){
            log.error("유효하지 않은 Token입니다", e.getMessage());
            return false;
        }
    }
}