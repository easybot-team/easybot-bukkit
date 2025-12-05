package com.springwater.easybot.hook.impl;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.hook.Injector;
import com.springwater.easybot.utils.ClassUtils;
import com.springwater.easybot.utils.ReflectionUtils;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.util.proxy.DefineClassHelper;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;

import java.util.Arrays;
import java.util.function.BiPredicate;

public class SendMessageHooks extends Injector {
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
        Easybot.instance.getLogger().info("class: " + targetClass.getName());
        Easybot.instance.getLogger().info("method dump:");
        Arrays.stream(targetClass.getMethods()).forEach(x -> Easybot.instance.getLogger().info(x.toString()));

        CtMethod callEvent = ClassUtils.getMethodByName(targetClass.getMethods(), "sendMessage");
        callEvent.insertBefore(
                "if(" + CALLBACK_CLASS.getName() + ".getInstance().onSendMessage(this,$1))return;"
        );
    }

    @Override
    public boolean canHook() {
        return true;
    }

    @Override
    protected void initClassPath() {

    }

    private final SendMessageHandler sendMessageHandler = new SendMessageHandler();

    public static String getNativeServerClassPath(){
        Server server = Bukkit.getServer();
        try {
            Object dedicatedServer = ReflectionUtils.findFieldByType(server, "DedicatedServer");
            return dedicatedServer.getClass().getName();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public SendMessageHooks() {

        super(getNativeServerClassPath(), "org.bukkit.Server");
        try {
            Class<?> sendMessageHooker =
                    DefineClassHelper.toClass(
                            CALLBACK_CLASS.getName(),
                            Bukkit.class,
                            Bukkit.class.getClassLoader(),
                            null,
                            CALLBACK_CLASS.toBytecode()
                    );

            BiPredicate<Server, Event> callback = this.sendMessageHandler::onSendMessage;
            sendMessageHooker.getConstructor(BiPredicate.class).newInstance(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
