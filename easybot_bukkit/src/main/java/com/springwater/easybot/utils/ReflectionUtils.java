package com.springwater.easybot.utils;

import com.springwater.easybot.Easybot;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public class ReflectionUtils {

    /**
     * 在指定对象中查找类型为指定类的字段，并返回该字段的值。
     *
     * @param instance 要查找的对象实例
     * @param type     要查找的字段的类型
     * @return 匹配的字段值，如果未找到则返回 null
     * @throws IllegalAccessException 如果字段不可访问
     */
    public static Object findFieldByType(Object instance, String type) throws IllegalAccessException {
        if (instance == null || type == null) {
            throw new IllegalArgumentException("Instance and type must not be null");
        }
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().getName().endsWith(type)) {
                field.setAccessible(true);
                return field.get(instance);
            }
        }
        return null;
    }

    public static void dumpAllFieldToConsole(Object instance) {
        if(instance == null) throw new IllegalArgumentException("Instance and type must not be null");
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            Easybot.instance.getLogger().info(instance.getClass().getName() + " {Field} -> " + field.getType().getName() + " " + field.getName());
        }
    }

    public static String getNMSClassPath(){
        return Bukkit.getServer().getName().replace(".CraftServer", "");
    }
}
