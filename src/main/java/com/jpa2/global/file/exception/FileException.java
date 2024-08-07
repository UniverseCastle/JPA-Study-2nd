package com.jpa2.global.file.exception;

import com.jpa2.global.exception.BaseException;
import com.jpa2.global.exception.BaseExceptionType;

public class FileException extends BaseException {

	private BaseExceptionType exceptionType;
	
	public FileException(BaseExceptionType exceptionType) {
		this.exceptionType = exceptionType;
	}

	@Override
	public BaseExceptionType getExceptionType() {
		return exceptionType;
	}
}