package ca.brocku.cosc.jb08tu.cosc3v97project;

import android.provider.BaseColumns;

public final class FeedDatabase {
	private FeedDatabase() {}
	
	public static final class Feeds implements BaseColumns {
		private Feeds() {}
		
		public static final String	FEEDS_TABLE_NAME	= "tbl_feeds";
		
		public static final String	FEED_NAME			= "name";
		public static final String	FEED_URL			= "url";
		
		public static final String	DEFAULT_SORT_ORDER	= FEED_NAME + " ASC";
	}
}
