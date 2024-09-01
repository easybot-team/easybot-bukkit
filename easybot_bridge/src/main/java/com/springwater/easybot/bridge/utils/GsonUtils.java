package com.springwater.easybot.bridge.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public class GsonUtils {
    /**
     * 合并一个 JsonObject 和一个 Java 对象
     *
     * @param gson       Gson 对象
     * @param jsonObject 要合并的 JsonObject
     * @param object     要合并的 Java 对象
     * @return 合并后的 JsonObject
     */
    public static JsonObject merge(Gson gson, JsonObject jsonObject, Object object) {
        // 将对象转换为 JsonElement
        JsonElement element = gson.toJsonTree(object);

        // 确保元素是一个 JsonObject
        if (element.isJsonObject()) {
            JsonObject objectJson = element.getAsJsonObject();

            // 将所有键值对合并到原有的 JsonObject 中
            for (Map.Entry<String, JsonElement> kv : objectJson.entrySet()) {
                jsonObject.add(kv.getKey(), objectJson.get(kv.getKey()));
            }
        }

        return jsonObject;
    }
}
