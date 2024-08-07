package com.jpa2.domain.comment.exception;

import com.jpa2.global.exception.BaseException;
import com.jpa2.global.exception.BaseExceptionType;

public class CommentException extends BaseException {
	private BaseExceptionType baseExceptionType;
	
	
	public CommentException(BaseExceptionType baseExceptionType) {
		this.baseExceptionType = baseExceptionType;
	}
	
	@Override
	public BaseExceptionType getExceptionType() {
		return this.baseExceptionType;
	}
}