package com.jyn.annotationdemo.ioc;

import android.app.Activity;

/**
 * Created by jiao on 2019/10/29.
 */
public class ViewInjector {
    private static final String SUFFIX = "$$ViewInject";

    public static void inject(Activity activity) {
        ViewInject proxyActivity = findProxyActivity(activity);
        if (proxyActivity == null) {
            return;
        }
        proxyActivity.inject(activity, activity);
    }

    public static void inject(Object host, Object root) {
        Class<?> clazz = host.getClass();
        String proxyClassFullName = clazz.getName() + "$$ViewInject";
        Class<?> proxyClazz = null;
        try {
            proxyClazz = Class.forName(proxyClassFullName);
            ViewInject viewInject = (ViewInject) proxyClazz.newInstance();
            viewInject.inject(host, root);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据使用注解的类和约定的命名规则，反射获取注解生成的类
     *
     * @param object
     * @return
     */
    private static ViewInject findProxyActivity(Object object) {
        try {
            Class clazz = object.getClass();
            Class injectorClazz = Class.forName(clazz.getName() + SUFFIX);
            return (ViewInject) injectorClazz.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
