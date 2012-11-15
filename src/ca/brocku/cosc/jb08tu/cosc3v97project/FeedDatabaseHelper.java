package ca.brocku.cosc.jb08tu.cosc3v97project;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

class FeedDatabaseHelper extends SQLiteOpenHelper {
	private static final String	DATABASE_NAME		= "feeds.db";
	private static final int	DATABASE_VERSION	= 1;
	
	FeedDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Feeds.FEEDS_TABLE_NAME + " (" + Feeds._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + Feeds.FEED_NAME + " TEXT," + Feeds.FEED_URL + " TEXT" + ");");
	}
	
	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	
	@Override public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
	
	public void addFeed(SQLiteDatabase mDB, String name, String url) {
		mDB.beginTransaction();
		
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Feeds.FEED_NAME, name);
			contentValues.put(Feeds.FEED_URL, url);
			
			mDB.insert(Feeds.FEEDS_TABLE_NAME, null, contentValues);
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	public void editFeed(SQLiteDatabase mDB, String id, String name, String url) {
		mDB.beginTransaction();
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Feeds.FEED_NAME, name);
			contentValues.put(Feeds.FEED_URL, url);
			
			mDB.update(Feeds.FEEDS_TABLE_NAME, contentValues, Feeds._ID + "=?", new String[] {id});
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	public void deleteFeed(SQLiteDatabase mDB, String id) {
		mDB.beginTransaction();
		try {
			mDB.delete(Feeds.FEEDS_TABLE_NAME, Feeds._ID + "=?", new String[] {id});
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	public Feed getFeed(SQLiteDatabase mDB, String id) {
		String columns[] = {Feeds.FEED_NAME, Feeds.FEED_URL};
		Cursor mCursor = mDB.query(Feeds.FEEDS_TABLE_NAME, columns, Feeds._ID + "=?", new String[] {id}, null, null, Feeds.DEFAULT_SORT_ORDER);
		mCursor.moveToFirst();
		
		String name = mCursor.getString(0);
		String url = mCursor.getString(1);
		
		mCursor.close();
		
		return new Feed(id, name, url);
	}
}
