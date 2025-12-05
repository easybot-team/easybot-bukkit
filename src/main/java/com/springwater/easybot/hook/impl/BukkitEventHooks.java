package com.springwater.easybot.hook.impl;

import com.springwater.easybot.hook.Injector;
import com.springwater.easybot.utils.ClassUtils;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.util.proxy.DefineClassHelper;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;

import java.util.function.BiPredicate;

public class BukkitEventHooks extends Injector {
    private static final CtClass CALLBACK_CLASS;
    static {
        try {
            CALLBACK_CLASS = classPool.get(BukkitEventCallback.class.getName());
            CALLBACK_CLASS.replaceClassName(
                    BukkitEventCallback.class.getName(),
                    Bukkit.class.getPackage().getName() + "." + BukkitEventCallback.class.getSimpleName()
            );
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void hookClass() throws Exception {
        CtMethod callEvent = ClassUtils.getMethodByName(targetClass.getMethods(), "callEvent");
        callEvent.insertBefore(
                "if(" + CALLBACK_CLASS.getName() + ".getInstance().onCallEvent(this.server,$1))return;"
        );
    }

    private final BukkitEventHandler bukkitEventHandler = new BukkitEventHandler();

    @Override
    public boolean canHook() {
        return true;
    }

    @Override
    protected void initClassPath() {

    }

    public BukkitEventHooks() {
        super("org.bukkit.plugin.SimplePluginManager", "org.bukkit.plugin.Plugin");

        try {
            Class<?> bukkitEventHooker =
                    DefineClassHelper.toClass(
                            CALLBACK_CLASS.getName(),
                            Bukkit.class,
                            Bukkit.class.getClassLoader(),
                            null,
                            CALLBACK_CLASS.toBytecode()
                    );

            BiPredicate<Server,Event> callback = this.bukkitEventHandler::onBukkitEvent;
            bukkitEventHooker.getConstructor(BiPredicate.class).newInstance(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
