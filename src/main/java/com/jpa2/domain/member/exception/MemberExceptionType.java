package com.jpa2.domain.member.exception;

import org.springframework.http.HttpStatus;

import com.jpa2.global.exception.BaseExceptionType;

public enum MemberExceptionType implements BaseExceptionType {
	
	//== 회원가입, 로그인 시 ==//
	ALREADY_EXIST_USERNAME(600, HttpStatus.OK, "이미 존재하는 아이디 입니다."),
	WRONG_PASSWORD(601, HttpStatus.OK, "비밀번호가 잘못되었습니다."),
	NOT_FOUND_MEMBER(602, HttpStatus.OK, "회원 정보가 없습니다.");
	
	private int errorCode; // 에러코드
	private HttpStatus httpStatus; // Http 상태코드, 200 OK로 통일
	private String errormessage; // 에러 메세지
	
	private MemberExceptionType(int errorCode, HttpStatus httpStatus, String errorMessage) {
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
		this.errormessage = errorMessage;
	}

	@Override
	public int getErrorCode() {
		return this.errorCode;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return this.httpStatus;
	}

	@Override
	public String getErrorMessage() {
		return this.errormessage;
	}
	
}