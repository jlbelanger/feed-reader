package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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
	
	// add a new feed to the database
	public Feed addFeed(SQLiteDatabase mDB, String name, String url) {
		mDB.beginTransaction();
		long feedId = 0;
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Feeds.FEED_NAME, name);
			contentValues.put(Feeds.FEED_URL, url);
			feedId = mDB.insert(Feeds.FEEDS_TABLE_NAME, null, contentValues);
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
		return new Feed("" + feedId, name, url);
	}
	
	// add a new feed item to the database
	public FeedItem addFeedItem(SQLiteDatabase mDB, FeedItem feedItem) {
		mDB.beginTransaction();
		long feedItemId = 0;
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Feeds.FEED_ITEM_FEED_ID, feedItem.getFeedId());
			contentValues.put(Feeds.FEED_ITEM_TITLE, feedItem.getTitle());
			contentValues.put(Feeds.FEED_ITEM_PUB_DATE, feedItem.getSortDate());
			contentValues.put(Feeds.FEED_ITEM_LINK, feedItem.getLink());
			contentValues.put(Feeds.FEED_ITEM_DESCRIPTION, feedItem.getDescription());
			contentValues.put(Feeds.FEED_ITEM_CONTENT_ENCODED, feedItem.getContent());
			contentValues.put(Feeds.FEED_ITEM_IS_READ, 0);
			feedItemId = mDB.insert(Feeds.FEED_ITEMS_TABLE_NAME, null, contentValues);
			feedItem.setId("" + feedItemId);
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
		return feedItem;
	}
	
	// update a feed
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
	
	// delete a feed
	public void deleteFeed(SQLiteDatabase mDB, String feedId) {
		mDB.beginTransaction();
		try {
			mDB.delete(Feeds.FEEDS_TABLE_NAME, Feeds._ID + "=?", new String[] {feedId});
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
		this.deleteAllFeedItems(mDB, feedId);
	}
	
	// delete all feed items belonging to a feed
	private void deleteAllFeedItems(SQLiteDatabase mDB, String feedId) {
		mDB.beginTransaction();
		try {
			mDB.delete(Feeds.FEED_ITEMS_TABLE_NAME, Feeds.FEED_ITEM_FEED_ID + "=?", new String[] {feedId});
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	// update a feed item's isRead status
	public void markFeedItemAsRead(SQLiteDatabase mDB, String feedItemId, boolean markAsRead) {
		mDB.beginTransaction();
		try {
			ContentValues contentValues = new ContentValues();
			if(markAsRead) {
				contentValues.put(Feeds.FEED_ITEM_IS_READ, "1");
			}
			else {
				contentValues.put(Feeds.FEED_ITEM_IS_READ, "0");
			}
			mDB.update(Feeds.FEED_ITEMS_TABLE_NAME, contentValues, Feeds._ID + "=?", new String[] {feedItemId});
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	// update all feed items in a feed's isRead status to read
	public void markAllFeedsItemsAsRead(SQLiteDatabase mDB, String feedId) {
		mDB.beginTransaction();
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Feeds.FEED_ITEM_IS_READ, "1");
			mDB.update(Feeds.FEED_ITEMS_TABLE_NAME, contentValues, Feeds.FEED_ITEM_FEED_ID + "=? AND " + Feeds.FEED_ITEM_IS_READ + "=?", new String[] {feedId, "0"});
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	// update all feed items in the database's isRead status to read
	public void markAllFeedsItemsAsRead(SQLiteDatabase mDB) {
		mDB.beginTransaction();
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Feeds.FEED_ITEM_IS_READ, "1");
			mDB.update(Feeds.FEED_ITEMS_TABLE_NAME, contentValues, Feeds.FEED_ITEM_IS_READ + "=?", new String[] {"0"});
			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}
	
	// adds feed items in list to database
	public void addNewFeedItemsToDatabase(SQLiteDatabase mDB, List<FeedItem> feedItems) {
		for(FeedItem feedItem : feedItems) {
			if(!this.doesFeedItemExist(mDB, feedItem.getFeedId(), feedItem.getTitle(), feedItem.getSortDate())) {
				this.addFeedItem(mDB, feedItem);
			}
		}
	}
	
	// returns row for feed with given id
	public Feed getFeed(SQLiteDatabase mDB, String feedId) {
		String[] columns = {Feeds.FEED_NAME, Feeds.FEED_URL};
		String selection = Feeds._ID + "=?";
		String[] selectionArgs = new String[] {feedId};
		Cursor mCursor = mDB.query(Feeds.FEEDS_TABLE_NAME, columns, selection, selectionArgs, null, null, Feeds.FEEDS_DEFAULT_SORT_ORDER);
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
	
	// returns row for feed item with given id
	public FeedItem getFeedItem(SQLiteDatabase mDB, String feedItemId) {
		String[] columns = {Feeds.FEED_ITEM_FEED_ID, Feeds.FEED_ITEM_TITLE, Feeds.FEED_ITEM_PUB_DATE, Feeds.FEED_ITEM_LINK, Feeds.FEED_ITEM_DESCRIPTION, Feeds.FEED_ITEM_CONTENT_ENCODED, Feeds.FEED_ITEM_IS_READ};
		String selection = Feeds._ID + "=?";
		String[] selectionArgs = new String[] {feedItemId};
		Cursor mCursor = mDB.query(Feeds.FEED_ITEMS_TABLE_NAME, columns, selection, selectionArgs, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
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
	
	// returns row for feed before/after feed with given id
	public FeedItem getNextFeedItem(SQLiteDatabase mDB, FeedItem feedItem, boolean getPrevious, boolean showAll) {
		String[] columns = {Feeds._ID, Feeds.FEED_ITEM_TITLE};
		String selection = "";
		String[] selectionArgs;
		if(showAll) {
			selection = Feeds.FEED_ITEM_FEED_ID + "=?";
			selectionArgs = new String[] {feedItem.getFeedId()};
		}
		else {
			selection = Feeds.FEED_ITEM_FEED_ID + "=? AND (" + Feeds.FEED_ITEM_IS_READ + "=? OR " + Feeds._ID + "=?)";
			selectionArgs = new String[] {feedItem.getFeedId(), "0", feedItem.getId()};
		}
		String orderBy = Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER;
		Cursor mCursor = mDB.query(Feeds.FEED_ITEMS_TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);
		mCursor.moveToFirst();
		int numFeedItems = mCursor.getCount();
		String feedItemId;
		boolean getThisOne = false;
		for(int i = 0; i < numFeedItems; i++) {
			feedItemId = mCursor.getString(mCursor.getColumnIndex(Feeds._ID));
			mCursor.moveToNext();
			if(feedItemId.equals(feedItem.getId())) {
				getThisOne = true;
				break;
			}
		}
		if(getPrevious) {
			mCursor.moveToPrevious();
			mCursor.moveToPrevious();
		}
		if(getThisOne && !mCursor.isAfterLast() && !mCursor.isBeforeFirst()) {
			feedItemId = mCursor.getString(mCursor.getColumnIndex(Feeds._ID));
			mCursor.close();
			return this.getFeedItem(mDB, feedItemId);
		}
		mCursor.close();
		return null;
	}
	
	// returns true if there is a feed with the given id
	public boolean doesFeedExist(SQLiteDatabase mDB, String feedId) {
		String[] columns = {Feeds._ID};
		String selection = Feeds._ID + "=?";
		String[] selectionArgs = new String[] {feedId};
		Cursor mCursor = mDB.query(Feeds.FEEDS_TABLE_NAME, columns, selection, selectionArgs, null, null, Feeds.FEEDS_DEFAULT_SORT_ORDER);
		int numFeeds = mCursor.getCount();
		mCursor.close();
		if(numFeeds > 0) {
			return true;
		}
		return false;
	}
	
	// returns true if there is a feed item with the given id
	public boolean doesFeedItemExist(SQLiteDatabase mDB, String feedId, String title, String pubDate) {
		String[] columns = {Feeds._ID};
		String selection = Feeds.FEED_ITEM_FEED_ID + "=? AND " + Feeds.FEED_ITEM_TITLE + "=? AND " + Feeds.FEED_ITEM_PUB_DATE + "=?";
		String[] selectionArgs = new String[] {feedId, title, pubDate};
		Cursor mCursor = mDB.query(Feeds.FEED_ITEMS_TABLE_NAME, columns, selection, selectionArgs, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
		int numFeedItems = mCursor.getCount();
		mCursor.close();
		if(numFeedItems > 0) {
			return true;
		}
		return false;
	}
	
	// returns the number of unread feed items
	public int getNumUnreadFeedItems(SQLiteDatabase mDB, String feedId) {
		String[] columns = {Feeds._ID};
		String selection = Feeds.FEED_ITEM_FEED_ID + "=? AND " + Feeds.FEED_ITEM_IS_READ + "=?";
		String[] selectionArgs = new String[] {feedId, "0"};
		Cursor mCursor = mDB.query(Feeds.FEED_ITEMS_TABLE_NAME, columns, selection, selectionArgs, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
		int numFeedItems = mCursor.getCount();
		mCursor.close();
		return numFeedItems;
	}
	
	// returns id of the nth feed in the database
	public String getFeedId(SQLiteDatabase mDB, int position) {
		String[] columns = {Feeds._ID};
		Cursor mCursor = mDB.query(Feeds.FEEDS_TABLE_NAME, columns, null, null, null, null, Feeds.FEEDS_DEFAULT_SORT_ORDER);
		mCursor.moveToPosition(position);
		String feedId = mCursor.getString(mCursor.getColumnIndex(Feeds._ID));
		mCursor.close();
		return feedId;
	}
	
	// returns a list containing each feed in the database
	public List<Feed> getFeedList(SQLiteDatabase mDB) {
		String[] columns = {Feeds._ID};
		Cursor mCursor = mDB.query(Feeds.FEEDS_TABLE_NAME, columns, null, null, null, null, Feeds.FEEDS_DEFAULT_SORT_ORDER);
		List<Feed> feedList = new LinkedList<Feed>();
		mCursor.moveToFirst();
		int numFeeds = mCursor.getCount();
		String feedId;
		for(int i = 0; i < numFeeds; i++) {
			feedId = mCursor.getString(mCursor.getColumnIndex(Feeds._ID));
			feedList.add(this.getFeed(mDB, feedId));
			mCursor.moveToNext();
		}
		mCursor.close();
		return feedList;
	}
	
	// returns a map containing each feed in the database
	public List<Map<String, String>> getFeedMap(SQLiteDatabase mDB) {
		String[] columns = {Feeds._ID, Feeds.FEED_NAME};
		Cursor mCursor = mDB.query(Feeds.FEEDS_TABLE_NAME, columns, null, null, null, null, Feeds.FEEDS_DEFAULT_SORT_ORDER);
		List<Map<String, String>> feedMap = new ArrayList<Map<String, String>>();
		mCursor.moveToFirst();
		int numFeeds = mCursor.getCount();
		int numUnreadItems = 0;
		for(int i = 0; i < numFeeds; i++) {
			Map<String, String> item = new HashMap<String, String>(2);
			item.put(Feeds.FEED_NAME, mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_NAME)));
			numUnreadItems = this.getNumUnreadFeedItems(mDB, mCursor.getString(mCursor.getColumnIndex(Feeds._ID)));
			item.put("numItems", Utilities.getNumItems(numUnreadItems));
			feedMap.add(item);
			mCursor.moveToNext();
		}
		mCursor.close();
		return feedMap;
	}
	
	// returns a map containing each feed item meeting the given criteria in the database
	public List<Map<String, String>> getFeedItemMap(final Context context, SQLiteDatabase mDB, String[] columns, String selection, String[] selectionArgs, String subtext) {
		Cursor mCursor = mDB.query(Feeds.FEED_ITEMS_TABLE_NAME, columns, selection, selectionArgs, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
		mCursor.moveToFirst();
		List<Map<String, String>> feedItemMap = new ArrayList<Map<String, String>>();
		DateFormat inDateFormat = Utilities.getDateFormatter();
		DateFormat outDateFormat = Utilities.getDateFormatter(context);
		int numFeedItems = mCursor.getCount();
		Date date = null;
		Feed feed;
		String pubDate;
		for(int i = 0; i < numFeedItems; i++) {
			Map<String, String> item = new HashMap<String, String>(2);
			item.put(Feeds.FEED_ITEM_TITLE, mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_TITLE)));
			if(subtext.equals(Feeds.FEED_ITEM_PUB_DATE)) {
				pubDate = mCursor.getString(mCursor.getColumnIndex(subtext));
				try {
					date = inDateFormat.parse(pubDate);
					item.put(subtext, outDateFormat.format(date));
				}
				catch(ParseException e) {
					item.put(subtext, pubDate);
				}
			}
			else if(subtext.equals(Feeds.FEED_ITEM_FEED_ID)) {
				feed = this.getFeed(mDB, mCursor.getString(mCursor.getColumnIndex(subtext)));
				if(feed != null) {
					item.put(subtext, feed.getName());
				}
				else {
					item.put(subtext, "");
				}
			}
			else {
				item.put(subtext, mCursor.getString(mCursor.getColumnIndex(subtext)));
			}
			feedItemMap.add(item);
			mCursor.moveToNext();
		}
		mCursor.close();
		return feedItemMap;
	}
	
	// loads all unread feed items for a given feed into a ListView
	// returns the number of feed items in the ListView
	public int loadFeedItemsFromDatabase(final Activity activity, final SQLiteDatabase mDB, final Feed feed, boolean showAll) {
		String[] columns = {Feeds._ID, Feeds.FEED_ITEM_TITLE, Feeds.FEED_ITEM_PUB_DATE};
		String selection = Feeds.FEED_ITEM_FEED_ID + "=?";
		String[] selectionArgs;
		if(!showAll) {
			selection += " AND " + Feeds.FEED_ITEM_IS_READ + "=?";
			selectionArgs = new String[] {feed.getId(), "0"};
		}
		else {
			selectionArgs = new String[] {feed.getId()};
		}
		return this.loadFeedItemsFromDatabase(activity, mDB, columns, selection, selectionArgs, Feeds.FEED_ITEM_PUB_DATE, R.id.listViewFeedItems, showAll);
	}
	
	// loads all unread feed items into a ListView
	// returns the number of feed items in the ListView
	public int loadAggregatedFeedItemsFromDatabase(final Activity activity, final SQLiteDatabase mDB) {
		String[] columns = {Feeds._ID, Feeds.FEED_ITEM_TITLE, Feeds.FEED_ITEM_FEED_ID};
		String selection = Feeds.FEED_ITEM_IS_READ + "=?";
		String[] selectionArgs = new String[] {"0"};
		return this.loadFeedItemsFromDatabase(activity, mDB, columns, selection, selectionArgs, Feeds.FEED_ITEM_FEED_ID, R.id.listViewFeedItemsAggregated, false);
	}
	
	// loads feed items with given criteria into the given ListView
	// returns the number of feed items in the ListView
	public int loadFeedItemsFromDatabase(final Activity activity, final SQLiteDatabase mDB, String[] columns, String selection, String[] selectionArgs, String subtext, int listView, final boolean showAll) {
		Cursor mCursor = mDB.query(Feeds.FEED_ITEMS_TABLE_NAME, columns, selection, selectionArgs, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
		int numFeedItems = mCursor.getCount();
		if(numFeedItems > 0) {
			// get feed items
			List<Map<String, String>> feedItemMap = this.getFeedItemMap(activity, mDB, columns, selection, selectionArgs, subtext);
			
			// get feed item id list
			final List<String> feedItemIds = new LinkedList<String>();
			mCursor.moveToFirst();
			for(int i = 0; i < numFeedItems; i++) {
				feedItemIds.add(mCursor.getString(mCursor.getColumnIndex(Feeds._ID)));
				mCursor.moveToNext();
			}
			
			// add feed items to ListView
			SimpleAdapter adapter = new SimpleAdapter(activity, feedItemMap, android.R.layout.simple_list_item_2, new String[] {Feeds.FEED_ITEM_TITLE, subtext}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeedItems = (ListView)activity.findViewById(listView);
			lstFeedItems.setAdapter(adapter);
			
			// create feed item ListView item listener
			lstFeedItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// view selected feed item's activity
					String currentFeedItemId = feedItemIds.get(position);
					Bundle bundle = new Bundle();
					bundle.putString(Feeds._ID, currentFeedItemId);
					Intent intent = new Intent(parent.getContext(), FeedItemActivity.class);
					intent.putExtras(bundle);
					if(showAll) {
						intent.putExtra("showAll", true);
					}
					else {
						intent.putExtra("showAll", false);
					}
					activity.startActivityForResult(intent, 0);
				}
			});
		}
		mCursor.close();
		return numFeedItems;
	}
}
