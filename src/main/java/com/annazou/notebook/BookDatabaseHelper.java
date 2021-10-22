package com.annazou.notebook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.material.internal.NavigationMenu;

public class BookDatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String TABLE_NAME = "book_db";

    private static final String COLUMN_BOOK = "book";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ORDER = "top_order";
    private static final String COLUMN_STAR = "star";

    public BookDatabaseHelper(Context context){
        super(context, TABLE_NAME, null, VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_NAME + " TEXT, " +
                COLUMN_BOOK + " TEXT, " +
                COLUMN_ORDER + " INTEGER, " +
                COLUMN_STAR + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteBook(String book){
        getWritableDatabase().delete(TABLE_NAME, COLUMN_BOOK + "=?", new String[]{book});
    }

    public void addChapter(String fileName, String book){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, fileName);
        cv.put(COLUMN_BOOK, book);
        cv.put(COLUMN_ORDER, 0);
        cv.put(COLUMN_STAR, 0);
        getWritableDatabase().insert(TABLE_NAME, null, cv);
    }

    public void deleteChapter(String fileName, String book){
        getWritableDatabase().delete(TABLE_NAME,  COLUMN_NAME + "=? and " + COLUMN_BOOK + "=?", new String[]{fileName, book});

    }

    public boolean isStar(String fileName){
        int star = 0;
        Cursor cursor = getReadableDatabase().query(TABLE_NAME, null, COLUMN_NAME + "=?", new String[]{fileName}, null, null, null);
        if (cursor != null && cursor.moveToLast()) {
            star = cursor.getInt(cursor.getColumnIndex(COLUMN_STAR));
            cursor.close();
        }
        return star == 1 ? true : false;
    }

    public void setStar(String fileName, boolean star){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STAR, star ? 1 : 0);
        getWritableDatabase().update(TABLE_NAME, cv, COLUMN_NAME + "=?", new String[]{fileName});
    }

    public int getTopOrder(String fileName){
        int order = 0;
        Cursor cursor = getReadableDatabase().query(TABLE_NAME, null, COLUMN_NAME + "=?", new String[]{fileName}, null, null, null);
        if (cursor != null && cursor.moveToLast()) {
            order = cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER));
            cursor.close();
        }
        return order;
    }

    public void setTopOrder(String fileName, int order){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ORDER, order);
        getWritableDatabase().update(TABLE_NAME, cv, COLUMN_NAME + "=?", new String[]{fileName});
    }
}
