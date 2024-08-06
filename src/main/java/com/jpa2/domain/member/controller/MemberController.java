package com.jpa2.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jpa2.domain.member.dto.MemberInfoDto;
import com.jpa2.domain.member.dto.MemberSignUpDto;
import com.jpa2.domain.member.dto.MemberUpdateDto;
import com.jpa2.domain.member.dto.MemberWithdrawDto;
import com.jpa2.domain.member.dto.UpdatePasswordDto;
import com.jpa2.domain.member.serivce.MemberService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController // 문자열을 response의 Body에 작성하여 전송
@RequiredArgsConstructor // 항상 상태코드를 200으로 반환
public class MemberController {

	private final MemberService memberService;
	
	/**
	 * 회원가입
	 */
	@PostMapping("/signUp")
	@ResponseStatus(HttpStatus.OK)
	public void signUp(@Valid @RequestBody MemberSignUpDto memberSignUpDto) throws Exception {
		memberService.signUp(memberSignUpDto);
	}
	
	/**
	 * 회원정보 수정
	 */
	@PutMapping("/member")
	@ResponseStatus(HttpStatus.OK)
	public void updateBasicInfo(@Valid @RequestBody MemberUpdateDto memberUpdateDto) throws Exception {
		memberService.update(memberUpdateDto);
	}
	
	/**
	 * 비밀번호 수정
	 */
	@PutMapping("/member/password")
	@ResponseStatus(HttpStatus.OK)
	public void updatePassword(@Valid @RequestBody UpdatePasswordDto updatePasswordDto) throws Exception {
		memberService.updatePassword(updatePasswordDto.checkPassword(), updatePasswordDto.toBePassword());
	}
	
	/**
	 * 회원탈퇴
	 */
	@DeleteMapping("/member")
	@ResponseStatus(HttpStatus.OK)
	public void withdraw(@Valid @RequestBody MemberWithdrawDto memberWithdrawDto) throws Exception {
		memberService.withdraw(memberWithdrawDto.checkPassword());
	}
	
	/**
	 * 회원정보 조회
	 */
	@GetMapping("/member/{id}")
	public ResponseEntity getInfo(@Valid @PathVariable(name = "id") Long id) throws Exception {
		MemberInfoDto info = memberService.getinfo(id);
		
		return new ResponseEntity(info, HttpStatus.OK);
	}
	
	/**
	 * 내정보 조회
	 */
	@GetMapping("/member")
	public ResponseEntity getMyInfo(HttpServletResponse response) throws Exception {
		MemberInfoDto info = memberService.getMyInfo();
		
		return new ResponseEntity(info, HttpStatus.OK);
	}
}