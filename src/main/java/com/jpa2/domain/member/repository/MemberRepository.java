package com.jpa2.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpa2.domain.member.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByUsername(String username);
	
	boolean existsByUsername(String username);
	
	Optional<Member> findByRefreshToken(String refreshToken);
}