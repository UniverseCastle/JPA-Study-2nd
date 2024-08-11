package com.jpa2.global.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.jpa2.global.log.LogTrace;
import com.jpa2.global.log.TraceStatus;

import lombok.RequiredArgsConstructor;

/**
 * @Pointcut
 * AOP에서 특정 Join Point를 정의하는 데 사용
 * 어떤 메서드에 Advice를 적용할지를 결정하는 표현식을 정의
 * 
 * execution(* com.jpa2.domain..*service*.*(..))
 * com.jpa2.domain 패키지와
 * 그 하위 모든 폴더들 중 클래스 이름의 중간에 Service가 들어간 모든 메서드에 적용되는 포인트컷
 * 
 * joinPoint.getSignature().toShortString()
 * 'MemberService.signUp(..)'과 같은 형식으로 반환되므로 이를 이용해 로그를 남김
 */
@Aspect
@Component
@RequiredArgsConstructor
public class LogAop {

	private final LogTrace logTrace;
	
	@Pointcut("execution(* com.jpa2.domain..*Service*.*(..))")
	public void allService() {};
	
	@Pointcut("execution(* com.jpa2.domain..*Repository*.*(..))")
	public void allRepository() {};
	
	@Pointcut("execution(* com.jpa2.domain..*Controller*.*(..))")
	public void allController() {};
	
	@Around("allService() || allController() || allRepository()")
	public Object logTrace(ProceedingJoinPoint joinPoint) throws Throwable {
		TraceStatus status = null;
		
		try {
			status = logTrace.begin(joinPoint.getSignature().toShortString());
			Object result = joinPoint.proceed();
			
			logTrace.end(status);
			
			return result;
		} catch (Throwable e) {
			e.printStackTrace();
			logTrace.exception(status, e);
			throw e;
		}
	}
}