package com.jpa2.domain.member.serivce;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jpa2.domain.member.Member;
import com.jpa2.domain.member.dto.MemberInfoDto;
import com.jpa2.domain.member.dto.MemberSignUpDto;
import com.jpa2.domain.member.dto.MemberUpdateDto;
import com.jpa2.domain.member.repository.MemberRepository;
import com.jpa2.global.util.security.SecurityUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	
	
	@Override
	public void signUp(MemberSignUpDto memberSignUpDto) throws Exception { // 회원가입 메서드
		Member member = memberSignUpDto.toEntity(); // 빌더패턴으로 회원 객체에 저장
		member.addUserAuthority(); // entity로 변환 후 USER 권한 부여
		member.encodePassword(passwordEncoder); // 패스워드 암호화
		
		if (memberRepository.findByUsername(memberSignUpDto.username()).isPresent()) { // 중복검사
			throw new Exception("이미 존재하는 아이디 입니다.");
		}
		
		memberRepository.save(member);
	}
	
	@Override
	public void update(MemberUpdateDto memberUpdateDto) throws Exception { // 회원정보 업데이트 메서드
		Member member = memberRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		
		memberUpdateDto.age().ifPresent(member::updateAge); // ifPresent -> 필드가 존재하는 경우에만 업데이트 진행
		memberUpdateDto.name().ifPresent(member::updateName); // :: -> member 객체의 메서드 참조
		memberUpdateDto.nickName().ifPresent(member::updateNickName);
	}
	
	@Override
	public void updatePassword(String checkPassword, String toBePassword) throws Exception { // 패스워드 업데이트 메서드
		Member member = memberRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		
		if (!member.matchPassword(passwordEncoder, checkPassword)) {
			throw new Exception("비밀번호가 일치하지 않습니다.");
		}
		member.updatePassword(passwordEncoder, toBePassword);
	}
	
	@Override
	public void withdraw(String checkPassword) throws Exception { // 탈퇴 메서드
		Member member = memberRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		
		if (!member.matchPassword(passwordEncoder, checkPassword)) {
			throw new Exception("비밀번호가 일치하지 않습니다.");
		}
		memberRepository.delete(member);
	}
	
	@Override
	public MemberInfoDto getinfo(Long id) throws Exception { // id로 회원정보를 조회하는 메서드
		Member findMember = memberRepository.findById(id).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		
		return new MemberInfoDto(findMember);
	}
	
	@Override
	public MemberInfoDto getMyInfo() throws Exception { // 내정보 가져오는 메서드
		Member findMember = memberRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
		
		return new MemberInfoDto(findMember);
	}
	
	
}