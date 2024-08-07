package com.jpa2.global.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpa2.domain.member.Member;
import com.jpa2.domain.member.Role;
import com.jpa2.domain.member.repository.MemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

// 기본 로그인 테스트
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class LoginTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	EntityManager em;
	
	PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	private static String KEY_USERNAME = "username";
	private static String KEY_PASSWORD = "password";
	private static String USERNAME = "username";
	private static String PASSWORD = "123456789";
	private static String LOGIN_URL = "/login";
	
	@Value("${jwt.access.header}")
	private String accessHeader;
	@Value("${jwt.refresh.header}")
	private String refreshHeader;
	
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
				.age(20)
				.build());
		clear();
	}
	
	private Map getUsernamePasswordMap(String username, String password) {
		Map<String, String> map = new HashMap<>();
		map.put(KEY_USERNAME, username);
		map.put(KEY_PASSWORD, password);
		
		return map;
	}
	
	private ResultActions perform(String url, MediaType mediaType, Map usernamePasswordMap) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders
				.post(url)
				.contentType(mediaType)
				.content(objectMapper.writeValueAsString(usernamePasswordMap)));
	}
	
//== 테스트 ==//
	
//	@Test
	public void 로그인_성공() throws Exception {
		// given
		Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);
		
		// when
		MvcResult result = perform(LOGIN_URL, MediaType.APPLICATION_JSON, map)
				.andDo(print())
				.andExpect(status().is3xxRedirection()) // 302 상태 코드
				.andReturn();
		
		// then
//		assertThat(result.getResponse().getHeader(accessHeader)).isNotNull();
//		assertThat(result.getResponse().getHeader(refreshHeader)).isNotNull();
	}

//	로그인 실패 - 아이디 오류
//	@Test
	public void 로그인_실패_아이디틀림() throws Exception {
		// given
		Map<String, String> map = new HashMap<>();
		map.put("username",USERNAME+"123");
		map.put("password",PASSWORD);
		
		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders
				.post(LOGIN_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(map)))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andReturn();
		
		//then
		assertThat(result.getResponse().getHeader(accessHeader)).isNull();
		assertThat(result.getResponse().getHeader(refreshHeader)).isNull();
	}
	
//	로그인 실패 - 비밀번호 오류
//	@Test
	public void 로그인_실패_비밀번호틀림() throws Exception {
		// given
		Map<String, String> map = new HashMap<>();
		map.put("username",USERNAME);
		map.put("password",PASSWORD + "123");
		
		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders
				.post(LOGIN_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(map)))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andReturn();
		
		//then
		assertThat(result.getResponse().getHeader(accessHeader)).isNull();
		assertThat(result.getResponse().getHeader(refreshHeader)).isNull();
	}
	
//	로그인 주소 틀리면 Forbidden
//	@Test
	public void 로그인_주소가_틀리면_FORBIDDEN() throws Exception {
		// given
		Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);
		
		// when, then
		perform(LOGIN_URL + "123", MediaType.APPLICATION_JSON, map)
				.andDo(print())
				.andExpect(status().isForbidden());
	}
	
//	로그인 형식 JOSN이 아니면 200
//	@Test
	public void 로그인_데이터형식_JSON이_아니면_401() throws Exception {
		// given
		Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);
		
		// when, then
		perform(LOGIN_URL, MediaType.APPLICATION_FORM_URLENCODED, map)
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andReturn();
	}
	
//	로그인 Http Method가 Post가 아니면 404 NotFound
//	@Test
	public void 로그인_HTTP_METHOD_GET이면_NOTFOUND() throws Exception {
		// given
		Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);
		
		// when
		mockMvc.perform(MockMvcRequestBuilders
				.get(LOGIN_URL) // Get으로 보냄
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.content(objectMapper.writeValueAsString(map)))
			.andDo(print())
			.andExpect(status().isNotFound());
	}
	
	
}