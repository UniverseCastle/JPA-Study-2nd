package com.jpa2.global.jwt.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpa2.domain.member.Member;
import com.jpa2.domain.member.Role;
import com.jpa2.domain.member.repository.MemberRepository;
import com.jpa2.global.jwt.service.JwtService;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class JwtFilterAuthenticationTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	EntityManager em;
	
	@Autowired
	JwtService jwtService;
	
	PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
	
	@Value("${jwt.secret}")
	private String secret;
	@Value("${jwt.access.header}")
	private String accessHeader;
	@Value("${jwt.refresh.header}")
	private String refreshHeader;

	private static String KEY_USERNAME = "username";
	private static String KEY_PASSWORD = "password";
	private static String USERNAME = "username";
	private static String PASSWORD = "password";
	
	private static String LOGIN_URL = "/login";
	
	private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
	private static final String BEARER = "Bearer ";
	
	private ObjectMapper objectMapper = new ObjectMapper();

	private void clear() {
		em.flush();
		em.clear();
	}
	
	@BeforeEach
	private void init() {
		memberRepository.save(Member.builder()
				.username(USERNAME)
				.password(delegatingPasswordEncoder.encode(PASSWORD))
				.name("Member1")
				.nickName("NickName1")
				.role(Role.USER)
				.age(22)
				.build());
		clear();
	}
	
	private Map getUsernamePasswordMap(String username, String password) {
		Map<String, String> map = new HashMap<>();
		map.put(KEY_USERNAME, username);
		map.put(KEY_PASSWORD, password);
		
		return map;
	}
	
	private Map getAccessAndRefreshToken() throws Exception {
		Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);
		
		MvcResult result = mockMvc.perform(
				post(LOGIN_URL) // 로그인 URL로 POST 요청
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(map))) // 요청 본문에 JSON 데이터 추가
			.andReturn();
		// Access Token과 Refresh Token 헤더 추출
		String accessToken = result.getResponse().getHeader(accessHeader);
		String refreshToken = result.getResponse().getHeader(refreshHeader);
		// 토큰 정보를 담을 맵 생성
		Map<String, String> tokenMap = new HashMap<>();
		tokenMap.put(accessHeader, accessToken);
		tokenMap.put(refreshHeader, refreshToken);
		
		return tokenMap;
	}
	
	
	//== Test ==//
	/**
     * AccessToken : 존재하지 않음,
     * RefreshToken : 존재하지 않음
     */
//	@Test
	public void Access_Refresh_모두_존재_x() throws Exception {
		// when, then
		mockMvc.perform(
				get(LOGIN_URL + "123") // login이 아닌 다른 임의의 주소
		)
				.andExpect(status().isForbidden()
		);
	}

	/**
	 * AccessToken : 유효,
	 * RefreshToken : 존재하지 않음
	 */
//	@Test
	public void AccessToken만_보내서_인증() throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String accessToken = (String)accessAndRefreshToken.get(accessHeader); // KEY
		
		// when, then
		mockMvc.perform(
				get(LOGIN_URL + "123")
				.header(accessHeader, BEARER + accessToken) //login이 아닌 다른 임의의 주소
		)
				.andExpectAll(status().isNotFound()); // 없는 주소로 보냈으므로 NotFound
	}
	
	/**
	 * AccessToken : 유효하지 않음,
	 * RefreshToken : 존재하지 않음
	 */
//	@Test
	public void 유효하지_않은_AccessToken만_보내서_인증x_상태코드는_403() throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String accessToken = (String)accessAndRefreshToken.get(accessHeader);
		
		// when
		mockMvc.perform(
				get(LOGIN_URL + "123")
				.header(accessHeader, accessToken + "1") // login이 아닌 다른 임의의 주소
		)
				.andExpectAll(status().isForbidden()); // 없는 주소로 보냈으므로 NotFound
	}
	
	/**
	 * AccessToken : 존재하지 않음
	 * RefreshToken : 유효
	 */
//	@Test
	public void 유효한RefreshToken만_보내서_AccessToken_재발급_200() throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String refreshToken = (String)accessAndRefreshToken.get(refreshHeader);
		
		// when, then
		MvcResult result = mockMvc.perform(
				get(LOGIN_URL + "123").header(refreshHeader, BEARER + refreshToken) // login이 아닌 다른 임의의 주소
		)
				.andExpect(status().isOk()) // 응답 상태 200 OK 인지 검증
				.andReturn(); // 결과 반환
		
		String accessToken = result.getResponse().getHeader(accessHeader);
		
		String subject = JWT.require(Algorithm.HMAC512(secret)).build().verify(accessToken).getSubject();
		assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
	}
	
	/**
	 * AccessToken : 존재하지 않음
	 * RefreshToken : 유효하지 않음
	 */
//	@Test
	public void 유효하지_않은_RefreshToken만_보내면_403() throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String refreshToken = (String)accessAndRefreshToken.get(refreshHeader);
		
		// when, then
		mockMvc.perform(
				get(LOGIN_URL + "123")
				.header(refreshHeader, refreshToken) // Bearer를 붙이지 않음
		)
				.andExpect(status().isForbidden());
		
		mockMvc.perform(
				get(LOGIN_URL + "123")
				.header(refreshHeader, BEARER + refreshToken + "1")) // 유효하지 않은 토큰
				.andExpect(status().isForbidden());
	}
	
	/**
	 * AccessToken : 유효
	 * RefreshToken : 유효
	 */
