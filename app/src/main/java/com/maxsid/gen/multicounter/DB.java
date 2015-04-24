package com.maxsid.gen.multicounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxim on 25.01.15.
 */
public class DB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_COUNTERS = "counters";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_BG_COLOR = "bg_color";
    public static final String COLUMN_TEXT_COLOR = "text_color";
    public static final String COLUMN_OWNER = "owner";
    public static final String COLUMN_OPERATION = "operation";
    public static final String COLUMN_STEP = "step";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_BLOCKED = "blocked";
    public static final String COLUMN_CLICKS = "display_clicks";
    public static final String COLUMN_LIMIT = "value_limit";
    public static final String COLUMN_DISPLAY = "display";


    private static final String CREATE_DATABASE =
            "create table " + TABLE_COUNTERS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_NAME + " text, " +
                    COLUMN_VALUE + " text, " +
                    COLUMN_BG_COLOR + " integer, " +
                    COLUMN_TEXT_COLOR + " integer, " +
                    COLUMN_OPERATION + " integer, " +
                    COLUMN_STEP + " text, " +
                    COLUMN_OWNER + " integer, " +
                    COLUMN_POSITION + " integer," +
                    COLUMN_BLOCKED + " boolean default(0), " +
                    COLUMN_CLICKS + " integer default(0)," +
                    COLUMN_LIMIT + " integer default(null), " +
                    COLUMN_DISPLAY + " integer default(0));";
    private static final String ADD_MAIN_COUNTER =
            "insert into '" + TABLE_COUNTERS + "'" +
                    " (" + COLUMN_ID + ", " + COLUMN_VALUE + ", " + COLUMN_NAME + ", " +
                    COLUMN_BG_COLOR + ", " + COLUMN_TEXT_COLOR + ", " + COLUMN_OPERATION +
                    ", " + COLUMN_STEP + ", " + COLUMN_BLOCKED + " ) " +
                    "values (0,0,'Главный',-1, -16777216, 1, 1, 0);";


    SQLiteDatabase sqd;

    public DB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sqd = getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(Globals.LOG_TAG, "---CREATE DATABASE---\n" + CREATE_DATABASE + "\n" + ADD_MAIN_COUNTER);
        Globals.logInfo(CREATE_DATABASE + "\n\n" + ADD_MAIN_COUNTER);
        db.execSQL(CREATE_DATABASE);
        db.execSQL(ADD_MAIN_COUNTER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(Globals.LOG_TAG, "---UPGRADE DATABASE---\noldVersion: " + oldVersion + "\nnewVersion: " + newVersion);
    }


    public long insertCounter(Counter counter) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, counter.getName());
        cv.put(COLUMN_VALUE, counter.getValue());
        cv.put(COLUMN_BG_COLOR, counter.getBgColor());
        cv.put(COLUMN_TEXT_COLOR, counter.getTextColor());
        cv.put(COLUMN_OWNER, counter.getOwner());
        cv.put(COLUMN_OPERATION, counter.getOperation());
        cv.put(COLUMN_STEP, counter.getStep().toString());
        cv.put(COLUMN_POSITION, counter.getPosition());
        cv.put(COLUMN_BLOCKED, counter.isBlocked());
        cv.put(COLUMN_LIMIT, counter.getLimitToString());
        cv.put(COLUMN_DISPLAY, counter.getDisplay());
        counter.setId(sqd.insert(TABLE_COUNTERS, null, cv));
        Log.d(Globals.LOG_TAG, "Insert counter in DB with id=" + counter.getId());
        return counter.getId();
    }

    public long insertGlobalCounter() {
        return insertCounter(Globals.counter);
    }

    public List<Counter> getListCountersFromDB(long owner) {
        List<Counter> list = new ArrayList<>();

        Cursor c = sqd.query(TABLE_COUNTERS, null, "owner = ? and id != ?", new String[]{String.valueOf(owner),
                String.valueOf(0)}, null, null, COLUMN_POSITION);
        Log.d(Globals.LOG_TAG, "Counters in db: " + c.getCount());
        if (c.moveToFirst()) {
            int idIndex = c.getColumnIndex(COLUMN_ID);
            int nameIndex = c.getColumnIndex(COLUMN_NAME);
            int countIndex = c.getColumnIndex(COLUMN_VALUE);
            int bgColIndex = c.getColumnIndex(COLUMN_BG_COLOR);
            int textColIndex = c.getColumnIndex(COLUMN_TEXT_COLOR);
            int ownerIndex = c.getColumnIndex(COLUMN_OWNER);
            int operationIndex = c.getColumnIndex(COLUMN_OPERATION);
            int stepIndex = c.getColumnIndex(COLUMN_STEP);
            int positionIndex = c.getColumnIndex(COLUMN_POSITION);
            int blockedIndex = c.getColumnIndex(COLUMN_BLOCKED);
            int clicksIndex = c.getColumnIndex(COLUMN_CLICKS);
            int limitIndex = c.getColumnIndex(COLUMN_LIMIT);
            int displayIndex = c.getColumnIndex(COLUMN_DISPLAY);
            do {
                int childCount = sqd.query(TABLE_COUNTERS, null, " owner = ?",
                        new String[]{c.getString(idIndex)}, null, null, null).getCount();

                Counter counter = new Counter(c.getLong(idIndex), c.getString(nameIndex),
                        c.getInt(bgColIndex), c.getInt(textColIndex), c.getString(countIndex),
                        c.getLong(ownerIndex), childCount, c.getInt(operationIndex), c.getString(stepIndex), c.getInt(positionIndex),
                        c.getString(limitIndex));
                counter.setBlocked(c.getString(blockedIndex));
                counter.setClicks(c.getInt(clicksIndex));
                counter.setDisplay(c.getInt(displayIndex));
                Log.d(Globals.LOG_TAG, "Counter " + counter.getLog());
                list.add(counter);
            } while (c.moveToNext());

        }
        return list;
    }

    public int updateCounter(Counter counter) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, counter.getName());
        cv.put(COLUMN_VALUE, counter.getValue());
        cv.put(COLUMN_BG_COLOR, counter.getBgColor());
        cv.put(COLUMN_TEXT_COLOR, counter.getTextColor());
        cv.put(COLUMN_OPERATION, counter.getOperation());
        cv.put(COLUMN_STEP, counter.getStep().toString());
        cv.put(COLUMN_CLICKS, counter.getClicks());
        cv.put(COLUMN_LIMIT, counter.getLimitToString());
        cv.put(COLUMN_DISPLAY, counter.getDisplay());
        int countUpd = sqd.update(TABLE_COUNTERS, cv, "id = ?", new String[]{String.valueOf(counter.getId())});
        Log.d(Globals.LOG_TAG, "Rows updated: " + countUpd + ". Counter id: " + counter.getId());
        return countUpd;
    }

    public int updateGlobalCounter() {
        return updateCounter(Globals.counter);
    }

    public int deleteCounter(Counter counter) {
        int delCount = sqd.delete(TABLE_COUNTERS, "id = ? or owner = ?",
                new String[]{String.valueOf(counter.getId()), String.valueOf(counter.getId())});
        Log.d(Globals.LOG_TAG, "Rows deleted: " + delCount + ". Counter id: " + counter.getId());
        return delCount;
    }

    public void blockOrUnblockCounter(long id, boolean block) {
        //int res = block ? 1 : 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_BLOCKED, block);
        sqd.update(TABLE_COUNTERS, cv, "id = ?", new String[]{String.valueOf(id)});
        if (getCounterOnId(id).isOwner()) {
            List<Counter> list = getListCountersFromDB(id);
            for (int i = 0; i < list.size(); i++) {
                blockOrUnblockCounter(list.get(i).getId(), block);
            }
        }
    }

    public int deleteGlobalCounter() {
        return deleteCounter(Globals.counter);
    }

    public int clearDatabase() {
        int delCount = sqd.delete(TABLE_COUNTERS, "id != 0", null);
        Log.d(Globals.LOG_TAG, "Cleare database. Rows deleted: " + delCount);
        return delCount;
    }

    public int resetAllCounters() {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_VALUE, 0);
        cv.put(COLUMN_CLICKS, 0);
        int updCount = sqd.update(TABLE_COUNTERS, cv, null, null);
        Log.d(Globals.LOG_TAG, "Reseted " + updCount + " counters");
        return updCount;
    }

    public long divideCounter(Counter counter, String newName) {
        Counter c = new Counter(newName, counter.getTextColor(), counter.getBgColor(),
                counter.getValue(), counter.getId(), counter.getOperation(), counter.getStep().toString(), 0,
                counter.getLimitToString());
        c.setBlocked(counter.isBlocked());
        c.setDisplay(counter.getDisplay());
        return insertCounter(c);
    }

    public int updateOwnerCount(long ownerId, String ownerCount) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_VALUE, ownerCount);
        int countUpd = sqd.update(TABLE_COUNTERS, cv, "id = ?", new String[]{String.valueOf(ownerId)});
        Log.d(Globals.LOG_TAG, "Owner updated: " + countUpd + " rows. Owner id: " + ownerId);
        return countUpd;
    }

    public Counter getCounterOnId(long id) {
        Cursor c = sqd.query(TABLE_COUNTERS, null, "id == ?", new String[]{String.valueOf(id)}, null, null, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
            int idIndex = c.getColumnIndex(COLUMN_ID);
            int nameIndex = c.getColumnIndex(COLUMN_NAME);
            int countIndex = c.getColumnIndex(COLUMN_VALUE);
            int bgColIndex = c.getColumnIndex(COLUMN_BG_COLOR);
            int textColIndex = c.getColumnIndex(COLUMN_TEXT_COLOR);
            int ownerIndex = c.getColumnIndex(COLUMN_OWNER);
            int operationIndex = c.getColumnIndex(COLUMN_OPERATION);
            int stepIndex = c.getColumnIndex(COLUMN_STEP);
            int positionIndex = c.getColumnIndex(COLUMN_POSITION);
            int blockedIndex = c.getColumnIndex(COLUMN_BLOCKED);
            int clicksIndex = c.getColumnIndex(COLUMN_CLICKS);
            int limitIndex = c.getColumnIndex(COLUMN_LIMIT);
            int displayIndex = c.getColumnIndex(COLUMN_DISPLAY);
            int childCount = sqd.query(TABLE_COUNTERS, null, " owner = ?",
                    new String[]{c.getString(idIndex)}, null, null, null).getCount();
            Counter counter = new Counter(c.getLong(idIndex), c.getString(nameIndex),
                    c.getInt(bgColIndex), c.getInt(textColIndex), c.getString(countIndex),
                    c.getLong(ownerIndex), childCount, c.getInt(operationIndex), c.getString(stepIndex), c.getInt(positionIndex),
                    c.getString(limitIndex));
            counter.setBlocked(c.getString(blockedIndex));
            counter.setClicks(c.getInt(clicksIndex));
            counter.setDisplay(c.getInt(displayIndex));
            Log.d(Globals.LOG_TAG, "Counter " + counter.getLog());
            return counter;
        }
        return null;
    }

    public int deleteChild(long owner) {
        int delCount = sqd.delete(TABLE_COUNTERS, "owner == ?", new String[]{String.valueOf(owner)});
        Log.d(Globals.LOG_TAG, "Merge counters in " + owner + ". Child deleted: " + delCount);
        return delCount;
    }

    public void moveCounterOnPosition(long owner, long id, int newPosition) {
        String movingCounter = "UPDATE counters\n" +
                "   SET position = " + newPosition + ", \n" +
                "                  owner = " + owner + "\n" +
                " WHERE id = " + id + ";";

        displacePosition(owner, id, newPosition);
        sqd.execSQL(movingCounter);
    }

    public void displacePosition(long owner, long excludId, int firstPosition) {
        String query;
        if (excludId > 0) {
            query = "UPDATE counters\n" +
                    "   SET position = position + 1\n" +
                    " WHERE position >= " + firstPosition + " AND \n" +
                    "       owner = " + owner + " AND\n" +
                    "       id != " + excludId + ";\n";
        } else {
            query = "UPDATE counters\n" +
                    "   SET position = position + 1\n" +
                    " WHERE position >= " + firstPosition + " AND \n" +
                    "       owner = " + owner + " AND;";
        }

        sqd.execSQL(query);
    }


    public void updatePosition(long id, int newPosition) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_POSITION, newPosition);
        sqd.update(TABLE_COUNTERS, cv, "id = ?", new String[]{String.valueOf(id)});
    }

    public Counter copyCounter(long owner, long id, int newPosition, boolean displacePosition) {
        if (displacePosition) displacePosition(owner, id, newPosition);
        Counter counter = getCounterOnId(id);
        counter.setPosition(newPosition);
        counter.setOwner(owner);
        counter.setId(insertCounter(counter));
        return counter;
    }

    public void copyAllCountersOnOwner(long owner, long newOwner) {
        List<Counter> list = getListCountersFromDB(owner);
        for (int i = 0; i < list.size(); i++) {
            long origId = list.get(i).getId();
            Counter newCounter = copyCounter(newOwner, list.get(i).getId(), list.get(i).getPosition(), false);
            if (newCounter.isOwner()) {
                copyAllCountersOnOwner(origId, newCounter.getId());
            }
        }
    }
}
