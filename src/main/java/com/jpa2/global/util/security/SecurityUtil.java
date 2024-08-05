package com.jpa2.global.util.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtil {
	public static String getLoginUsername() { // SecurityContextHolder에서 username을 꺼내오는 메서드
		UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		return user.getUsername();
	}
}