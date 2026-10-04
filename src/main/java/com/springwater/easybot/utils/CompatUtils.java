package com.springwater.easybot.utils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.lang.reflect.Method;

/**
 * 跨服务端版本的兼容层。
 * <p>
 * Bukkit 在 1.13 ~ 26.x 之间陆续把"字符串消息"改成了 Adventure 的 {@code Component}，
 * 例如 {@code AsyncPlayerPreLoginEvent#setKickMessage(String)} 与 {@code Player#kickPlayer(String)}
 * 都属于"已弃用、后续会移除"的 API。
 * <p>
 * 这里优先使用新接口，新接口不存在时（旧服务端）回退到旧接口，
 * 这样同一个 jar 既能跑 1.13，也能跑 Paper 26.3。
 */
public final class CompatUtils {

    private static final String COMPONENT_CLASS = "net.kyori.adventure.text.Component";

    private static volatile boolean disallowResolved = false;
    private static volatile Method disallowMethod = null;

    private static volatile boolean kickResolved = false;
    private static volatile Method kickMethod = null;

    private CompatUtils() {
    }

    /**
     * 拒绝玩家登录并设置提示语。
     *
     * @return true 表示已通过新接口处理；false 表示调用方需要自行走旧接口
     */
    public static boolean disallowPreLogin(AsyncPlayerPreLoginEvent event, String message) {
        try {
            Method method = resolveDisallowMethod();
            if (method == null) return false;

            Class<?> resultType = method.getParameterTypes()[0];
            //noinspection unchecked,rawtypes
            Object result = Enum.valueOf((Class<? extends Enum>) resultType, "KICK_OTHER");
            method.invoke(event, result, legacyToComponent(message));
            return true;
        } catch (Throwable ignored) {
            // 反射失败时不影响调用方回退到旧接口
            return false;
        }
    }

    /**
     * 把玩家踢出服务器。
     *
     * @return true 表示已通过新接口处理；false 表示调用方需要自行走旧接口
     */
    public static boolean kickPlayer(Player player, String message) {
        try {
            Method method = resolveKickMethod();
            if (method == null) return false;

            method.invoke(player, legacyToComponent(message));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * 把旧版 {@code §} 颜色文本转成 Adventure {@code Component}。
     * <p>
     * 只在确认服务端存在 Adventure 新接口时才会被调用，因此低于 1.16 的服务端不会加载到这个类。
     */
    private static Object legacyToComponent(String message) throws ClassNotFoundException {
        Class.forName(COMPONENT_CLASS);
        return LegacyComponentSerializer.legacySection()
                .deserialize(message == null ? "" : message);
    }

    private static Method resolveDisallowMethod() {
        if (disallowResolved) return disallowMethod;
        synchronized (CompatUtils.class) {
            if (disallowResolved) return disallowMethod;
            Method found = null;
            try {
                Class<?> componentClass = Class.forName(COMPONENT_CLASS);
                for (Method method : AsyncPlayerPreLoginEvent.class.getMethods()) {
                    if (!"disallow".equals(method.getName())) continue;
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length != 2) continue;
                    if (!params[0].isEnum()) continue;
                    // 同时存在 PlayerPreLoginEvent.Result 的重载，这里只要 AsyncPlayerPreLoginEvent 自己的
                    if (!params[0].getName().startsWith(AsyncPlayerPreLoginEvent.class.getName())) continue;
                    if (!params[1].equals(componentClass)) continue;
                    found = method;
                    break;
                }
            } catch (Throwable ignored) {
            }
            disallowMethod = found;
            disallowResolved = true;
            return found;
        }
    }

    private static Method resolveKickMethod() {
        if (kickResolved) return kickMethod;
        synchronized (CompatUtils.class) {
            if (kickResolved) return kickMethod;
            Method found = null;
            try {
                Class<?> componentClass = Class.forName(COMPONENT_CLASS);
                found = Player.class.getMethod("kick", componentClass);
            } catch (Throwable ignored) {
            }
            kickMethod = found;
            kickResolved = true;
            return found;
        }
    }
}
