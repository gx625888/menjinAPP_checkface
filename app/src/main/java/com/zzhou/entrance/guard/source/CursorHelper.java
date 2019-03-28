package com.zzhou.entrance.guard.source;

import android.database.Cursor;

public class CursorHelper {
	
	private Cursor cursor;
	
	public CursorHelper(Cursor cursor) {
		this.cursor = cursor;
	}
	
	public boolean getBoolean(String columnName) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(columnName)) != 0;
	}
	
	public float getFloat(String columnName) {
		return cursor.getFloat(cursor.getColumnIndexOrThrow(columnName));
	}
	
	public int getInt(String columnName) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
	}
	
	public long getLong(String columnName) {
		return cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
	}
	
	public String getString(String columnName) {
		return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
	}
	
	public boolean moveToNext() {
		return cursor.moveToNext();
	}
}
