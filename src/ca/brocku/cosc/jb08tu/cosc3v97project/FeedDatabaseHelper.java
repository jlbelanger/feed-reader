package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class FeedDatabaseHelper extends SQLiteOpenHelper {
	private static final String	DATABASE_NAME		= "feeds.db";
	private static final int	DATABASE_VERSION	= 3;
	
	FeedDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Feeds.FEEDS_TABLE_NAME + " (" + Feeds._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + Feeds.FEED_NAME + " TEXT," + Feeds.FEED_URL + " TEXT" + ");");
		db.execSQL("CREATE TABLE " + Feeds.FEED_ITEMS_TABLE_NAME + " (" + Feeds._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + Feeds.FEED_ITEM_FEED_ID + " TEXT," + Feeds.FEED_ITEM_TITLE + " TEXT," + Feeds.FEED_ITEM_PUB_DATE + " DATETIME," + Feeds.FEED_ITEM_LINK + " TEXT," + Feeds.FEED_ITEM_DESCRIPTION + " TEXT," + Feeds.FEED_ITEM_CONTENT_ENCODED + " TEXT," + Feeds.FEED_ITEM_IS_READ + " TINYINT(1)" + ");");
	}
	
	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion == 1) {
			db.execSQL("CREATE TABLE " + Feeds.FEED_ITEMS_TABLE_NAME + " (" + Feeds._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + Feeds.FEED_ITEM_FEED_ID + " TEXT," + Feeds.FEED_ITEM_TITLE + " TEXT," + Feeds.FEED_ITEM_PUB_DATE + " DATETIME," + Feeds.FEED_ITEM_LINK + " TEXT," + Feeds.FEED_ITEM_DESCRIPTION + " TEXT," + Feeds.FEED_ITEM_CONTENT_ENCODED + " TEXT," + Feeds.FEED_ITEM_IS_READ + " TINYINT(1)" + ");");
		}
		else if(oldVersion == 2) {
			db.execSQL("DROP TABLE " + Feeds.FEED_ITEMS_TABLE_NAME + ";");
			db.execSQL("CREATE TABLE " + Feeds.FEED_ITEMS_TABLE_NAME + " (" + Feeds._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + Feeds.FEED_ITEM_FEED_ID + " TEXT," + Feeds.FEED_ITEM_TITLE + " TEXT," + Feeds.FEED_ITEM_PUB_DATE + " DATETIME," + Feeds.FEED_ITEM_LINK + " TEXT," + Feeds.FEED_ITEM_DESCRIPTION + " TEXT," + Feeds.FEED_ITEM_CONTENT_ENCODED + " TEXT," + Feeds.FEED_ITEM_IS_READ + " TINYINT(1)" + ");");
		}
	}
	
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
	
	public void addFeedItem(Context context, SQLiteDatabase mDB, FeedItem feedItem) {
		mDB.beginTransaction();
		try {
			Log.i("feeddd", "---add feed item with date " + feedItem.getSortDate());
			ContentValues contentValues = new ContentValues();
			contentValues.put(Feeds.FEED_ITEM_FEED_ID, feedItem.getFeedId());
			contentValues.put(Feeds.FEED_ITEM_TITLE, feedItem.getTitle());
			contentValues.put(Feeds.FEED_ITEM_PUB_DATE, feedItem.getSortDate());
			contentValues.put(Feeds.FEED_ITEM_LINK, feedItem.getLink());
			contentValues.put(Feeds.FEED_ITEM_DESCRIPTION, feedItem.getDescription());
			contentValues.put(Feeds.FEED_ITEM_CONTENT_ENCODED, feedItem.getContent());
			contentValues.put(Feeds.FEED_ITEM_IS_READ, 0);
			mDB.insert(Feeds.FEED_ITEMS_TABLE_NAME, null, contentValues);
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	public void editFeed(SQLiteDatabase mDB, String feedId, String name, String url) {
		mDB.beginTransaction();
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Feeds.FEED_NAME, name);
			contentValues.put(Feeds.FEED_URL, url);
			mDB.update(Feeds.FEEDS_TABLE_NAME, contentValues, Feeds._ID + "=?", new String[] {feedId});
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	public void deleteFeed(SQLiteDatabase mDB, String feedId) {
		mDB.beginTransaction();
		try {
			mDB.delete(Feeds.FEEDS_TABLE_NAME, Feeds._ID + "=?", new String[] {feedId});
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	public void markFeedItemAsRead(SQLiteDatabase mDB, String feedItemId) {
		mDB.beginTransaction();
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Feeds.FEED_ITEM_IS_READ, "1");
			mDB.update(Feeds.FEED_ITEMS_TABLE_NAME, contentValues, Feeds._ID + "=?", new String[] {feedItemId});
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	public Feed getFeed(SQLiteDatabase mDB, String feedId) {
		String columns[] = {Feeds.FEED_NAME, Feeds.FEED_URL};
		Cursor mCursor = mDB.query(Feeds.FEEDS_TABLE_NAME, columns, Feeds._ID + "=?", new String[] {feedId}, null, null, Feeds.FEEDS_DEFAULT_SORT_ORDER);
		mCursor.moveToFirst();
		Feed feed = null;
		if(mCursor.getCount() > 0) {
			String name = mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_NAME));
			String url = mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_URL));
			feed = new Feed(feedId, name, url);
		}
		mCursor.close();
		return feed;
	}
	
	public FeedItem getFeedItem(SQLiteDatabase mDB, String feedItemId) {
		String columns[] = {Feeds.FEED_ITEM_FEED_ID, Feeds.FEED_ITEM_TITLE, Feeds.FEED_ITEM_PUB_DATE, Feeds.FEED_ITEM_LINK, Feeds.FEED_ITEM_DESCRIPTION, Feeds.FEED_ITEM_CONTENT_ENCODED, Feeds.FEED_ITEM_IS_READ};
		Cursor mCursor = mDB.query(Feeds.FEED_ITEMS_TABLE_NAME, columns, Feeds._ID + "=?", new String[] {feedItemId}, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
		mCursor.moveToFirst();
		FeedItem feedItem = null;
		if(mCursor.getCount() > 0) {
			String feedId = mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_FEED_ID));
			String title = mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_TITLE));
			String pubDate = mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_PUB_DATE));
			String link = mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_LINK));
			String description = mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_DESCRIPTION));
			String contentEncoded = mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_CONTENT_ENCODED));
			String strIsRead = mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_IS_READ));
			boolean isRead = false;
			if(strIsRead.equals("1")) {
				isRead = true;
			}
			feedItem = new FeedItem(feedItemId, feedId, title, pubDate, link, description, contentEncoded, isRead);
		}
		mCursor.close();
		return feedItem;
	}
	
	public boolean doesFeedItemExist(SQLiteDatabase mDB, String feedId, String title, String pubDate) {
		String columns[] = {Feeds._ID};
		Cursor mCursor = mDB.query(Feeds.FEED_ITEMS_TABLE_NAME, columns, Feeds.FEED_ITEM_FEED_ID + "=? AND " + Feeds.FEED_ITEM_TITLE + "=? AND " + Feeds.FEED_ITEM_PUB_DATE + "=?", new String[] {feedId, title, pubDate}, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
		int count = mCursor.getCount();
		mCursor.close();
		if(count > 0) {
			return true;
		}
		return false;
	}
	
	public int getNumUnreadFeedItems(SQLiteDatabase mDB, String feedId) {
		String columns[] = {Feeds._ID};
		Cursor mCursor = mDB.query(Feeds.FEED_ITEMS_TABLE_NAME, columns, Feeds.FEED_ITEM_FEED_ID + "=? AND " + Feeds.FEED_ITEM_IS_READ + "=?", new String[] {feedId, "0"}, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
		int count = mCursor.getCount();
		mCursor.close();
		return count;
	}
	
	public void addNewFeedItemsToDatabase(List<FeedItem> feedItems, Context context, SQLiteDatabase mDB) {
		for(FeedItem feedItem : feedItems) {
			if(!this.doesFeedItemExist(mDB, feedItem.getFeedId(), feedItem.getTitle(), feedItem.getSortDate())) {
				this.addFeedItem(context, mDB, feedItem);
			}
		}
	}
	
	public List<Map<String, String>> getFeedMap(SQLiteDatabase mDB, Cursor mCursor) {
		List<Map<String, String>> feedList = new ArrayList<Map<String, String>>();
		mCursor.moveToFirst();
		int count = mCursor.getCount();
		String[] numItems = new String[count];
		int num = 0;
		for(int i = 0; i < count; i++) {
			num = this.getNumUnreadFeedItems(mDB, mCursor.getString(mCursor.getColumnIndex(Feeds._ID)));
			numItems[i] = num + " item";
			if(num != 1) {
				numItems[i] = numItems[i] + "s";
			}
			Map<String, String> item = new HashMap<String, String>(2);
			item.put("name", mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_NAME)));
			item.put("numItems", numItems[i]);
			feedList.add(item);
			mCursor.moveToNext();
		}
		return feedList;
	}
	
	public List<Map<String, String>> getFeedItemMap(final Context context, Cursor mCursor) {
		List<Map<String, String>> feedItemsList = new ArrayList<Map<String, String>>();
		mCursor.moveToFirst();
		DateFormat inDateFormat = Utilities.getDateFormatter("yyyy-MM-dd HH:mm:ss");
		DateFormat outDateFormat = Utilities.getDateFormatter(context);
		Date date = null;
		int count = mCursor.getCount();
		for(int i = 0; i < count; i++) {
			Map<String, String> item = new HashMap<String, String>(2);
			item.put("title", mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_TITLE)));
			try {
				date = inDateFormat.parse(mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_PUB_DATE)));
			}
			catch(ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			item.put("pubDate", outDateFormat.format(date));
			feedItemsList.add(item);
			mCursor.moveToNext();
		}
		return feedItemsList;
	}
}
