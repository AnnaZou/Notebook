package com.annazou.notebook;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingUtils {
    private static final String SP_NAME = "notebook_sp";
    private static final String SETTING_COLOR_MODE = "color_mode";
    private static final String SETTING_FONT_SIZE = "font_size";
    private static final String SETTING_BOOK_SORT = "book_sort";
    public static final String SORT_AUTO = "auto_sort";
    public static final String SORT_MODIFY = "sort_modify";

    // 0:daymode,1:darkmode
    public static boolean isDarkMode(Context context){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        int mode = sp.getInt(SETTING_COLOR_MODE, 0);
        return mode == 1 ? true : false;
    }

    public static void setColorMode(Context context, boolean isDark){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SETTING_COLOR_MODE, isDark ? 1 : 0);
        editor.commit();
    }

    // 0: normal, 1: small, 3: large
    public static int getFontSize(Context context){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt(SETTING_FONT_SIZE, 0);
    }

    public static void setFontSize(Context context, int size){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SETTING_FONT_SIZE, size);
        editor.commit();
    }

    public static String getBookSortMethod(Context context){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(SETTING_BOOK_SORT, SORT_MODIFY);
    }

    public static void setBookSortMethod(Context context, String method){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SETTING_BOOK_SORT, method);
        editor.commit();
    }
}
