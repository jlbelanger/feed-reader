package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.widget.ListView;

public class AggregatedActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aggregated);
		openDatabase();
	}
	
	@Override public void onStart() {
		super.onStart();
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
		getMenuInflater().inflate(R.menu.activity_aggregated, menu);
		return true;
	}
	
	private void displayActivity() {
		// check for network connection
		if(Utilities.hasNetworkConnection(this.getApplicationContext())) {
			// get all feeds
			List<Feed> feeds = mDatabase.getFeedList(mDB);
			for(Feed feed : feeds) {
				// get new feed items
				final List<FeedItem> feedItems = Utilities.getNewFeedItems(mDatabase, mDB, feed);
				
				// add new feed items to database
				mDatabase.addNewFeedItemsToDatabase(mDB, feedItems);
			}
		}
		
		// load all feed items into ListView
		mDatabase.loadAggregatedFeedItemsFromDatabase(this, mDB);
		
		// set activity title
		final ListView lstFeedItems = (ListView)findViewById(R.id.listViewFeedItemsAggregated);
		int numFeedItems = lstFeedItems.getCount();
		setTitle(this.getApplicationContext().getResources().getString(R.string.title_activity_aggregated) + " (" + Utilities.getNumItems(numFeedItems) + ")");
	}
}
