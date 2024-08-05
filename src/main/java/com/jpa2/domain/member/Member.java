package com.jpa2.domain.member;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.jpa2.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MEMBER")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id; // PK
	
	@Column(nullable = false, length = 30, unique = true)
	private String username; // 아이디
	
	private String password; // 비밀번호
	
	@Column(nullable = false, length = 30)
	private String name; // 이름(실명)
	
	@Column(nullable = false, length = 30)
	private String nickName; // 별명
	
	@Column(nullable = false, length = 30)
	private Integer age; // 나이
	
	private Role role; // 권한 -> USER, ADMIN
	
	@Column(length = 1000)
	private String refreshToken; // RefreshToken
	
	//== 정보 수정 ==//
	public void updatePassword(PasswordEncoder passwordEncoder, String password) {
		this.password = passwordEncoder.encode(password);
	}
	
	public void updateName(String name) {
		this.name = name;
	}
	
	public void updateNickName(String nickName) {
		this.nickName = nickName;
	}
	
	public void updateAge(int age) { // Integer로 자동으로 박싱(boxing)
		this.age = age;
	}
	
	public void updateRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public void destroyRefreshToken() {
		this.refreshToken = null;
	}
	
	//== 패스워드 암호화 ==//
	public void encodePassword(PasswordEncoder passwordEncoder) {
		this.password = passwordEncoder.encode(password);
	}
}