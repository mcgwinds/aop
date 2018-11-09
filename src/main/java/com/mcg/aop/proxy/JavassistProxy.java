package com.mcg.aop.proxy;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;


/**
 * @author mcg
 */
public class JavassistProxy implements AopProxy {

    AdvisedSupport config;

    public JavassistProxy(AdvisedSupport config){
        this.config=config;
    }


    public Object createProxy(ClassLoader classLoader) {

        AopProxyUtils.validateClassIfNecessary(config.getTargetClass(), classLoader);
        ProxyFactory proxyFactory=new ProxyFactory();
        proxyFactory.setInterfaces(config.getProxiedInterfaces());
        proxyFactory.setSuperclass(config.getTargetClass());
        proxyFactory.setHandler(new MethodHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Method method1, Object[] args) throws Throwable {
                Object oldProxy = null;
                boolean setProxyContext = false;
                TargetSource targetSource = config.getTargetSource();
                Object target = null;
                try {
                    Object retVal;
                    target = targetSource.getTarget();
                    Class<?> targetClass = (target != null ? target.getClass() : null);
                    List<Object> chain = config.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
                    if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
                        Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
                        retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
                    }
                    else {
                        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
                        retVal = invocation.proceed();

                    }
                    // Massage return value if necessary
                    if (retVal != null && retVal == target &&
                            !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
                        // Special case: it returned "this". Note that we can't help
                        // if the target sets a reference to itself in another returned object.
                        retVal = proxy;
                    }
                    Class<?> returnType = method.getReturnType();
                    if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
                        throw new AopInvocationException(
                                "Null return value from advice does not match primitive return type for: " + method);
                    }
                    return retVal;
                }
                finally {
                    if (target != null && !targetSource.isStatic()) {
                        // Must have come from TargetSource.
                        targetSource.releaseTarget(target);
                    }
                    if (setProxyContext) {
                        // Restore old proxy.
                        AopContext.setCurrentProxy(oldProxy);
                    }
                }
            }
        });

        Object proxy=null;
        try {
            proxy= proxyFactory.createClass().newInstance();
        } catch (InstantiationException e) {
            throw new ProxyException("InstantiationException",e);
        } catch (IllegalAccessException e) {
            throw new ProxyException("IllegalAccessException",e);
        }
        return proxy;

    }


    @Override
    public Object getProxy() {

        return getProxy(null);
    }

    @Override
    public Object getProxy(@Nullable ClassLoader classLoader) {

        return createProxy(classLoader);
    }
}
