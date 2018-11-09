package com.mcg.aop.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author mcg
 */
public class AopProxyUtils extends org.springframework.aop.framework.AopProxyUtils {

    private static final Log logger = LogFactory.getLog(AopProxyUtils.class);

    private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<>();

    public static Object[] adaptArgumentsIfNecessary(Method method, @Nullable Object[] arguments) {
        if (ObjectUtils.isEmpty(arguments)) {
            return new Object[0];
        }
        if (method.isVarArgs()) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == arguments.length) {
                int varargIndex = paramTypes.length - 1;
                Class<?> varargType = paramTypes[varargIndex];
                if (varargType.isArray()) {
                    Object varargArray = arguments[varargIndex];
                    if (varargArray instanceof Object[] && !varargType.isInstance(varargArray)) {
                        Object[] newArguments = new Object[arguments.length];
                        System.arraycopy(arguments, 0, newArguments, 0, varargIndex);
                        Class<?> targetElementType = varargType.getComponentType();
                        int varargLength = Array.getLength(varargArray);
                        Object newVarargArray = Array.newInstance(targetElementType, varargLength);
                        System.arraycopy(varargArray, 0, newVarargArray, 0, varargLength);
                        newArguments[varargIndex] = newVarargArray;
                        return newArguments;
                    }
                }
            }
        }
        return arguments;
    }

    public static void validateClassIfNecessary(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader) {
        if (logger.isWarnEnabled()) {
            synchronized (validatedClasses) {
                if (!validatedClasses.containsKey(proxySuperClass)) {
                    doValidateClass(proxySuperClass, proxyClassLoader,
                            ClassUtils.getAllInterfacesForClassAsSet(proxySuperClass));
                    validatedClasses.put(proxySuperClass, Boolean.TRUE);
                }
            }
        }
    }

    /**
     * Checks for final methods on the given {@code Class}, as well as package-visible
     * methods across ClassLoaders, and writes warnings to the log for each one found.
     */
    private static void doValidateClass(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader, Set<Class<?>> ifcs) {
        if (proxySuperClass != Object.class) {
            Method[] methods = proxySuperClass.getDeclaredMethods();
            for (Method method : methods) {
                int mod = method.getModifiers();
                if (!Modifier.isStatic(mod) && !Modifier.isPrivate(mod)) {
                    if (Modifier.isFinal(mod)) {
                        if (implementsInterface(method, ifcs)) {
                            logger.info("Unable to proxy interface-implementing method [" + method + "] because " +
                                    "it is marked as final: Consider using interface-based JDK proxies instead!");
                        }
                        logger.debug("Final method [" + method + "] cannot get proxied via CGLIB: " +
                                "Calls to this method will NOT be routed to the target instance and " +
                                "might lead to NPEs against uninitialized fields in the proxy instance.");
                    }
                    else if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod) &&
                            proxyClassLoader != null && proxySuperClass.getClassLoader() != proxyClassLoader) {
                        logger.debug("Method [" + method + "] is package-visible across different ClassLoaders " +
                                "and cannot get proxied via CGLIB: Declare this method as public or protected " +
                                "if you need to support invocations through the proxy.");
                    }
                }
            }
            doValidateClass(proxySuperClass.getSuperclass(), proxyClassLoader, ifcs);
        }
    }

    private static boolean implementsInterface(Method method, Set<Class<?>> ifcs) {
        for (Class<?> ifc : ifcs) {
            if (ClassUtils.hasMethod(ifc, method.getName(), method.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }

}
