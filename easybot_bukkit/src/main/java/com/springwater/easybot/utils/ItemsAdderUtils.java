package com.springwater.easybot.utils;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.ItemsAdder;

public class ItemsAdderUtils {
    public static boolean isItemsAdderInstalled() {
        try {
            Class.forName("dev.lone.itemsadder.api.ItemsAdder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isQFacesInstalled() {
        try{
            if(!isItemsAdderInstalled()){
                return false;
            }
            return new FontImageWrapper("qqnt_sysface_res:qface_0").exists();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getFace(int faceId) {
        if(!isQFacesInstalled()){
            return "";
        }
        FontImageWrapper warper = new FontImageWrapper("qqnt_sysface_res:qface_" + faceId);
        if(!warper.exists()){
            return null;
        }
        return warper.getString();
    }
}
