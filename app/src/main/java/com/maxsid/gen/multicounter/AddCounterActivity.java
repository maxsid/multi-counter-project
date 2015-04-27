package com.maxsid.gen.multicounter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Random;

import yuku.ambilwarna.AmbilWarnaDialog;


public class AddCounterActivity extends ActionBarActivity
        implements View.OnClickListener {

    Button butBgColor, butTextColor, butApply;
    EditText etName, etValue, etStep, etLimit;
    Spinner spinnerOperation, spinnerDisplayValue;
    CheckBox cbLimitOn;
    int positionEdit;
    boolean edit;
    long owner;
    int amountCounters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_counter);
        //Реклама
        StartAppSDK.init(this, Globals.startAppdevID, Globals.startAppAppID, true);
        StartAppAd.init(this, Globals.startAppdevID, Globals.startAppAppID);
        StartAppAd.showSlider(this);
        //Конец рекламы
        butBgColor = ((Button) findViewById(R.id.bSelectColor));
        butBgColor.setOnClickListener(this);
        butBgColor.setOnCreateContextMenuListener(onCreateCtxMenuGenerateColors);
        butTextColor = ((Button) findViewById(R.id.bSelectTextColor));
        butTextColor.setOnClickListener(this);
        butTextColor.setOnCreateContextMenuListener(onCreateCtxMenuGenerateColors);
        butApply = ((Button) findViewById(R.id.bAddCounter));
        butApply.setOnClickListener(this);

        etName = (EditText) findViewById(R.id.etCounterName);
        etValue = (EditText) findViewById(R.id.etCounterCount);
        etStep = (EditText) findViewById(R.id.etCounterStep);
        etLimit = (EditText) findViewById(R.id.etMaxAndMinValue);

        cbLimitOn = (CheckBox) findViewById(R.id.cbMaxAndMinValue);
        cbLimitOn.setOnCheckedChangeListener(onCheckedMaxAndMinOnOff);

        spinnerOperation = (Spinner) findViewById(R.id.spinnerOperation);
        spinnerDisplayValue = (Spinner) findViewById(R.id.spinnerDisplayValue);

        edit = getIntent().getBooleanExtra("edit", false);
        owner = getIntent().getLongExtra("owner", 0);
        amountCounters = getIntent().getIntExtra("amount", 0);
        fillFields();
    }

    CompoundButton.OnCheckedChangeListener onCheckedMaxAndMinOnOff = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            etLimit.setEnabled(isChecked);
        }
    };

    View.OnCreateContextMenuListener onCreateCtxMenuGenerateColors = new View.OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int intColor = generateBGColor();
            setDefaultColors(intColor, setColorButTextColor(intColor));
        }
    };

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_counter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        adding();
        return super.onOptionsItemSelected(item);
    }

    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.bSelectTextColor:
            case R.id.bSelectColor:
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, getBackgroundColor(v), new AmbilWarnaDialog.OnAmbilWarnaListener() {

                    public void onCancel(AmbilWarnaDialog dialog) {
                    }

                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        Log.d(Globals.LOG_TAG, "Color selected: " + color);
                        if (v.getId() == R.id.bSelectColor)
                            setColorButTextColor(color);

                        v.setBackgroundColor(color);
                    }
                });

                dialog.show();
                break;
            case R.id.bAddCounter:
                adding();
                break;
        }
    }

    private void adding() {
        if (getStep().equals("0")) {
            showToast(getString(R.string.dont_zero));
            return;
        }
        try {
            if (cbLimitOn.isChecked()) {
                if (getOperation() == 0) {
                    if (Double.valueOf(getCounterCount()) > Double.valueOf(getLimit())) {
                        showToast(getString(R.string.value_is_big));
                        return;
                    }
                } else {
                    if (Double.valueOf(getCounterCount()) < Double.valueOf(getLimit())) {
                        showToast(getString(R.string.value_is_small));
                        return;
                    }
                }
            }

            if (!edit) {
                Globals.counter = new Counter(getCounterName(),
                        getBackgroundColor(butBgColor), getBackgroundColor(butTextColor),
                        getCounterCount(), owner, getOperation(), getStep(), amountCounters, getLimit());
                Globals.counter.setDisplay(getDisplayValue());
            } else {
                Globals.counter.setName(getCounterName());
                Globals.counter.setValue(getCounterCount());
                Globals.counter.setBgColor(getBackgroundColor(butBgColor));
                Globals.counter.setTextColor(getBackgroundColor(butTextColor));
                Globals.counter.setOperation(getOperation());
                Globals.counter.setStep(new BigDecimal(getStep()));
                Globals.counter.setLimit(getLimit());
                Globals.counter.setDisplay(getDisplayValue());
            }
        } catch (NumberFormatException ex){
            showToast(getString(R.string.incorrect_number));
            return;
        } catch (Exception ex){
            showToast(ex.getLocalizedMessage());
            return;
        }

        setResult(RESULT_OK, new Intent());
        finish();
    }

    private void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public static int getBackgroundColor(View v) {
        ColorDrawable drawable = (ColorDrawable) v.getBackground();
        if (Build.VERSION.SDK_INT >= 11) {
            return drawable.getColor();
        }
        try {
            Field field = drawable.getClass().getDeclaredField("mState");
            field.setAccessible(true);
            Object object = field.get(drawable);
            field = object.getClass().getDeclaredField("mUseColor");
            field.setAccessible(true);
            return field.getInt(object);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return 0;
    }

    private String getLimit() {
        if (!cbLimitOn.isChecked())
            return null;
        return etLimit.getText().toString().isEmpty() ? etLimit.getHint().toString() : etLimit.getText().toString();
    }

    public int setColorButTextColor(int color) {
        //int m = getResources().getInteger(R.integer.black) - 1;
        final byte H = 0, S = 1, V = 2;
        float[] hsv = {0, 0, 0};        //Основной цвет
        float[] hsvresult = {0, 0, 0}; //Дополняющий цвет
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
        //Высчитываем доплняющий цвет
        //H
        if (hsv[H] >= 180)
            hsvresult[H] = hsv[H] - 180;
        else
            hsvresult[H] = hsv[H] + 180;
        //S
        hsvresult[S] = (hsv[V] * hsv[S]) / (hsv[V] * (hsv[S] - 1) + 1);
        //V
        hsvresult[V] = hsv[V] * (hsv[S] - 1) + 1;

        color = Color.HSVToColor(hsvresult);
        Log.d(Globals.LOG_TAG, "Negative Color generated: " + color);
        //return m - color;
        return color;
    }

    private void fillFields() {
        Counter counter = Globals.counter;
        if (edit) {
            butApply.setText(getResources().getString(R.string.apply));
            positionEdit = counter.getPosition();
            etName.setText(counter.getName());
            etName.setHint(counter.getName());
            etValue.setText(String.valueOf(counter.getValue()));
            etValue.setEnabled(!counter.isOwner());
            butBgColor.setBackgroundColor(counter.getBgColor());
            butTextColor.setBackgroundColor(counter.getTextColor());
            etStep.setText(counter.getStep().toString());
            etStep.setEnabled(!counter.isOwner());
            spinnerOperation.setSelection(counter.getOperation());
            spinnerOperation.setEnabled(!counter.isOwner());
            spinnerDisplayValue.setEnabled(!counter.isOwner());
            spinnerDisplayValue.setSelection(counter.getDisplay());
            cbLimitOn.setChecked(counter.isLimited());
            cbLimitOn.setEnabled(!counter.isOwner());
            if (counter.isLimited())
                etLimit.setText(counter.getLimitToString());
            else
                etLimit.setEnabled(false);

            etLimit.setEnabled(!counter.isOwner());
        } else {
            etName.setHint(String.format(getResources().getString(R.string.counter_n), amountCounters + 1));
            int intColor = generateBGColor();
            setDefaultColors(intColor, setColorButTextColor(intColor));
            etLimit.setEnabled(false);
        }
    }

    private int generateBGColor() {
        Random random = new Random();
        return random.nextInt(16777216) - 16777217;
    }

    private void setDefaultColors(int bgColor, int textColor) {
        boolean colorFromPref = Globals.pref.getBoolean(Globals.SETTING_KEY_CB_DEFAULT_BUT_COLOR, false);
        if (colorFromPref || edit) {
            butBgColor.setBackgroundColor(Globals.pref.getInt(Globals.SETTING_KEY_CHANGE_BUTTON_COLOR, bgColor));
            findViewById(R.id.bSelectTextColor).setBackgroundColor(
                    Globals.pref.getInt(Globals.SETTING_KEY_CHANGE_BUTTON_T_COLOR, textColor));
        } else {
            butBgColor.setBackgroundColor(bgColor);
            findViewById(R.id.bSelectTextColor).setBackgroundColor(textColor);
        }
    }

    private String getCounterName() {
        if (!etName.getText().toString().isEmpty())
            return etName.getText().toString();
        return etName.getHint().toString();
    }

    private String getCounterCount() {
        if (!etValue.getText().toString().isEmpty())
            return etValue.getText().toString();
        return etValue.getHint().toString();
    }

    private int getOperation() {
        return spinnerOperation.getSelectedItemPosition();
    }

    private String getStep() {
        return etStep.getText().toString().equals("") ? etStep.getHint().toString() : etStep.getText().toString();
    }

    private int getDisplayValue() {
        return spinnerDisplayValue.getSelectedItemPosition();
    }

}
