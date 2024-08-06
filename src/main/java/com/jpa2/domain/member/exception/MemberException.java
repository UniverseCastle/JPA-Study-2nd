package com.jpa2.domain.member.exception;

import com.jpa2.global.exception.BaseException;
import com.jpa2.global.exception.BaseExceptionType;

public class MemberException extends BaseException {

	private BaseExceptionType exceptionType;
	
	public MemberException(BaseExceptionType exceptionType) {
		this.exceptionType = exceptionType;
	}

	@Override
	public BaseExceptionType getExceptionType() {
		return exceptionType;
	}
}