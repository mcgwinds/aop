package com.mcg.aop.proxy;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.AopProxyFactory;

/**
 * @author mcg
 */
public class ByteBuddyProxyFactory implements AopProxyFactory {

    @Override
    public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {

        return new ByteBuddyProxy(config);

    }
}
