package com.maxsid.gen.multicounter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.widget.Toast;

import yuku.ambilwarna.widget.AmbilWarnaPreference;

/**
 * Created by maxim on 11.02.15.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    boolean dbCleared = false;
    boolean dbReseted = false;
    private CheckBoxPreference cbBGColor;
    private AmbilWarnaPreference bgColor;
    private AmbilWarnaPreference bgTextColor;
    private PreferenceCategory colorsBGCategory, soundsAndVibrationCategory;

    private CheckBoxPreference cbButtonColor;
    private CheckBoxPreference cbButtonColorForAll;
    private CheckBoxPreference cbVibration;
    private AmbilWarnaPreference buttonColor;
    private AmbilWarnaPreference buttonTextColor;
    private PreferenceCategory colorsButtonCategory;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        ((Preference) findPreference(Globals.SETTING_KEY_CLEAR_DATABASE)).setOnPreferenceClickListener(this);
        ((Preference) findPreference(Globals.SETTING_KEY_RESET_COUNTERS)).setOnPreferenceClickListener(this);

        colorsBGCategory = (PreferenceCategory) findPreference(Globals.SETTING_CATEGORY_BG_COLORS);
        cbBGColor = ((CheckBoxPreference) findPreference(Globals.SETTING_KEY_CB_DEFAULT_BG_COLOR));
        cbBGColor.setOnPreferenceClickListener(this);
        bgColor = ((AmbilWarnaPreference) findPreference(Globals.SETTING_KEY_CHANGE_DEFAULT_COLOR));
        bgTextColor = ((AmbilWarnaPreference) findPreference(Globals.SETTING_KEY_CHANGE_DEFAULT_T_COLOR));

        colorsButtonCategory = (PreferenceCategory) findPreference(Globals.SETTING_CATEGORY_BUT_COLORS);
        cbButtonColor = (CheckBoxPreference) findPreference(Globals.SETTING_KEY_CB_DEFAULT_BUT_COLOR);
        cbButtonColorForAll = (CheckBoxPreference) findPreference(Globals.SETTING_KEY_CB_DBC_FOR_ALL_BUT);
        cbButtonColor.setOnPreferenceClickListener(this);
        cbButtonColorForAll.setOnPreferenceClickListener(this);
        buttonColor = (AmbilWarnaPreference) findPreference(Globals.SETTING_KEY_CHANGE_BUTTON_COLOR);
        buttonTextColor = (AmbilWarnaPreference) findPreference(Globals.SETTING_KEY_CHANGE_BUTTON_T_COLOR);

        soundsAndVibrationCategory = (PreferenceCategory) findPreference(Globals.SETTING_CATEGORY_SOUNDS_AND_VIBRATION);
        cbVibration = ((CheckBoxPreference) findPreference(Globals.SETTING_KEY_CB_VIBRATION));
        cbVibration.setChecked(Globals.pref.getBoolean(Globals.SETTING_KEY_CB_VIBRATION, true));
        setVisibleColorsChanges();
    }

    private boolean hasVibration() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        return v.hasVibrator();
    }

    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals(Globals.SETTING_KEY_CLEAR_DATABASE)) {
            if (!dbCleared)
                showDialog(Globals.DIALOG_CLEAR_DATABASE);
            else
                Toast.makeText(this, getString(R.string.db_is_empty), Toast.LENGTH_LONG).show();
        } else if (key.equals(Globals.SETTING_KEY_RESET_COUNTERS)) {
            if (!dbCleared & !dbReseted)
                showDialog(Globals.DIALOG_RESET_ALL_COUNTERS);
            else if (dbCleared)
                Toast.makeText(this, getString(R.string.db_is_empty), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, getString(R.string.db_is_reseted), Toast.LENGTH_LONG).show();
        } else if (key.equals(Globals.SETTING_KEY_CB_DEFAULT_BG_COLOR) ||
                key.equals(Globals.SETTING_KEY_CB_DEFAULT_BUT_COLOR)) {
            setVisibleColorsChanges();
        }
        return false;
    }

    private void setVisibleColorsChanges() {
        if (cbBGColor.isChecked()) {
            colorsBGCategory.addPreference(bgColor);
            colorsBGCategory.addPreference(bgTextColor);
        } else {
            colorsBGCategory.removePreference(bgColor);
            colorsBGCategory.removePreference(bgTextColor);
        }

        if (!cbButtonColor.isChecked()) {
            colorsButtonCategory.removePreference(buttonColor);
            colorsButtonCategory.removePreference(buttonTextColor);
            colorsButtonCategory.removePreference(cbButtonColorForAll);
        } else {
            colorsButtonCategory.addPreference(buttonColor);
            colorsButtonCategory.addPreference(buttonTextColor);
            colorsButtonCategory.addPreference(cbButtonColorForAll);
        }

        if (!hasVibration()) {
            soundsAndVibrationCategory.removePreference(cbVibration);
        }
    }

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        switch (id) {
            case Globals.DIALOG_CLEAR_DATABASE:
                ad.setMessage(getString(R.string.dialog_delete_all_counters));
                ad.setPositiveButton(R.string.yes, clearDatabaseOnClickListener);
                ad.setNegativeButton(R.string.no, clearDatabaseOnClickListener);
                break;
            case Globals.DIALOG_RESET_ALL_COUNTERS:
                ad.setMessage(getString(R.string.dialog_reset_all_counters));
                ad.setPositiveButton(R.string.yes, resetAllCountersOnClickListener);
                ad.setNegativeButton(R.string.no, resetAllCountersOnClickListener);
                break;
        }

        return ad.create();
    }

    Dialog.OnClickListener clearDatabaseOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == Dialog.BUTTON_POSITIVE) {
                Globals.db.clearDatabase();
                dbCleared = true;

            }
        }
    };

    Dialog.OnClickListener resetAllCountersOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == Dialog.BUTTON_POSITIVE) {
                Globals.db.resetAllCounters();
                dbReseted = true;
            }
        }
    };

    public void onBackPressed() {
        setResult(RESULT_OK, new Intent());
        finish();
    }
}
