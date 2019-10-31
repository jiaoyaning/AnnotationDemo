package com.jyn.ioc_compiler;

import com.google.auto.service.AutoService;
import com.jyn.ioc_annotation.BindView;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import static javax.lang.model.element.Modifier.PRIVATE;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.jyn.ioc_annotation.BindView")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class IocProcessor extends AbstractProcessor {

    private Filer mFileUtils;

    private Elements mElementUtils;

    private Messager mMessager;

    private Map<String, ProxyInfo> mProxyMap = new HashMap<String, ProxyInfo>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFileUtils = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<String>();
        annotationTypes.add(BindView.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mProxyMap.clear();
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        for (Element element : elements) {
            if (!checkAnnotationValid(element)) {
                return false;
            }

            VariableElement variableElement = (VariableElement) element;
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            String qualifiedName = typeElement.getQualifiedName().toString();
            ProxyInfo proxyInfo = mProxyMap.get(qualifiedName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mElementUtils, typeElement);
                mProxyMap.put(qualifiedName, proxyInfo);
            }

            proxyInfo.mElementList.add(element);
        }

        for (String key : mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(key);
            try {
                JavaFileObject jfo = mFileUtils.createSourceFile(
                        proxyInfo.getProxyClassFullName(),
                        proxyInfo.getTypeElement());
                Writer writer = jfo.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(),
                        "Unable to write injector for type %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }
        }

        return true;
    }

    private boolean checkAnnotationValid(Element annotatedElement) {

        if (isPrivate(annotatedElement)) {
            error(annotatedElement, "%s() must can not be private.", annotatedElement.getSimpleName());
            return false;
        }
        return true;
    }


    boolean isPrivate(Element annotatedClass) {
        return annotatedClass.getModifiers().contains(PRIVATE);
    }
    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
