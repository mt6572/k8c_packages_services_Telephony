package com.lava.phone.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Database {
	private DatabaseHelper  databaseHelper = null;
	private Context context;
	public Database(Context context){
		databaseHelper = DatabaseHelper.getInstance(context);
		this.context = context;
	}
	DatabaseHelper getDatabaseHelper(){
		if(databaseHelper==null){
			databaseHelper = DatabaseHelper.getInstance(context);
		}
		return databaseHelper;
	}
	public SQLiteDatabase getSQLiteDatabase(){
		return getDatabaseHelper().getWritableDatabase();
	}
	public void executeSQL(String sql){
		getSQLiteDatabase().execSQL(sql);
	}
	public void executeSQL(String sql,String...args){
		getSQLiteDatabase().execSQL(sql,args);
	}
	
	/**
	 * @param sql
	 * @param args
	 * @return
	 */
	public Cursor executeQuery(String sql,String...args){
		return getSQLiteDatabase().rawQuery(sql, args);
	}
	public long insert(String table,ContentValues values){
		long id = getSQLiteDatabase().insert(table,null,  values);
		return id;
	}
	public void update(String table,ContentValues values,String whereClause,String... whereArgs){
		getSQLiteDatabase().update(table, values, whereClause, whereArgs);
	}
	public void close(){
		getDatabaseHelper().close();
	}
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
}
