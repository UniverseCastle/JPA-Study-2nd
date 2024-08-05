package com.jpa2.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.jpa2.domain.member.Member;
import com.jpa2.domain.member.Role;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional // 각 테스트가 끝난 후 롤백하여 데이터베이스 상태를 유지
class MemberRepositoryTest {
	
	@Autowired
	MemberRepository memberRepository; // MemberRepository 인터페이스를 주입받아 사용
	
	@Autowired
	EntityManager em; // JPA의 EntityManager를 주입받아 데이터베이스 작업을 수행

	private void clear(){
        em.flush();
        em.clear();
    }
	
	@AfterEach // 각 테스트 메소드가 실행된 후 호출되는 메서드
	private void after() {
		em.clear(); // EntityManager의 영속성 컨텍스트를 초기화하여 이전 상태를 지움
	}
	
//	회원가입
//	@Test
	public void 회원저장_성공() throws Exception {
		// given *필요한 상태나 조건을 설정
		Member member = Member.builder()
				.username("username")
				.password("1234567890")
				.name("Member1")
				.nickName("NickName1")
				.role(Role.USER)
				.age(20)
				.build();
		
		// when *이벤트 발생
		Member saveMember = memberRepository.save(member);
		
		// then *검증
		Member findMember = memberRepository.findById(saveMember.getId()).orElseThrow(() -> new RuntimeException("저장된 회원이 없습니다.")); //아직 예외 클래스를 만들지 않았기에 RuntimeException으로 처리
		
		// 동일한 객체인지 검증
		assertThat(findMember).isSameAs(saveMember);
		assertThat(findMember).isSameAs(member);
	}
	
//	아이디 없이 회원가입
//	@Test
	public void 오류_회원가입시_아이디가_없음() throws Exception {
		// given
		Member member = Member.builder()
				.password("1234567890")
				.name("Member1")
				.nickName("NickName1")
				.role(Role.USER)
				.age(20)
				.build();
		
		// when, then
		assertThrows(Exception.class, () -> memberRepository.save(member));
		
		// assertThrows: 특정 코드 블록이 주어진 예외를 발생시키는지를 검증
		// 첫 번째 인자는 기대하는 예외 클래스, 두 번째 인자는 테스트할 코드 블록
	}
	
//	이름 없이 회원가입
//	@Test
	public void 오류_회원가입시_이름이_없음() throws Exception{
		// given
		Member member = Member.builder()
				.username("username")
				.password("1234567890")
				.nickName("NickName1")
				.role(Role.USER)
				.age(20)
				.build();
		
		// when, then
		assertThrows(Exception.class, () -> memberRepository.save(member));
	}
	
//	닉네임 없이 회원가입
//	@Test
	public void 오류_회원가입시_닉네임이_없음() {
		// given
		Member member = Member.builder()
				.username("username")
				.password("1234567890")
				.name("Member1")
				.role(Role.USER)
				.age(20)
				.build();
		
		// when, then
		assertThrows(Exception.class, () -> memberRepository.save(member));
	}
	
//	나이 없이 회원가입
//	@Test
	public void 오류_회원가입시_나이가_없음() throws Exception {
		// given
		Member member = Member.builder()
				.username("username")
				.password("1234567890")
				.name("Member1")
				.nickName("NickName1")
				.role(Role.USER)
				.build();

		// when, then
		assertThrows(Exception.class, () -> memberRepository.save(member));
	}
	
//	회원가입 시 중복된 아이디 있으면 오류
//	@Test
	public void 오류_회원가입시_중복된_아이디가_있음() throws Exception {
		// given
		Member member1 = Member.builder()
				.username("username")
				.password("1234567890")
				.name("Member1")
				.role(Role.USER)
				.age(20)
				.build();
		Member member2 = Member.builder()
				.username("username")
				.password("1234567890")
				.name("Member1")
				.role(Role.USER)
				.age(20)
				.build();
		
		// when, then
		assertThrows(Exception.class, () -> memberRepository.save(member2));
	}
	
//	회원수정
//	@Test
	public void 성공_회원수정() throws Exception {
		// given
		Member member1 = Member.builder()
				.username("username")
				.password("1234567890")
				.name("Member1")
				.role(Role.USER)
				.nickName("NickName1")
				.age(20)
				.build();
		memberRepository.save(member1);
		clear(); // 영속성 컨텍스트 초기화
		
		String updatePassword = "updatePassword"; // 업데이트할 비밀번호
		String updateName = "updateName"; // 업데이트할 이름
		String updateNickName = "updateNickName"; // 업데이트할 닉네임
		int updateAge = 30; // 업데이트할 나이
		
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // 비밀번호 인코더 생성
		
		// when
		Member findMember = memberRepository.findById(member1.getId()).orElseThrow(() -> new Exception()); // 저장된 member1을 조회
		findMember.updateAge(updateAge); // 업데이트
		findMember.updateName(updateName);
		findMember.updateNickName(updateNickName);
		findMember.updatePassword(passwordEncoder, updatePassword);
		em.flush(); // 변경 내용을 데이터베이스에 반영
		
		// then
		Member findUpdateMember = memberRepository.findById(findMember.getId()).orElseThrow(() -> new Exception()); // 업데이트된 회원 다시 조회
		
		assertThat(findUpdateMember).isSameAs(findMember); // 같은 객체인지 검증
		assertThat(passwordEncoder.matches(updatePassword, findUpdateMember.getPassword())).isTrue();
		assertThat(findUpdateMember.getName()).isEqualTo(updateName);
		assertThat(findUpdateMember.getName()).isNotEqualTo(member1.getName());
	}
	
//	회원삭제
//	@Test
	public void 성공_회원삭제() throws Exception {
		// given
		Member member1 = Member.builder()
				.username("username")
				.password("1234567890")
				.name("Member1")
				.role(Role.USER)
				.nickName("NickName1")
				.age(20)
				.build();
		
		memberRepository.save(member1);
		clear();
		
		// when
		memberRepository.delete(member1);
		clear();

		// then
		assertThrows(Exception.class, () -> memberRepository.findById(member1.getId()).orElseThrow(() -> new Exception()));
	}
	
//	existByUsername 정상작동 테스트
//	@Test
	public void existByUsername_정상작동() throws Exception {
		// given
		String username = "username";
		Member member1 = Member.builder()
				.username(username)
				.password("1234567890")
				.name("Member1")
				.role(Role.USER)
				.nickName("NickName1")
				.age(20)
				.build();
		memberRepository.save(member1);
		clear();
		
		// when, then
		assertThat(memberRepository.existsByUsername(username)).isTrue();
		assertThat(memberRepository.existsByUsername(username + "123")).isFalse();
	}
	
//	findByUsername 정상작동 테스트
//	@Test
	public void findByUsername_정상작동() throws Exception {
		// given
		String username = "username";
		Member member1 = Member.builder()
				.username(username)
				.password("1234567890")
				.name("Member1")
				.role(Role.USER)
				.nickName("NickName1")
				.age(20)
				.build();
		memberRepository.save(member1);
		clear();
		assertThat(memberRepository.findByUsername(username).get().getUsername()).isEqualTo(member1.getUsername());
		assertThat(memberRepository.findByUsername(username).get().getName()).isEqualTo(member1.getName());
		assertThat(memberRepository.findByUsername(username).get().getId()).isEqualTo(member1.getId());
		assertThrows(Exception.class, () -> memberRepository.findByUsername(username + "123").orElseThrow(() -> new Exception()));
	}
	
//	회원가입 시 생성시간 등록
	@Test
	public void 회원가입시_생성시간_등록() throws Exception {
		// given
		Member member1 = Member.builder()
				.username("username")
				.password("1234567890")
				.name("Member1")
				.nickName("NickName1")
				.role(Role.USER)
				.age(20)
				.build();
		memberRepository.save(member1);
		clear();
		
		// when
		Member findMember = memberRepository.findById(member1.getId()).orElseThrow(() -> new Exception());
		
		// then
		assertThat(findMember.getCreatedDate()).isNotNull();
		assertThat(findMember.getLastModifiedDate()).isNotNull();
	}
}