package com.jpa2.domain.member.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @RestControllerAdvice
 * @ControllerAdvice에 @ResponseBody를 합친 것
 * 모든 @Controller에서 발생하는 예외를 처리해줄 수 있도록 도와주는 역할
 * 
 * @ExceptionHandler
 * 어떤 예외를 잡아서 처리할 지 명시
 * 
 * Filter에서 발생하는 예외는 ControllerAdvice까지 넘어오지 않음
 * 스프링 시큐리티 필터에서 발생한 권한 없는 예외 등은
 * 따로 Filter에 handler를 설정하여 처리
 */
@RestControllerAdvice
public class ExceptionAdvice {

	@ExceptionHandler(Exception.class)
	public ResponseEntity handleMemberEx(Exception exception) {
		return new ResponseEntity(HttpStatus.OK);
	}
}