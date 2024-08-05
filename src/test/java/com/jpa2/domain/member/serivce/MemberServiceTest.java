package com.jpa2.domain.member.serivce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jpa2.domain.member.Member;
import com.jpa2.domain.member.Role;
import com.jpa2.domain.member.dto.MemberSignUpDto;
import com.jpa2.domain.member.repository.MemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {

	@Autowired
	EntityManager em;
	
	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	MemberService memberService;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	String PASSWORD = "password";
	
	private void clear() {
		em.flush();
		em.clear();
	}
	
	private MemberSignUpDto makeMemberSignUpDto() {
		return new MemberSignUpDto("username", PASSWORD, "name", "nickName", 20);
	}
	
	private MemberSignUpDto setMember() throws Exception {
		MemberSignUpDto memberSignUpDto = makeMemberSignUpDto();
		memberService.signUp(memberSignUpDto);
		clear();
		
		SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
		
		emptyContext.setAuthentication(new UsernamePasswordAuthenticationToken(User.builder()
				.username(memberSignUpDto.username())
				.password(memberSignUpDto.password())
				.roles(Role.USER.name())
				.build(),
				null, null)
		);
		
		SecurityContextHolder.setContext(emptyContext);
		
		return memberSignUpDto;
	}
	
	@AfterEach
	public void removeMember() {
		SecurityContextHolder.createEmptyContext().setAuthentication(null);
	}
	
	//== Test ==//
	/**
	 * 회원가입
	 * 	회원가입 시 아이디, 비밀번호, 이름, 별명, 나이를 입력하지 않으면 오류
	 * 	이미 존재하는 아이디가 있으면 오류
	 * 	회원가입 후 회원의 ROLE은 USER
	 */
//	@Test
	public void 회원가입_성공() throws Exception {
		// given
		MemberSignUpDto memberSignUpDto = makeMemberSignUpDto();
		
		// when
		memberService.signUp(memberSignUpDto);
		clear();
		
		// then TODO: 이곳 MEMBEREXCEPTION으로 고치기
		Member member = memberRepository.findByUsername(memberSignUpDto.username()).orElseThrow(() -> new Exception("회원이 없습니다."));
		
		assertThat(member.getId()).isNotNull();
		assertThat(member.getUsername()).isEqualTo(memberSignUpDto.username());
		assertThat(member.getName()).isEqualTo(memberSignUpDto.name());
		assertThat(member.getNickName()).isEqualTo(memberSignUpDto.nickName());
		assertThat(member.getAge()).isEqualTo(memberSignUpDto.age());
		assertThat(member.getRole()).isSameAs(Role.USER);
	}
	
//	아이디중복 테스트
	@Test
	public void 회원가입_실패_원인_아이디중복() throws Exception {
		// given
		MemberSignUpDto memberSignUpDto = makeMemberSignUpDto();
		memberService.signUp(memberSignUpDto);
		clear();
		
		// when, then
		assertThat(assertThrows(Exception.class, () -> memberService.signUp(memberSignUpDto)).getMessage()).isEqualTo("이미 존재하는 아이디 입니다.");
	}
	
	
	
}