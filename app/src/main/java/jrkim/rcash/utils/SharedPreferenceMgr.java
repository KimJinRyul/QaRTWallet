package jrkim.rcash.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import jrkim.rcash.keystore.KeyStoreMgr;

/**
 * KeyStoreMgr 를 이용해서 값을 저장할때 자동으로 암호화
 * 값을 꺼낼때 자동으로 복호화 해준다.
 */
public class SharedPreferenceMgr {
    private final String PREF_NAME = "RCash_pref";
    private static Context context;

    public SharedPreferenceMgr(Context context) {
        this.context = context.getApplicationContext();
    }

    private SharedPreferences.Editor getEditor() {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        return pref.edit();
    }

    public void put(String key, String value) {
        SharedPreferences.Editor editor = getEditor();
        if(value != null) {
            editor.putString(key, KeyStoreMgr.getInstance().encryptText(context, value));
        } else {
            editor.putString(key, null);
        }
        editor.apply();
    }

    public void put(String key, boolean value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void put(String key, int value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(key, value);
        editor.apply();
    }

    public void put(String key, long value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putLong(key, value);
        editor.apply();
    }

    public String get(String key, String defaultValue) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            String str = pref.getString(key, defaultValue);
            if(str == null || str.equals(defaultValue)) {
                return str;
            } else {
                return KeyStoreMgr.getInstance().decryptText(context, str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public boolean get(String key, boolean defaultValue) {
        return context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE).getBoolean(key, defaultValue);
    }

    public int get(String key, int defaultValue) {
        return context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE).getInt(key, defaultValue);
    }

    public long get(String key, long defaultValue) {
        return context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE).getLong(key, defaultValue);
    }
}
