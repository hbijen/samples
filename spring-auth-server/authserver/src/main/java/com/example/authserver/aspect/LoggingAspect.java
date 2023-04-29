package com.example.authserver.aspect;

import org.aspectj.lang.JoinPoint;

import lombok.extern.slf4j.Slf4j;

// @Component
// @Aspect
@Slf4j
public class LoggingAspect {
    
    // @Pointcut( "execution(public com.example.authserver.*.*(..))")
    // public void pointcut() { }
    
    //@Before("execution(* org.springframework.security*.*(..))")
    public void beforeEntering(JoinPoint jp) {
        String className = jp.getTarget().getClass().getSimpleName();
        String methodName = jp.getSignature().getName();        
        log.warn(String.format("invoking %s %s", className, methodName));
    }
}
