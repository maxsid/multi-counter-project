package com.maxsid.gen.multicounter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;


public class MainActivity extends ActionBarActivity {

    CountersAdapter adapter;
    public GridView gridView;
    public RelativeLayout mainActivity;
    public TextView tvSumCounters;
    DB db;
    long owner;
    public Animation clickAnim;
    private long movingCounterId = -1;
    private boolean copyCounter = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Реклама
        StartAppSDK.init(this, Globals.startAppdevID, Globals.startAppAppID, true);
        StartAppAd.init(this, Globals.startAppdevID, Globals.startAppAppID);
        StartAppAd.showSlider(this);
        //Конец рекламы
        //Анимации
        clickAnim = new AnimationUtils().loadAnimation(this, R.anim.click_button);
        //Конец анимации
        Globals.pref = PreferenceManager.getDefaultSharedPreferences(this);
        Globals.display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        mainActivity = (RelativeLayout) findViewById(R.id.rlMain);
        tvSumCounters = (TextView) findViewById(R.id.tvSumCounters);
        tvSumCounters.getViewTreeObserver().addOnGlobalLayoutListener(tvSumCountersGlobalLayoutListener);
        //Log.d(Globals.LOG_TAG, "MH:" + gridView.getMeasuredHeight() + " MW:" + gridView.getMeasuredWidth());
        Globals.res = getResources();

