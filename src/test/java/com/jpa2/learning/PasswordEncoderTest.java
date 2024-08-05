package com.jpa2.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.transaction.Transactional;

@SpringBootTest
class PasswordEncoderTest {
	
	@Autowired
	PasswordEncoder passwordEncoder;

//	비밀번호 암호화
//	@Test
	public void 패스워드_암호화() throws Exception {
		// given
		String password = "123123";
		
		// when
		String encodePassword = passwordEncoder.encode(password);
		
		// then
		assertThat(encodePassword).startsWith("{");
		assertThat(encodePassword).contains("{bcrypt}");
		assertThat(encodePassword).isNotEqualTo(password);
		
		// BCryptPasswordEncoder로 인코딩된 패스워드는 {bcrypt}를 시작부분에 포함해야함
	}

//	비밀번호 랜덤 암호화 -> 항상 다른 결과가 반환
//	@Test
	public void 패스워드_랜덤_암호화() throws Exception {
		// given
		String password = "1234";
		
		// when
		String encodePassword = passwordEncoder.encode(password);
		String encodePassword2 = passwordEncoder.encode(password);
		
		// then
		assertThat(encodePassword).isNotEqualTo(encodePassword2);
	}
	
//	암호화된 비밀번호 매치
	@Test
	public void 암호화된_비밀번호_매치() throws Exception {
		// given
		String password = "1234";
		
		// when
		String encodePassword = passwordEncoder.encode(password);
		
		// than
		assertThat(passwordEncoder.matches(password, encodePassword)).isTrue();
	}
}
