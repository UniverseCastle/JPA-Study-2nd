package com.jpa2.domain.post.exception;

import com.jpa2.global.exception.BaseException;
import com.jpa2.global.exception.BaseExceptionType;

public class PostException extends BaseException {

	private BaseExceptionType baseExceptionType;
	
	public PostException(BaseExceptionType baseExceptionType) {
		this.baseExceptionType = baseExceptionType;
	}

	@Override
	public BaseExceptionType getExceptionType() {
		return this.baseExceptionType;
	}
}