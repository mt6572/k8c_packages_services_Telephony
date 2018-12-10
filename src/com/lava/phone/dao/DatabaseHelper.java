package com.lava.phone.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{
	private static final String TAG = DatabaseHelper.class.getSimpleName();

	private DatabaseHelper(Context context) {
		super(context, DATABASENAME, null,version);
		
	}
	private static DatabaseHelper databaseHelper=null;
	public static DatabaseHelper getInstance(Context context){
		if(databaseHelper==null){
			databaseHelper = new DatabaseHelper(context);
		}
		return databaseHelper;
	}
	public final static String DATABASENAME="num.db";
	public final static int version = 1;
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "create database:"+DATABASENAME);
		db.execSQL("create table if not exists num(_id integer primary key," +
				"number varchar(20)," +  //原始
			/*	"number1 varchar(20)," + //+91
				"number2 varchar(20)," + //091
				"number3 varchar(20)," + //0
				"number4 varchar(20)," + //+86 china test
*/				"date datetime)");
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "update database:"+DATABASENAME);
	}
}
