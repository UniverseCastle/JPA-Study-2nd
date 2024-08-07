package com.jpa2.global.exception;

import java.net.BindException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
	public ResponseEntity<ExceptionDto> handlerBaseEx(BaseException exception) {
		log.error("BaseException errorMessgae(): {}", exception.getExceptionType().getErrorMessage());
		log.error("BaseException errorCode(): {}", exception.getExceptionType().getErrorCode());
		
		return new ResponseEntity<ExceptionDto>(new ExceptionDto(exception.getExceptionType().getErrorCode()),
												   exception.getExceptionType().getHttpStatus());
	}
	
	// @Valid 에서 예외 발생
	@ExceptionHandler(BindException.class)
	public ResponseEntity<ExceptionDto> handleValidEx(BindException exception) {
		log.error("@ValidException 발생! {}", exception.getMessage());
		
		return new ResponseEntity<ExceptionDto>(new ExceptionDto(2000), HttpStatus.BAD_REQUEST);
	}
	
	// HttpMessageNotReadableException => json 파싱 오류
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ExceptionDto> httpMessageNotReadableExceptionEx(HttpMessageNotReadableException exception) {
		log.error("Json을 파싱하는 과정에서 예외 발생! {}", exception.getMessage());
		
		return new ResponseEntity<ExceptionDto>(new ExceptionDto(3000), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Void> handleMemberEx(Exception exception) {
		exception.printStackTrace();
		
		return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
	}
	
	@Data
	@AllArgsConstructor
	static class ExceptionDto {
		private Integer errorCode;
	}
}