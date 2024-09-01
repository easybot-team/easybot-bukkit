package com.springwater.easybot.hook;

import com.springwater.easybot.Easybot;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.util.proxy.DefineClassHelper;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;

public abstract class Injector {

    protected static final ClassPool classPool = ClassPool.getDefault();

    static {
        classPool.appendClassPath(new LoaderClassPath(Easybot.class.getClassLoader()));
    }

    protected final String targetClassName;
    protected final String classNameWithoutPackage;
    protected Class<?> neighbor;
    protected CtClass targetClass;


    public Injector(String targetClassName, String neighborName) {
        this.targetClassName = targetClassName;
        // split the class name
        String[] className = this.targetClassName.split("\\.");
        // get the class name without the package
        classNameWithoutPackage = className[className.length - 1];

        if (!this.canHook()) return;

        try {
            this.initClassPath();
            this.neighbor = Class.forName(neighborName);
            this.targetClass = classPool.get(targetClassName);
            this.hookClass();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void predefineClass() throws Exception {
        DefineClassHelper.toClass(
                targetClassName,
                neighbor,
                neighbor.getClassLoader(),
                null,
                targetClass.toBytecode()
        );
    }

    public void redefineClass(Instrumentation instrumentation) throws Exception {
        instrumentation.redefineClasses(
                new ClassDefinition(Class.forName(targetClassName), targetClass.toBytecode())
        );
    }

    public abstract void hookClass() throws Exception;

    public abstract boolean canHook();

    protected abstract void initClassPath();

}