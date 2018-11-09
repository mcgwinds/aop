package aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor;
import org.springframework.aop.target.SingletonTargetSource;

import java.lang.reflect.Method;

public class CglibProxyTest {

    public static void main(String[] args) {
        DefaultAopProxyFactory defaultAopProxyFactory=new DefaultAopProxyFactory();
        Clazz c=new Clazz();
        AdvisedSupport config=new AdvisedSupport();
        TargetSource targetSource=new SingletonTargetSource(c);
        config.setTargetSource(targetSource);
        config.setInterfaces(Interface.class);
        config.addAdvisor(new Advisor() {
                              @Override
                              public Advice getAdvice() {
                                  return new MethodBeforeAdviceInterceptor(new MethodBeforeAdvice() {
                                      @Override
                                      public void before(Method method, Object[] args, Object target) throws Throwable {
                                          System.out.println("-----before method1-----");
                                      }
                                  });
                              }

                              @Override
                              public boolean isPerInstance() {
                                  return false;
                              }

                              ;
                          }
        );

        config.addAdvice(new MethodBeforeAdvice() {

            @Override
            public void before(Method method, Object[] args, Object target) throws Throwable {
                System.out.println("-----before method2-----");
            }
        });
        config.addAdvice(new AfterReturningAdvice() {

            @Override
            public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
                System.out.println(returnValue);
                System.out.println("-----afterReturning-----");
            }
        });
        AopProxy aopProxy=defaultAopProxyFactory.createAopProxy(config);
        Interface i=(Interface) aopProxy.getProxy();
        i.hello("world");
    }
}
