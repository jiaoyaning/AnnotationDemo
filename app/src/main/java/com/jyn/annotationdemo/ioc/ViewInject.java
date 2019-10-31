package com.jyn.annotationdemo.ioc;

/**
 * Created by jiao on 2019/10/31.
 */
public interface ViewInject <T>{
    void inject(T t, Object source);
}
