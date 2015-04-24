package com.maxsid.gen.multicounter;

import java.math.BigDecimal;

/**
 * Created by Максим Сидоров on 12.01.2015.
 */

public class Counter {

    public static class OPERATIONS {
        public static final byte ADD = 0; //Сложение
        public static final byte SUBTRACT = 1; //Деление
    }

    public static class DISPLAY_VALUE {
        public static final byte VALUE = 0; //Значение
        public static final byte CLICKS = 1; //Кликки
        public static final byte VALUE_AND_LIMIT = 2; //Значение/Лимит
        public static final byte VALUE_AND_CLICKS = 3; //Значение(Клики)
        public static final byte CLICKS_AND_VALUE = 4; //Клики(Значение)
    }

    private String name;
    private BigDecimal value, step, limit;
    private int bgColor, textColor, position, clicks = 0, childCount;
    private long id, owner;
    private boolean isOwner, isBlocked = false;
    private byte display, operation;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Counter(String _name, int _bgColor, int _textColor, String _count, long owner, int childCount,
                   int operation, String step, int position, String limit) {
        setName(_name);
        setBgColor(_bgColor);
        setTextColor(_textColor);
        setValue(_count);
        setOwner(owner);
        setOperation(operation);
        setStep(new BigDecimal(step));
        setPosition(position);
        setChildCount(childCount);
        setLimit(limit);
    }

    public Counter(String _name, int _bgColor, int _textColor, String _count, long owner,
                   int operation, String step, int position, String limit) {
        setName(_name);
        setBgColor(_bgColor);
        setTextColor(_textColor);
        setValue(_count);
        setOwner(owner);
        setOperation(operation);
        setStep(new BigDecimal(step));
        setPosition(position);
        setLimit(limit);
    }

    public Counter(long id, String _name, int _bgColor, int _textColor, String _count, long owner, int childCount,
                   int operation, String step, int position, String limit) {
        setId(id);
        setName(_name);
        setBgColor(_bgColor);
        setTextColor(_textColor);
        setValue(_count);
        setOwner(owner);
        setOperation(operation);
        setStep(new BigDecimal(step));
        setPosition(position);
        setChildCount(childCount);
        setLimit(limit);
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public String getValue() {
        return this.value.toString();
    }

    public BigDecimal getBigDecimalCount() {
        return value;
    }

    public void setValue(long _count) {
        setValue(new BigDecimal(_count));
    }

    public void setBgColor(int _color) {
        this.bgColor = _color;
    }

    public int getBgColor() {
        return this.bgColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean increment() {
        clicks++;
        switch (operation) {
            case OPERATIONS.ADD:
                value = value.add(step);
                if (isLimited() && value.compareTo(limit) == 1) {
                    clicks--;
                    value = value.subtract(step);
                    return false;
                }
                return true;
            case OPERATIONS.SUBTRACT:
                value = value.subtract(step);
                if (isLimited() && value.compareTo(limit) == -1) {
                    clicks--;
                    value = value.add(step);
                    return false;
                }
                return true;
        }
        return false;
    }

    public String getLog() {
        return getId() + ":" + getName() + ", " + getValue() + ", " + getBgColor() + ", " + getTextColor() + ", "
                + getPosition() + ", " + getOwner() + ", " + isOwner() + ", " + isBlocked();
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public boolean isOwner() {
        return isOwner;
    }

    private void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = new BigDecimal(value);
    }


    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        setOperation((byte) operation);
    }

    public void setOperation(byte operation) {
        this.operation = operation;
    }

    public BigDecimal getStep() {
        return step;
    }

    public void setStep(BigDecimal step) {
        this.step = step;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public void setBlocked(String isStringBlocked) {
        this.isBlocked = "1".equals(isStringBlocked);
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        setOwner(childCount > 0);
        this.childCount = childCount;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public String getLimitToString() {
        if (!isLimited())
            return null;
        return getLimit().toString();
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    private void deleteLimit() {
        this.limit = null;
    }

    public void setLimit(String maxOrMin) {
        if (maxOrMin != null)
            setLimit(new BigDecimal(maxOrMin));
        else
            deleteLimit();
    }

    public byte getDisplay() {
        return display;
    }

    public void setDisplay(int display) {
        setDisplay((byte) display);
    }

    public void setDisplay(byte display) {
        this.display = display;
    }

    public boolean cancelLastClick(){
        if (clicks == 0) return false;
        clicks--;
        switch (operation) {
            case OPERATIONS.ADD:
                value = value.subtract(step);
                return true;
            case OPERATIONS.SUBTRACT:
                value = value.add(step);
                return true;
        }
        return false;
    }

    public boolean isLimited() {
        return null != limit;
    } //null - лимит не установлен, т.е. false. Иначе, true
}