package com.maxsid.gen.multicounter;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.view.Display;

import java.math.BigDecimal;

/**
 * Created by maxim on 25.01.15.
 */
public class Globals {
    public static Counter counter;
    public static DB db;
    public static SharedPreferences pref;
    public static BigDecimal sumCounters;
    public static Display display;

    public static final int DIALOG_ITEM = 1;
    public static final int DIALOG_DELETE_ITEM = 2;
    public static final int DIALOG_CLEAR_DATABASE = 4;
    public static final int DIALOG_RESET_ALL_COUNTERS = 5;
    public static final int DIALOG_MERGE_WARNING = 6;

    public static final int ADD_COUNTER_ACTIVITY = 1;
    public static final int EDIT_COUNTER_ACTIVITY = 2;
    public static final int SETTINGS_ACTIVITY = 3;

    public static final String SETTING_KEY_CLEAR_DATABASE = "clear_database";
    public static final String SETTING_KEY_RESET_COUNTERS = "reset_counters";

    public static final String SETTING_KEY_CB_DEFAULT_BG_COLOR = "checkbox_default_bg_color";
    public static final String SETTING_KEY_CB_DEFAULT_BUT_COLOR = "checkbox_default_but_color";
    public static final String SETTING_KEY_CB_DBC_FOR_ALL_BUT = "checkbox_but_color_for_all_buttons";
    public static final String SETTING_KEY_CB_VIBRATION = "checkbox_vibration";

    public static final String SETTING_KEY_CHANGE_DEFAULT_COLOR = "change_default_background_color";
    public static final String SETTING_KEY_CHANGE_DEFAULT_T_COLOR = "change_default_text_color";
    public static final String SETTING_KEY_CHANGE_BUTTON_COLOR = "change_default_button_color";
    public static final String SETTING_KEY_CHANGE_BUTTON_T_COLOR = "change_default_button_text_color";


    public static final String SETTING_CATEGORY_BG_COLORS = "category_bg_colors";
    public static final String SETTING_CATEGORY_BUT_COLORS = "category_button_colors";
    public static final String SETTING_CATEGORY_SOUNDS_AND_VIBRATION = "category_sounds_and_vibration";


    public static final String LOG_TAG = "com.maxsid.gen.multicounter";

    public static Resources res;

    public static final String startAppAppID = "203112180";
    public static final String startAppdevID = "103184881";

    static void logInfo(String message) {
        Log.i(LOG_TAG, message);
    }
}
