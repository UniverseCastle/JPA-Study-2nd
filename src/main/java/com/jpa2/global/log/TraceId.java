package com.jpa2.global.log;

import java.util.UUID;

import com.jpa2.global.util.security.SecurityUtil;

public class TraceId {

	private String id;
	private int level; // 깊이, 메서드가 몇 번째로 호출되었는지에 대한 정보를 가지고 있음
	
	public TraceId() {
		this.id = createId();
		this.level = 0;
	}
	
	public TraceId(String id, int level) {
		this.id = id;
		this.level = level;
	}
	
	private String createId() {
		try {
			SecurityUtil.getLoginUsername();
		} catch (NullPointerException | ClassCastException e) { // 로그인 안하고 접근 & signUp등일 경우 anonymousUser가 반환되므로 캐스팅이 불가능
			return String.format("[Anonymous: %S]", UUID.randomUUID().toString().substring(0,8)); // UUID 익명 사용자 ID 생성
		}
		return SecurityUtil.getLoginUsername(); // 로그인 한 경우 사용자 이름 반환
	}
	
	public TraceId createNextid() {
		return new TraceId(id, level + 1);
	}
	
	public TraceId createPreviousId() {
		return new TraceId(id, level - 1);
	}
	
	public boolean isFirstLevel() {
		return level == 0;
	}
	
	public String getId() {
		return id;
	}
	
	public int getLevel() {
		return level;
	}
}