package com.jpa2.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class ExceptionAdvice {
	
	@ExceptionHandler(BaseException.class)
	public ResponseEntity handlerBaseEx(BaseException exception) {
		log.error("BaseException errorMessgae(): {}", exception.getExceptionType().getErrorMessage());
		log.error("BaseException errorCode(): {}", exception.getExceptionType().getErrorCode());
		
		return new ResponseEntity(new ExceptionDto(exception.getExceptionType().getErrorCode()),
												   exception.getExceptionType().getHttpStatus());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity handleMemberEx(Exception exception) {
		exception.printStackTrace();
		
		return new ResponseEntity(HttpStatus.OK); // 서버에서 예외가 발생하더라도 200 반환
	}
	
	@Data
	@AllArgsConstructor
	static class ExceptionDto {
		private Integer errorCode;
	}
}