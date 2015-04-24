package com.maxsid.gen.multicounter;

/**
 * Created by Максим Сидоров on 12.01.2015.
 */

import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.StrikethroughSpan;
import android.view.ContextMenu;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

public class CountersAdapter extends BaseAdapter implements View.OnClickListener, View.OnCreateContextMenuListener {

    private Context ctx;
    private List<Counter> counters;
    private LayoutInflater lInflater;
    public DB db;
    public long owner;
    private MainActivity main;
    private int butWidth, butHeight;

    public CountersAdapter(Context context) {
        this.ctx = context;
        this.main = (MainActivity) context;
        this.db = main.db;
        this.owner = main.owner;
        this.counters = db.getListCountersFromDB(owner);
        this.lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resizingButtons();
    }

    public String refreshSumCounters() {
        BigDecimal sum = new BigDecimal(0);
        int count = getCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                sum = sum.add(getCounter(i).getBigDecimalCount());
            }
        }
        Globals.sumCounters = sum;
        db.updateOwnerCount(owner, sum.toString());
        main.setTextSumCounters(sum.toString());
        return sum.toString();
    }

    public int getCount() {
        return counters.size();
    }

    public Object getItem(int position) {
        return counters.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public Counter getCounter(int position) {
        return (Counter) getItem(position);
    }

    public Counter getCounterOnId(long id) {
        for (int i = 0; i < getCount(); i++)
            if (getCounter(i).getId() == id)
                return getCounter(i);
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = lInflater.inflate(R.layout.item, main.gridView, false);
        }

        Counter counter = getCounter(position);
        if (counter.getPosition() != position) {
            counter.setPosition(position);
            Globals.db.updatePosition(counter.getId(), position);
        }

        AbsListView.LayoutParams params = (AbsListView.LayoutParams) v.getLayoutParams();
        params.height = butHeight;
        params.width = butWidth;
        v.setLayoutParams(params);

        TextView tvName = (TextView) v.findViewById(R.id.tvName);
        tvName.clearComposingText();
        if (counter.isOwner())
            tvName.setText(Html.fromHtml("<u>" + counter.getName() + "</u>"));
        else if (counter.isBlocked()) {
            SpannableStringBuilder sp = new SpannableStringBuilder(counter.getName());
            sp.setSpan(new StrikethroughSpan(), 0, counter.getName().length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvName.setText(sp);
        } else
            tvName.setText(Html.fromHtml(counter.getName()));

        setLabelValueButton(v, counter);
        if (Globals.pref.getBoolean(Globals.SETTING_KEY_CB_DEFAULT_BUT_COLOR, false) &&
                Globals.pref.getBoolean(Globals.SETTING_KEY_CB_DBC_FOR_ALL_BUT, false)) {
            ((TextView) v.findViewById(R.id.tvName)).setTextColor(
                    Globals.pref.getInt(Globals.SETTING_KEY_CHANGE_BUTTON_T_COLOR, counter.getTextColor()));
            ((TextView) v.findViewById(R.id.tvCount)).setTextColor(
                    Globals.pref.getInt(Globals.SETTING_KEY_CHANGE_BUTTON_T_COLOR, counter.getTextColor()));
            v.setBackgroundColor(
                    Globals.pref.getInt(Globals.SETTING_KEY_CHANGE_BUTTON_COLOR, counter.getBgColor()));
        } else {
            ((TextView) v.findViewById(R.id.tvName)).setTextColor(counter.getTextColor());
            ((TextView) v.findViewById(R.id.tvCount)).setTextColor(counter.getTextColor());
            v.setBackgroundColor(counter.getBgColor());
        }

        v.setTag(position);
        v.setOnClickListener(this);
        v.setOnCreateContextMenuListener(this);

        return v;
    }

    public void onClick(View v) {
        Globals.counter = getCounter((Integer) v.getTag());
        if (!Globals.counter.isOwner()) {
            if (!Globals.counter.isBlocked() && Globals.counter.increment()) {
                db.updateGlobalCounter();
                setLabelValueButton(v, Globals.counter);
                refreshSumCounters();
                if (Globals.pref.getBoolean(Globals.SETTING_KEY_CB_VIBRATION, true))
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                v.startAnimation(main.clickAnim);
            }
        } else {
            main.changeActivity(Globals.counter.getId());
        }
    }

    public void moveCounterOnPosition(long id, int newPosition) {
        Globals.db.moveCounterOnPosition(owner, id, newPosition);
    }

    public void blockOrUnblockCounter(long id, boolean block) {
        getCounterOnId(id).setBlocked(block);
        Globals.db.blockOrUnblockCounter(id, block);
    }

    public void refreshCounters() {
        this.counters = db.getListCountersFromDB(owner);
    }

    private void setLabelValueButton(View v, Counter counter) {
        String text = counter.getValue();
        if (!counter.isOwner()) {
            switch (counter.getDisplay()) {
                case Counter.DISPLAY_VALUE.CLICKS:
                    text = String.valueOf(counter.getClicks());
                    break;
                case Counter.DISPLAY_VALUE.VALUE_AND_LIMIT:
                    if (counter.isLimited())
                        text = counter.getValue() + "/" + counter.getLimitToString();
                    else
                        text = counter.getValue();
                    break;
                case Counter.DISPLAY_VALUE.VALUE_AND_CLICKS:
                    text = counter.getValue() + "(" + counter.getClicks() + ")";
                    break;
                case Counter.DISPLAY_VALUE.CLICKS_AND_VALUE:
                    text = counter.getClicks() + "(" + counter.getValue() + ")";
                    break;
                default:
                    text = counter.getValue();
            }
        }
        ((TextView) v.findViewById(R.id.tvCount)).setText(text);
        final int maxTextSize = Globals.res.getInteger(R.integer.max_count_text_size);
        final int minTextSize = Globals.res.getInteger(R.integer.min_count_text_size);
        double textSize = ((butWidth / (text.length() + 0.0)) / 65.0) * 100.0;
        if (textSize > butHeight)
            textSize = butHeight * 0.8;
        if (textSize > maxTextSize)
            textSize = maxTextSize;
        else if (textSize < minTextSize)
            textSize = minTextSize;
        TextView tvCount;
        tvCount = ((TextView) v.findViewById(R.id.tvCount));
        tvCount.setTextSize((float) textSize);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Globals.counter = getCounter((Integer) v.getTag());

        main.removeDialog(Globals.DIALOG_ITEM);
        main.showDialog(Globals.DIALOG_ITEM);
    }

    public void addCounter(Counter counter) {
        db.insertCounter(counter);
        counters.add(counter);
    }

    public void addGlobalCounter() {
        addCounter(Globals.counter);
    }

    public void removeCounter(int position) {
        counters.remove(position);
    }

    public void deleteCounter(Counter counter) {
        db.deleteCounter(counter);
        removeCounter(counter.getPosition());
    }

    public void deleteGlobalCounter() {
        deleteCounter(Globals.counter);
    }

    public void editCounter(Counter counter) {
        db.updateCounter(counter);
        counters.set(counter.getPosition(), counter);
    }

    public void editGlobalCounter() {
        editCounter(Globals.counter);
    }

    public int clearAdapter() {
        counters.clear();
        return db.clearDatabase();
    }

    public void divideCounter(Counter counter) {
        counter.setChildCount(1);
        db.divideCounter(counter, String.format(ctx.getString(R.string.counter_n), 1));
    }

    public void divideGlobalCounter() {
        divideCounter(Globals.counter);
    }

    public void mergeCounter(Counter counter) {
        db.deleteChild(counter.getId());
        counter.setChildCount(0);
    }

    public void mergeGlobalCounter() {
        mergeCounter(Globals.counter);
    }

    public void resizingButtons() {
        int minWidth = Globals.res.getInteger(R.integer.minimal_button_width);
        int minHeight = Globals.res.getInteger(R.integer.minimal_button_height);
        double width, height;
        if (main.gridView.getWidth() == 0 || main.gridView.getHeight() == 0 || getCount() == 0) {
            butWidth = minWidth;
            butHeight = minHeight;
            return;
        }

        minWidth += (main.gridView.getWidth() - minWidth) * Globals.pref.getFloat("sliderButtonWidth", 0);
        minHeight += (main.gridView.getHeight() - minHeight) * Globals.pref.getFloat("sliderButtonHeight", 0);

        width = main.gridView.getWidth() / (getCount() + 0.0f);
        width = width < minWidth ? minWidth + 0.0f : width;
        int numColumn = (int) ((main.gridView.getWidth()) / width);
        width = main.gridView.getWidth() / (numColumn + 0.0);
        width = width < minWidth ? minWidth + 0.0f : width;

        int numRows = (getCount() % (numColumn + 0.0)) == 0 ? getCount() / numColumn : (getCount() / numColumn) + 1;
        height = main.gridView.getHeight() / (numRows + 0.0f);
        height = height < minHeight ? minHeight : height;
        /*if (main.getAlbumRotateOrientation()) {
            butWidth = (int) height;
            butHeight = (int) width;
        } else {*/
            butWidth = (int) width;
            butHeight = (int) height;
        //}
        main.gridView.setNumColumns(numColumn);
    }
}