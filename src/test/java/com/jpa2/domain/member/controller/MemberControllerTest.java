package com.jpa2.domain.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpa2.domain.member.Member;
import com.jpa2.domain.member.dto.MemberSignUpDto;
import com.jpa2.domain.member.repository.MemberRepository;
import com.jpa2.domain.member.serivce.MemberService;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class MemberControllerTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	EntityManager em;
	
	@Autowired
	MemberService memberService;
	
	@Autowired
	MemberRepository memberRepository;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	private static String SIGN_UP_URL = "/signUp";
	
	private String username = "username";
	private String password = "password1234@";
	private String name = "name";
	private String nickName = "nickName";
	private Integer age = 20;
	
	private void clear() { 
		em.flush();
		em.clear();
	}
	
	private void signUp(String signUpData) throws Exception {
		mockMvc.perform(
				post(SIGN_UP_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(signUpData)
		)
		.andExpect(status().isOk());
	}
	
	@Value("${jwt.access.header}")
	private String accessHeader;
	
	private static final String BEARER = "Bearer ";
	
	private String getAccessToken() throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put("username", username);
		map.put("password", password);
		
		MvcResult result = mockMvc.perform( // 로그인 요청
				post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(map))
		)
		.andExpect(status().isOk())
		.andReturn();
			
		return result.getResponse().getHeader(accessHeader);
	}
	
	
	
//	회원가입
//	@Test
	public void 회원가입_성공() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		
		// when
		signUp(signUpData);
		
		// then
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		assertThat(member.getName()).isEqualTo(name);
		assertThat(memberRepository.findAll().size()).isEqualTo(1);
	}

//	값이 null일때 회원가입
//	@Test
	public void 회원가입_실패_필드가_없음() throws Exception {
		// given
		String noUsernameSignUpData = objectMapper.writeValueAsString(new MemberSignUpDto(null, password, name, nickName, age));
		String noPasswordSignUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, null, name, nickName, age));
		String noNameSignUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, null, nickName, age));
		String noNickNameSignUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, null, age));
		String noAgeSignUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, null));
		
		// when, then
		signUp(noUsernameSignUpData);
		signUp(noPasswordSignUpData);
		signUp(noNameSignUpData);
		signUp(noNickNameSignUpData);
		signUp(noAgeSignUpData);
		// 예외가 발생하더라도 상태코드 200
		
		assertThat(memberRepository.findAll().size()).isEqualTo(0);
	}
	
//	회원정보 수정
//	@Test
	public void 회원정보수정_성공() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		Map<String, Object> map = new HashMap<>();
		map.put("name", name + "변경");
		map.put("nickName", nickName + "변경");
		map.put("age", age + 1);
		
		String updateMemberData = objectMapper.writeValueAsString(map);
		
		// when
		mockMvc.perform(
				put("/member")
					.header(accessHeader, BEARER + accessToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateMemberData))
			.andExpect(status().isOk());
		
		// then
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		assertThat(member.getName()).isEqualTo(name + "변경");
		assertThat(member.getNickName()).isEqualTo(nickName + "변경");
		assertThat(member.getAge()).isEqualTo(age + 1);
		assertThat(memberRepository.findAll()).size().isEqualTo(1);
	}
	
//	원하는 필드만 변경
//	@Test
	public void 회원정보수정_원하는필드만변경_성공() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		Map<String, Object> map = new HashMap<>();
		map.put("name", name + "변경");
		String updateMemberData = objectMapper.writeValueAsString(map);
		
		// when
		mockMvc.perform(
				put("/member")
					.header(accessHeader, BEARER + accessToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateMemberData))
			.andExpect(status().isOk());
		
		// then
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		assertThat(member.getName()).isEqualTo(name + "변경");
		assertThat(member.getNickName()).isEqualTo(nickName);
		assertThat(member.getAge()).isEqualTo(age);
		assertThat(memberRepository.findAll().size()).isEqualTo(1);
	}
	
//	비밀번호 수정
//	@Test
	public void 비밀번호수정_성공() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		Map<String, Object> map = new HashMap<>();
		map.put("checkPassword", password);
		map.put("toBePassword", password + "!@#!@#!@#");
		
		String updatePassword = objectMapper.writeValueAsString(map);
		
		// when
		mockMvc.perform(
				put("/member/password")
					.header(accessHeader, BEARER + accessToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(updatePassword))
			.andExpect(status().isOk());
		
		// then
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		assertThat(passwordEncoder.matches(password, member.getPassword())).isFalse();
		assertThat(passwordEncoder.matches(password + "!@#!@#!@#", member.getPassword())).isTrue();
	}
	
//	@Test
	public void 비밀번호수정_실패_검증비밀번호가_틀림() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		Map<String, Object> map = new HashMap<>();
		map.put("checkPassword", password + "1");
		map.put("toBePassword", password + "!@#!@#!@#");
		
		String updatePassword = objectMapper.writeValueAsString(map);
		
		// when
		mockMvc.perform(
				put("/member/password")
					.header(accessHeader, BEARER + accessToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(updatePassword))
			.andExpect(status().isOk());
		
		// then
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		assertThat(passwordEncoder.matches(password, member.getPassword())).isTrue();
		assertThat(passwordEncoder.matches(password + "!@#!@#!@#", member.getPassword())).isFalse();
	}
	
