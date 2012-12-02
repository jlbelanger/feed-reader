package ca.brocku.cosc.jb08tu.cosc3v97project;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.widget.ListView;

public class FeedAllActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			feedId		= "";
	private static Feed				feed		= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_all);
		openDatabase();
	}
	
	@Override public void onStart() {
		super.onStart();
		openDatabase();
	}
	
	@Override public void onResume() {
		super.onResume();
		openDatabase();
		displayActivity();
	}
	
	@Override public void onPause() {
		super.onPause();
		closeDatabase();
	}
	
	@Override protected void onDestroy() {
		super.onDestroy();
		closeDatabase();
	}
	
	private void openDatabase() {
		if(mDatabase == null || mDB == null || !mDB.isOpen()) {
			mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
			mDB = mDatabase.getReadableDatabase();
		}
	}
	
	private void closeDatabase() {
		if(mDB != null) {
			mDB.close();
		}
		if(mDatabase != null) {
			mDatabase.close();
		}
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_feed_all, menu);
		
		// create view unread only option
		Bundle bundle = this.getIntent().getExtras();
		Intent intent = new Intent(this, FeedActivity.class);
		intent.putExtras(bundle);
		Utilities.setIntentOnMenuItem(menu, R.id.menu_view_unread_only, intent);
		
		return true;
	}
	
	private void displayActivity() {
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed
			feedId = bundle.getString(Feeds._ID);
			feed = mDatabase.getFeed(mDB, feedId);
			
			// check for network connection
			if(Utilities.hasNetworkConnection(this.getApplicationContext())) {
				// get new feed items for this feed
				Utilities.downloadAndSaveNewFeedItems(mDatabase, mDB, feed);
			}
			
			// load all feed items into ListView
			mDatabase.loadFeedItemsFromDatabase(this, mDB, feed, true);
			
			// set activity title
			final ListView lstFeedItems = (ListView)findViewById(R.id.listViewFeedItems);
			int numFeedItems = lstFeedItems.getCount();
			setTitle(feed.getName() + " (" + Utilities.getNumItems(numFeedItems) + " total)");
		}
	}
}
