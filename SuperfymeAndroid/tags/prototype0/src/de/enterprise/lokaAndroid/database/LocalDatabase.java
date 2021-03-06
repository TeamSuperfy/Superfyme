package de.enterprise.lokaAndroid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDatabase extends SQLiteOpenHelper{

	private static final String DATABASE_NAME = "loka.db";
	private static final int DATABASE_VERSION = 1;
	
	public LocalDatabase(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(UserTable.SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(UserTable.SQL_DROP);
		onCreate(db);
	}
	
}
