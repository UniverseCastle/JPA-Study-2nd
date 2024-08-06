package com.jpa2.global.exception;

public abstract class BaseException extends RuntimeException {
	public abstract BaseExceptionType getExceptionType();
}