//	@Test
	public void 유효한RefreshToken과_유효한AccessToken_같이보낼때_AccessToken_재발급_200 () throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String accessToken = (String)accessAndRefreshToken.get(accessHeader);
		String refreshToken = (String)accessAndRefreshToken.get(refreshHeader);
		
		// when, then
		MvcResult result = mockMvc.perform(
				get(LOGIN_URL + "123")
				.header(refreshHeader, BEARER + refreshToken)
				.header(accessHeader, BEARER + accessToken)
		)
				.andExpect(status().isOk())
				.andReturn();
		
		String responseAccessToken = result.getResponse().getHeader(accessHeader);
		String responseRefreshToken = result.getResponse().getHeader(refreshHeader);
		
		String subject = JWT.require(Algorithm.HMAC512(secret)).build().verify(responseAccessToken).getSubject();
		
		assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
		assertThat(responseRefreshToken).isNull(); // refreshToken은 재발급 되지 않음
	}
	
	/**
	 * AccessToken : 유효하지 않음
	 * RefreshToken : 유효
	 */
//	@Test
	public void 유효한RefreshToken과_유효하지_않은_AccessToken_같이보냈을때_AccessToken_재발급_200() throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String accessToken = (String)accessAndRefreshToken.get(accessHeader);
		String refreshToken = (String)accessAndRefreshToken.get(refreshHeader);
		
		// when, then
		MvcResult result = mockMvc.perform(
				get(LOGIN_URL+"123")
				.header(refreshHeader, BEARER + refreshToken)
				.header(accessHeader, BEARER + accessToken + 1) // access토큰이 유효하지 않음
		)
				.andExpect(status().isOk())
				.andReturn();
		
		String responseAccessToken = result.getResponse().getHeader(accessHeader);
		String responseRefreshToken = result.getResponse().getHeader(refreshHeader);
		
		String subject = JWT.require(Algorithm.HMAC512(secret)).build().verify(responseAccessToken).getSubject();
		
		assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
		assertThat(responseRefreshToken).isNull(); // refreshToken은 재발급되지 않음
	}
	
	/**
	 * AccessToken : 유효
	 * RefreshToken : 유효하지 않음
	 */
//	@Test
	public void 유효하지않은_RefreshToken과_유효한AccessToken_같이보낼때_상태코드200_혹은404_RefreshToken은_AccessToken모두_재발급되지않음() throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String accessToken = (String)accessAndRefreshToken.get(accessHeader);
		String refreshToken = (String)accessAndRefreshToken.get(refreshHeader);
		
		// when, then
		MvcResult result = mockMvc.perform(
				get(LOGIN_URL + "123")
				.header(refreshHeader, BEARER + refreshToken + 1)
				.header(accessHeader, BEARER + accessToken)
		)
				.andExpect(status().isNotFound()) // 없는 주소로 보냈으므로 NotFound
				.andReturn();
		
		String responseAccessToken = result.getResponse().getHeader(accessHeader);
		String responseRefreshToken = result.getResponse().getHeader(refreshHeader);
		
		assertThat(responseAccessToken).isNull(); // accessToken은 재발급 되지 않음
		assertThat(responseRefreshToken).isNull(); // refreshToken은 재발급 되지 않음
	}
	
	/**
	 * AccessToken : 유효하지 않음
	 * RefreshToken : 유효하지 않음
	 */
//	@Test
	public void 유효하지않은RefreshToken과_유효하지않은AccessToken_같이보냈을때_403() throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String accessToken = (String)accessAndRefreshToken.get(accessHeader);
		String refreshToken = (String)accessAndRefreshToken.get(refreshHeader);
		
		// when, then
		MvcResult result = mockMvc.perform(
				get(LOGIN_URL + "123")
				.header(refreshHeader, BEARER + refreshToken + 1)
				.header(accessHeader, BEARER + accessToken + 1)
		)
				.andExpect(status().isForbidden()) // 없는 주소로 보냈으므로 NotFound
				.andReturn();
		
		String responseAccessToken = result.getResponse().getHeader(accessHeader);
		String responseRefreshToken = result.getResponse().getHeader(refreshHeader);
		
		assertThat(responseAccessToken).isNull(); // 재발급 x
		assertThat(responseRefreshToken).isNull(); // 재발급 x
	}
	
//	로그인 주소로 보내면 필터 작동x
//	@Test
	public void 로그인_주소로_보내면_필터작동_x() throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String accessToken = (String)accessAndRefreshToken.get(accessHeader);
		String refreshToken = (String)accessAndRefreshToken.get(refreshHeader);
		
		// when, then
		MvcResult result = mockMvc.perform(
				post(LOGIN_URL) // get인 경우 config에서 permitAll 했기 때문에 notFound
				.header(refreshHeader, BEARER + refreshToken)
				.header(accessHeader, BEARER + accessToken)
		)
				.andExpect(status().isOk())
				.andReturn();
	}
}