        Globals.db = db = new DB(this);
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(gridViewGlobalLayoutListener);
        changeOwner(getIntent().getLongExtra("owner", 0));
        movingCounterId = getIntent().getLongExtra("movingCounterId", -1);
        copyCounter = getIntent().getBooleanExtra("copyCounter", false);
        gridView.setAdapter(adapter);
    }

    private ViewTreeObserver.OnGlobalLayoutListener tvSumCountersGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                tvSumCounters.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
                tvSumCounters.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
            setTextSumCounters(tvSumCounters.getText().toString());
        }
    };

    private ViewTreeObserver.OnGlobalLayoutListener gridViewGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
                gridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }

            adapter.resizingButtons();
            refreshGVData();
        }
    };


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_counter:
                showAddCounterActivity();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case Globals.ADD_COUNTER_ACTIVITY:
                adapter.addGlobalCounter();
                changeOwner(Globals.counter.getOwner());
                break;
            case Globals.EDIT_COUNTER_ACTIVITY:
                adapter.editGlobalCounter();
                changeOwner(Globals.counter.getOwner());
                break;
        }
        refreshGVData();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case Globals.DIALOG_ITEM:
                dialog.setTitle(Globals.counter.getName());
                break;
            case Globals.DIALOG_DELETE_ITEM:
                ((AlertDialog) dialog).setMessage(String.format(getResources().getString(R.string.delete_dialog_question),
                        Globals.counter.getName()));
                break;
        }
    }

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        switch (id) {
            case Globals.DIALOG_ITEM:
                String divOrMerg = Globals.counter.isOwner() ? getString(R.string.merge) : getString(R.string.divide);
                String blockOrUnblock = Globals.counter.isBlocked() ? getString(R.string.to_unblock) : getString(R.string.to_block);
                ad.setTitle(Globals.counter.getName());
                if (movingCounterId == -1)
                    if (!Globals.counter.isOwner() && Globals.counter.getClicks() > 0)
                        ad.setItems(new String[]{getString(R.string.cancel_last_click),getString(R.string.dialog_edit),
                            divOrMerg, getString(R.string.dialog_delete), getString(R.string.move_button), getString(R.string.copy_button), blockOrUnblock}
                            , contextItemDialogClickListener);
                    else
                        ad.setItems(new String[]{getString(R.string.dialog_edit),
                                divOrMerg, getString(R.string.dialog_delete), getString(R.string.move_button), getString(R.string.copy_button), blockOrUnblock}
                                , contextItemDialogClickListenerWithoutLastClick);
                else
                    ad.setItems(new String[]{getString(R.string.paste_button_before),
                            getString(R.string.paste_button_after), getString(R.string.cancel_paste)}
                            , contextItemDialogClickListenerPasteMode);

                break;
            case Globals.DIALOG_DELETE_ITEM:
                ad.setMessage(String.format(getString(R.string.delete_dialog_question),
                        Globals.counter.getName()));
                ad.setPositiveButton(R.string.yes, deleteItemDialogClickListener);
                ad.setNegativeButton(R.string.no, deleteItemDialogClickListener);
                break;
            case Globals.DIALOG_EXIT:
                ad.setMessage(getString(R.string.exit_question));
                ad.setPositiveButton(R.string.yes, exitProgrammDialogClickListener);
                ad.setNegativeButton(R.string.no, exitProgrammDialogClickListener);
                break;
            case Globals.DIALOG_MERGE_WARNING:
                ad.setMessage(getString(R.string.merge_warning));
                ad.setPositiveButton(R.string.yes, mergeWarningOnClickListener);
                ad.setNegativeButton(R.string.no, mergeWarningOnClickListener);
                break;
        }

        return ad.create();
    }

    Dialog.OnClickListener contextItemDialogClickListenerPasteMode = new DialogInterface.OnClickListener() {
        private final int DIALOG_WHICH_PASTE_BEFORE = 0;
        private final int DIALOG_WHICH_PASTE_AFTER = 1;
        private final int DIALOG_WHICH_CANCEL = 2;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DIALOG_WHICH_PASTE_BEFORE:
                    if (!copyCounter) {
                        adapter.moveCounterOnPosition(movingCounterId, Globals.counter.getPosition());
                    } else {
                        Counter c = db.copyCounter(owner, movingCounterId, Globals.counter.getPosition(), true);
                        if (c.isOwner())
                            db.copyAllCountersOnOwner(movingCounterId, c.getId());
                    }

                    movingCounterId = -1;
                    changeOwner(owner);
                    break;
                case DIALOG_WHICH_PASTE_AFTER:
                    if (!copyCounter) {
                        adapter.moveCounterOnPosition(movingCounterId, Globals.counter.getPosition() + 1);
                    } else {
                        Counter c = db.copyCounter(owner, movingCounterId, Globals.counter.getPosition() + 1, true);
                        if (c.isOwner())
                            db.copyAllCountersOnOwner(movingCounterId, c.getId());
                    }

                    movingCounterId = -1;
                    changeOwner(owner);
                    break;
                case DIALOG_WHICH_CANCEL:
                    movingCounterId = -1;
                    break;
            }
        }
    };

    Dialog.OnClickListener contextItemDialogClickListener = new DialogInterface.OnClickListener() {
        private final int DIALOG_WHICH_CANCEL_LAST_CLICK = 0;
        private final int DIALOG_WHICH_EDIT = 1;
        private final int DIALOG_WHICH_DIVIDE_OR_MERGE = 2;
        private final int DIALOG_WHICH_DELETE = 3;
        private final int DIALOG_WHICH_MOVE_BUTTON = 4;
        private final int DIALOG_WHICH_COPY_BUTTON = 5;
        private final int DIALOG_WHICH_BLOCK_OR_UNBLOCK_BUTTON = 6;

        public void onClick(DialogInterface dialog, int which) {
            Log.d(Globals.LOG_TAG, "contextItemDialog selected: " + which);
            switch (which) {
                case DIALOG_WHICH_CANCEL_LAST_CLICK:
                    Globals.counter.cancelLastClick();
                    refreshGVData();
                    break;
                case DIALOG_WHICH_EDIT:
                    showEditCounterActivity(Globals.counter);
                    break;
                case DIALOG_WHICH_DELETE:
                    showDialog(Globals.DIALOG_DELETE_ITEM);
                    break;
                case DIALOG_WHICH_DIVIDE_OR_MERGE:
                    if (!Globals.counter.isOwner()) {
                        adapter.divideGlobalCounter();
                        refreshGVData();
                    } else {
                        showDialog(Globals.DIALOG_MERGE_WARNING);
                    }
                    break;
                case DIALOG_WHICH_MOVE_BUTTON:
                    movingCounterId = Globals.counter.getId();
                    copyCounter = false;
                    break;
                case DIALOG_WHICH_COPY_BUTTON:
                    movingCounterId = Globals.counter.getId();
                    copyCounter = true;
                    break;
                case DIALOG_WHICH_BLOCK_OR_UNBLOCK_BUTTON:
                    adapter.blockOrUnblockCounter(Globals.counter.getId(), !Globals.counter.isBlocked());
                    refreshGVData();
                    break;
            }
        }
    };

    Dialog.OnClickListener contextItemDialogClickListenerWithoutLastClick = new DialogInterface.OnClickListener() {
        private final int DIALOG_WHICH_EDIT = 0;
        private final int DIALOG_WHICH_DIVIDE_OR_MERGE = 1;
        private final int DIALOG_WHICH_DELETE = 2;
        private final int DIALOG_WHICH_MOVE_BUTTON = 3;
        private final int DIALOG_WHICH_COPY_BUTTON = 4;
        private final int DIALOG_WHICH_BLOCK_OR_UNBLOCK_BUTTON = 5;

        public void onClick(DialogInterface dialog, int which) {
            Log.d(Globals.LOG_TAG, "contextItemDialog selected: " + which);
            switch (which) {
                case DIALOG_WHICH_EDIT:
                    showEditCounterActivity(Globals.counter);
                    break;
                case DIALOG_WHICH_DELETE:
                    showDialog(Globals.DIALOG_DELETE_ITEM);
                    break;
                case DIALOG_WHICH_DIVIDE_OR_MERGE:
                    if (!Globals.counter.isOwner()) {
                        adapter.divideGlobalCounter();
                        refreshGVData();
                    } else {
                        showDialog(Globals.DIALOG_MERGE_WARNING);
                    }
                    break;
                case DIALOG_WHICH_MOVE_BUTTON:
                    movingCounterId = Globals.counter.getId();
                    copyCounter = false;
                    break;
                case DIALOG_WHICH_COPY_BUTTON:
                    movingCounterId = Globals.counter.getId();
                    copyCounter = true;
                    break;
                case DIALOG_WHICH_BLOCK_OR_UNBLOCK_BUTTON:
                    adapter.blockOrUnblockCounter(Globals.counter.getId(), !Globals.counter.isBlocked());
                    refreshGVData();
                    break;
            }
        }
    };

    Dialog.OnClickListener mergeWarningOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == Dialog.BUTTON_POSITIVE) {
                adapter.mergeGlobalCounter();
                refreshGVData();
            }
        }
    };

    Dialog.OnClickListener deleteItemDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    adapter.deleteGlobalCounter();
                    changeOwner(owner);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (owner == 0) {
            showDialog(Globals.DIALOG_EXIT);
        } else {
            changeActivity(db.getCounterOnId(owner).getOwner());
        }
    }

    Dialog.OnClickListener exitProgrammDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    finish();
            }
        }
    };

    public void refreshGVData() {
        gridView.setAdapter(adapter);
        adapter.refreshSumCounters();

    }


    public void showAddCounterActivity() {
        Intent intent = new Intent(this, AddCounterActivity.class);
        intent.putExtra("amount", adapter.getCount());
        intent.putExtra("owner", owner);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
        startActivityForResult(intent, Globals.ADD_COUNTER_ACTIVITY);
    }

    public void showEditCounterActivity(Counter counter) {
        Intent intent = new Intent(this, AddCounterActivity.class);
        Globals.counter = counter;
        intent.putExtra("edit", true);
        intent.putExtra("amount", adapter.getCount());
        intent.putExtra("owner", owner);
        startActivityForResult(intent, Globals.EDIT_COUNTER_ACTIVITY);
    }

    public void newAdapter(long owner) {
        this.owner = owner;
        adapter = new CountersAdapter(this);
        adapter.refreshSumCounters();
        adapter.refreshCounters();
    }

    public void changeOwner(Counter newOwner) {
        newAdapter(newOwner.getId());
        changeColors(newOwner.getBgColor(), newOwner.getTextColor());
        refreshGVData();

        if (newOwner.getId() != 0)
            this.setTitle(newOwner.getName());
        else
            this.setTitle(R.string.main);

    }

    public void changeOwner(long newOwner) {
        newAdapter(newOwner);
        Counter counter = db.getCounterOnId(newOwner);
        changeColors(counter.getBgColor(), counter.getTextColor());
        refreshGVData();

        if (newOwner != 0)
            this.setTitle(db.getCounterOnId(newOwner).getName());
        else
            this.setTitle(R.string.main);

    }

    private void changeColors(int bgColor, int textColor) {
        boolean colorFromPref = Globals.pref.getBoolean(Globals.SETTING_KEY_CB_DEFAULT_BG_COLOR, false);
        if (colorFromPref) {
            mainActivity.setBackgroundColor(Globals.pref.getInt(Globals.SETTING_KEY_CHANGE_DEFAULT_COLOR, bgColor));
            tvSumCounters.setTextColor(Globals.pref.getInt(Globals.SETTING_KEY_CHANGE_DEFAULT_T_COLOR, textColor));
        } else {
            mainActivity.setBackgroundColor(bgColor);
            tvSumCounters.setTextColor(textColor);
        }
    }

    public void setTextSumCounters(String text) {
        tvSumCounters.setText(text);
        final int maxTextSize = 35;
        int width = tvSumCounters.getWidth() == 0 ? Globals.pref.getInt("tvSumCountersWidth", 0) : tvSumCounters.getWidth();
        int height = tvSumCounters.getHeight() == 0 ? Globals.pref.getInt("tvSumCountersHeight", 0) : tvSumCounters.getHeight();
        if (width == 0 || height == 0) return;

        int rows = (int) (maxTextSize / tvSumCounters.getTextSize());
        int maxLength = (int) (width / (tvSumCounters.getTextSize() * 0.57)) * rows;
        int previousRows = 0;
        int previousMaxLength = 0;
        if (tvSumCounters.getTextSize() < maxTextSize) {
            previousRows = (int) (maxTextSize / (tvSumCounters.getTextSize() * 2));
            previousMaxLength = (int) (width / ((tvSumCounters.getTextSize() * 2) * 0.57)) * previousRows;
        }

        if (text.length() > maxLength) {
            tvSumCounters.setTextSize(tvSumCounters.getTextSize() / 2);
            setTextSumCounters(text);
        } else if ((previousRows != 0 && previousMaxLength != 0) && text.length() <= previousMaxLength) {
            tvSumCounters.setTextSize(tvSumCounters.getTextSize() * 2);
            setTextSumCounters(text);
        }

    }

    public void changeActivity(long newOwner) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("owner", newOwner);
        intent.putExtra("movingCounterId", movingCounterId);
        intent.putExtra("copyCounter", copyCounter);
        overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
        startActivity(intent);
        this.finish();
    }

    public boolean getAlbumRotateOrientation() {
        int rotate = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotate) {
            case Surface.ROTATION_0 | Surface.ROTATION_180:
                return false;
            case Surface.ROTATION_90 | Surface.ROTATION_270:
                return true;
        }
        return false;
    }
}
