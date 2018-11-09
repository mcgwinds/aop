package com.mcg.aop.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.any;

/**
 * @author mcg
 */
public class ByteBuddyProxy implements AopProxy {

    private AdvisedSupport config;

    public ByteBuddyProxy(AdvisedSupport config){
        this.config=config;
    }


    public Object createProxy(ClassLoader classLoader) {

        AopProxyUtils.validateClassIfNecessary(config.getTargetClass(), classLoader);
        Object proxy=null;

        try {
            proxy = new ByteBuddy()
                    .subclass(config.getTargetClass())
                    .method(any()).intercept(InvocationHandlerAdapter.of(new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
                    }))
                    .make()
                    .load(classLoader)
                    .getLoaded()
                    .newInstance();
        }
        catch (Exception e) {
                throw new ProxyException("create Proxy exception",e);
        }
        return proxy;

    }

    @Override
    public Object getProxy() {
        return getProxy(null);
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {

        return createProxy(classLoader==null?Thread.currentThread().getContextClassLoader():classLoader);
    }
}
