package com.mcg.aop.proxy;

/**
 * @author mcg
 */
public class ProxyException extends RuntimeException {

    ProxyException(String message) {
        super(message);
    }

    ProxyException(String message,Throwable cause) {
        super(message,cause);
    }

}
