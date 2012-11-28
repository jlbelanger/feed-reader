package ca.brocku.cosc.jb08tu.cosc3v97project;

import android.provider.BaseColumns;

public final class FeedDatabase {
	private FeedDatabase() {}
	
	public static final class Feeds implements BaseColumns {
		private Feeds() {}
		
		public static final String	FEEDS_TABLE_NAME				= "tbl_feeds";
		public static final String	FEED_ITEMS_TABLE_NAME			= "tbl_feed_items";
		
		public static final String	FEED_NAME						= "name";
		public static final String	FEED_URL						= "url";
		
		public static final String	FEED_ITEM_FEED_ID				= "feedId";
		public static final String	FEED_ITEM_TITLE					= "title";
		public static final String	FEED_ITEM_PUB_DATE				= "pubDate";
		public static final String	FEED_ITEM_LINK					= "link";
		public static final String	FEED_ITEM_DESCRIPTION			= "description";
		public static final String	FEED_ITEM_CONTENT_ENCODED		= "contentEncoded";
		public static final String	FEED_ITEM_IS_READ				= "isRead";
		
		public static final String	FEEDS_DEFAULT_SORT_ORDER		= FEED_NAME + " ASC";
		public static final String	FEED_ITEMS_DEFAULT_SORT_ORDER	= Feeds._ID + " ASC, " + FEED_ITEM_PUB_DATE + " DESC";
	}
}
