package com.springwater.easybot.hook;

import bot.inker.acj.JvmHacker;
import com.springwater.easybot.Easybot;
import org.reflections.Reflections;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HookerManager {
    private final Logger logger = Easybot.instance.getLogger();

    public HookerManager() {
        List<Injector> injectors = this.getInjectorList();

        List<Injector> definedClasses = injectors.stream()
                .filter(Injector::canHook)
                .filter(injector -> {
                    try {
                        injector.predefineClass();
                        return false;
                    } catch (Throwable e) {
                        return true;
                    }
                })
                .collect(Collectors.toList());

        if (definedClasses.size() == 0) return;


        try {
            Instrumentation instrumentation = JvmHacker.instrumentation();

            definedClasses.forEach(injector -> {
                try {
                    injector.redefineClass(instrumentation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            logger.severe("Error while attaching agent");
            e.printStackTrace();
        }
    }

    private List<Injector> getInjectorList() {
        Reflections reflections = new Reflections("com.springwater.easybot.hook.impl");
        return reflections.getSubTypesOf(Injector.class).stream().map(injectorClass -> {
            try {
                return injectorClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
}
