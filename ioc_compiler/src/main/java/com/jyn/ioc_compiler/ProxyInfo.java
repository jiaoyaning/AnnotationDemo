package com.jyn.ioc_compiler;

import com.jyn.ioc_annotation.BindView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by jiao on 2019/10/29.
 */
public class ProxyInfo {
    private String packageName;

    private String proxyClassName;
    private TypeElement typeElement;

    public List<Element> mElementList = new ArrayList<>();

    public static final String PROXY = "ViewInject";

    public ProxyInfo(Elements elementUtils, TypeElement classElement) {
        this.typeElement = classElement;
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();
        String className = getClassName(classElement, packageName);
        this.packageName = packageName;
        this.proxyClassName = className + "$$" + PROXY;
    }


    public String getProxyClassFullName() {
        return packageName + "." + proxyClassName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code. Do not modify!\n");
        builder.append("package ").append(packageName).append(";\n\n");
        builder.append("import android.view.View;\n");
        builder.append("import com.jyn.ioc_annotation.*;\n");
        
        builder.append("import ").append(getLibrayPath(packageName)).append(".R;\n");
        builder.append("import com.jyn.annotationdemo.ioc.*;\n");
        builder.append('\n');
        builder.append("public class ").append(proxyClassName).append(" implements " + ProxyInfo.PROXY + "<" + typeElement.getQualifiedName() + ">");
        builder.append(" {\n");

        //生成方法
        generateMethods(builder);

        builder.append('\n');
        builder.append("}\n");
        return builder.toString();
    }

    private void generateMethods(StringBuilder builder) {
        builder.append("@Override\n ");
        builder.append("public void inject(").append(typeElement.getQualifiedName()).append(" host, Object source ) {\n");
        builder.append("initViewById(host,source);\n ");
        builder.append("  }\n");

        //生成 initViewById 方法
        builder.append("public void initViewById(").append(typeElement.getQualifiedName()).append(" host, Object source ) {\n");
        Iterator<Element> iterator = mElementList.iterator();
        while (iterator.hasNext()) {
            Element element = iterator.next();
            BindView annotation = element.getAnnotation(BindView.class);
            if (annotation != null) {
                VariableElement variableElement = (VariableElement) element;
                generateViewById(variableElement, builder);
                iterator.remove();
            }
        }
        builder.append("}");
    }

    private void generateViewById(VariableElement variableElement, StringBuilder builder) {
        int id = variableElement.getAnnotation(BindView.class).value();
        String type = variableElement.asType().toString();
        String name = variableElement.getSimpleName().toString();

        builder.append(" if(source instanceof android.app.Activity){\n");
        builder.append("host.").append(name).append(" = ");
        if (id == -1) {
            builder.append("(").append(type).append(")(((android.app.Activity)source).findViewById( ").append("R.id.").append(name).append("));\n");
        } else {
            builder.append("(").append(type).append(")(((android.app.Activity)source).findViewById( ").append(id).append("));\n");
        }
        builder.append("\n}else{\n");

        builder.append("host.").append(name).append(" = ");
        if (id == -1) {
            builder.append("(").append(type).append(")(((android.view.View)source).findViewById( ").append("R.id.").append(name).append("));\n");
        } else {
            builder.append("(").append(type).append(")(((android.view.View)source).findViewById( ").append(id).append("));\n");
        }
        builder.append("}\n");
    }

    /**
     * 获取包名
     *
     * @param packageName
     * @return
     */
    private String getLibrayPath(String packageName) {
        try {
            return packageName.substring(0, ordinalIndexOf(packageName, ".", 3));
        } catch (Exception e) {
            return packageName;
        }
    }

    private int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1) {
            pos = str.indexOf(substr, pos + 1);
        }
        return pos;
    }

    String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen)
                .replace('.', '$');
    }
}