//	비밀번호 형식이 올바르지 않음
//	@Test
	public void 비밀번호수정_실패_바꾸려는_비밀번호_형식_올바르지않음() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		Map<String, Object> map = new HashMap<>();
		map.put("checkPassword", password);
		map.put("toBePassword", "123123");
		
		String updatePassword = objectMapper.writeValueAsString(map);
		
		// when
		mockMvc.perform(
				put("/member/password")
					.header(accessHeader, BEARER + accessToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(updatePassword))
			.andExpect(status().isOk());
		
		// then
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		assertThat(passwordEncoder.matches(password, member.getPassword())).isTrue();
		assertThat(passwordEncoder.matches("123123", member.getPassword())).isFalse();
	}
	
//	회원탈퇴
//	@Test
	public void 회원탈퇴_성공() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		Map<String, Object> map = new HashMap<>();
		map.put("checkPassword", password);
		
		String updatePassword = objectMapper.writeValueAsString(map);
		
		// when
		mockMvc.perform(
				delete("/member")
					.header(accessHeader, BEARER + accessToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(updatePassword))
			.andExpect(status().isOk());
		
		// then
		assertThrows(Exception.class, () -> memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다.")));
	}
	
//	회원탈퇴 시 비밀번호 틀림
//	@Test
	public void 회원탈퇴_실패_비밀번호틀림() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		Map<String, Object> map = new HashMap<>();
		map.put("checkPassword", password + 11);
		
		String updatePassword = objectMapper.writeValueAsString(map);
		
		// when
		mockMvc.perform(
				delete("/member")
					.header(accessHeader, BEARER + accessToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(updatePassword))
			.andExpect(status().isOk());
		
		// then
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		assertThat(member).isNotNull();
	}
	
//	권한이 없는 상태에서 회원탈퇴
//	@Test
	public void 회원탈퇴_실패_권한이없음() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		Map<String, Object> map = new HashMap<>();
		map.put("checkPassword", password);
		
		String updatePassword = objectMapper.writeValueAsString(map);
		
		// when
		mockMvc.perform(
				delete("/member")
					.header(accessHeader, BEARER + accessToken + "1")
					.contentType(MediaType.APPLICATION_JSON)
					.content(updatePassword))
			.andExpect(status().isForbidden());
		
		// then
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		assertThat(member).isNotNull();
	}
	
//	내정보 조회
//	@Test
	public void 내정보조회_성공() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		// when
		MvcResult result = mockMvc.perform(
				get("/member")
					.characterEncoding(StandardCharsets.UTF_8)
					.header(accessHeader, BEARER + accessToken))
			.andExpect(status().isOk())
			.andReturn();
		
		// then
		Map<String, Object> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		assertThat(member.getAge()).isEqualTo(map.get("age"));
		assertThat(member.getUsername()).isEqualTo(map.get("username"));
		assertThat(member.getName()).isEqualTo(map.get("name"));
		assertThat(member.getNickName()).isEqualTo(map.get("nickName"));
	}
	
//	JWT 없음
//	@Test
	public void 내정보조회_실패_JWT없음() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		// when, then
		mockMvc.perform(
				get("/member")
					.characterEncoding(StandardCharsets.UTF_8)
					.header(accessHeader, BEARER + accessToken + 1))
			.andExpect(status().isForbidden());
	}
	
	/**
     * 회원정보조회 성공
     * 회원정보조회 실패 -> 회원이없음
     * 회원정보조회 실패 -> 권한이없음
     */
//	@Test
	public void 회원정보조회_성공() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		Long id = memberRepository.findAll().get(0).getId();
		
		// when
		MvcResult result = mockMvc.perform(
				get("/member/" + id)
					.characterEncoding(StandardCharsets.UTF_8)
					.header(accessHeader, BEARER + accessToken))
			.andExpect(status().isOk())
			.andReturn();
		
		// then
		Map<String, Object> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		assertThat(member.getAge()).isEqualTo(map.get("age"));
		assertThat(member.getUsername()).isEqualTo(map.get("username"));
		assertThat(member.getName()).isEqualTo(map.get("name"));
        assertThat(member.getNickName()).isEqualTo(map.get("nickName"));
	}
	
//	@Test
	public void 회원정보조회_실패_없는회원조회() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		// when
		MvcResult result = mockMvc.perform(
				get("/member/2211")
					.characterEncoding(StandardCharsets.UTF_8)
					.header(accessHeader, BEARER + accessToken))
			.andExpect(status().isOk())
			.andReturn();
		
		// then
		assertThat(result.getResponse().getContentAsString()).isEqualTo(""); //빈 문자열
	}
	
	@Test
	public void 회원정보조회_실패_JWT없음() throws Exception {
		// given
		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));
		signUp(signUpData);
		
		String accessToken = getAccessToken();
		
		// when, then
		mockMvc.perform(
				get("/member/1")
					.characterEncoding(StandardCharsets.UTF_8)
					.header(accessHeader, BEARER + accessToken + 1))
			.andExpect(status().isForbidden());
	}
}