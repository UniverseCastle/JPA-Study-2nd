package com.jpa2.global.log;

public class TraceStatus { // TraceId와 메서드가 처음으로 호출된 시간, 그리고 메세지를 가지고 있음

	private TraceId traceId;
	private Long startTimeMs;
	private String message;
	
	public TraceStatus(TraceId traceId, Long startTimeMs, String message) {
		this.traceId = traceId;
		this.startTimeMs = startTimeMs;
		this.message = message;
	}
	
	public TraceId getTraceId() {
		return traceId;
	}
	
	public Long getStartTimeMs() {
		return startTimeMs;
	}
	
	public String getMessage() {
		return message;
	}
}