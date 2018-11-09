package com.mcg.aop.proxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author mcg
 */
public class ReflectiveMethodInvocation extends org.springframework.aop.framework.ReflectiveMethodInvocation {

    protected ReflectiveMethodInvocation(Object proxy, Object target, Method method, Object[] arguments, Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {
        super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
    }
}
