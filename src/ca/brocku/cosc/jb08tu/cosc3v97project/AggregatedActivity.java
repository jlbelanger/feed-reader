package ca.brocku.cosc.jb08tu.cosc3v97project;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;
import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.view.Menu;
import android.widget.ListView;

public class AggregatedActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected Cursor				mCursor		= null;
	protected SQLiteDatabase		mDB			= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aggregated);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
	}
	
	@Override public void onStart() {
		super.onStart();
		displayAggregatedFeedItems();
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_aggregated, menu);
		return true;
	}
	
	@Override protected void onDestroy() {
		super.onDestroy();
		if(mCursor != null) {
			mCursor.close();
		}
		if(mDB != null) {
			mDB.close();
		}
		if(mDatabase != null) {
			mDatabase.close();
		}
	}

	private void displayAggregatedFeedItems() {
		// check for network connection
		if(Utilities.hasNetworkConnection(this.getApplicationContext())) {
			//TODO Utilities.downloadNewFeedItems(this.getApplicationContext(), mDatabase, mDB);
		}
		else {
			Utilities.returnToMain(this);
		}
		
		// load all feed items
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(Feeds.FEED_ITEMS_TABLE_NAME);
		String columns[] = {Feeds._ID, Feeds.FEED_ITEM_TITLE, Feeds.FEED_ITEM_FEED_ID};
		mCursor = queryBuilder.query(mDB, columns, Feeds.FEED_ITEM_IS_READ + "=?", new String[] {"0"}, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
		Utilities.loadFeedItemsFromDatabase(this, mDatabase, mDB, mCursor, R.id.listViewFeedItemsAggregated);
		mCursor.close();
		
		final ListView lstFeedItems = (ListView)findViewById(R.id.listViewFeedItemsAggregated);
		int count = lstFeedItems.getCount();
		setTitle(this.getApplicationContext().getResources().getString(R.string.title_activity_aggregated) + " (" + Utilities.getNumItems(count) + ")");
	}
}
