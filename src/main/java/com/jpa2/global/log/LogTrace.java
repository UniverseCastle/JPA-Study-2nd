package com.jpa2.global.log;

/**
 * 메서드가 실행되기 바로 직전에 호출되어 시작 시간과 메세지를 가진 TraceStatus를 반환
 * 이후 예외나 메서드가 정상 종료되었을 때 이를 가지고 걸린 시간을 측정
 */
public interface LogTrace {

	TraceStatus begin(String message);
	
	void end(TraceStatus status);
	
	void exception(TraceStatus status, Throwable e);
}