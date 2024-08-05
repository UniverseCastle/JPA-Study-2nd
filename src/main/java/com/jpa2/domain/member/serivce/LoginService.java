package com.jpa2.domain.member.serivce;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jpa2.domain.member.Member;
import com.jpa2.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("아이디가 없습니다."));
		
		return User.builder()
				.username(member.getUsername())
				.password(member.getPassword())
				.roles(member.getRole().name())
				.build();
	}
	
	
}