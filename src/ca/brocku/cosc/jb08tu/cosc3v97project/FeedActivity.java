package ca.brocku.cosc.jb08tu.cosc3v97project;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class FeedActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			feedId		= "";
	private static Feed				feed		= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
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
		getMenuInflater().inflate(R.menu.activity_feed, menu);
		
		// create edit feed option
		Bundle bundle = this.getIntent().getExtras();
		Intent intent = new Intent(this, EditFeedActivity.class);
		intent.putExtras(bundle);
		Utilities.setIntentOnMenuItem(menu, R.id.menu_edit_feed, intent);
		
		// create unsubscribe option
		MenuItem menuItemUnsubscribe = menu.findItem(R.id.menu_unsubscribe);
		menuItemUnsubscribe.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override public boolean onMenuItemClick(MenuItem item) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(FeedActivity.this);
				dialog.setMessage(getResources().getString(R.string.message_unsubscribe) + feed.getName() + "?");
				dialog.setNegativeButton(R.string.button_cancel, null);
				dialog.setPositiveButton(R.string.button_unsubscribe, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mDatabase.deleteFeed(mDB, feedId);
						finish();
					}
				});
				dialog.show();
				return true;
			}
		});
		
		// create mark all as read option
		MenuItem menuItemMarkAllAsRead = menu.findItem(R.id.menu_mark_all_as_read);
		menuItemMarkAllAsRead.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override public boolean onMenuItemClick(MenuItem item) {
				mDatabase.markAllFeedsItemsAsRead(mDB, feedId);
				finish();
				return true;
			}
		});
		
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
			mDatabase.loadFeedItemsFromDatabase(this, mDB, feed);
			
			// set activity title
			final ListView lstFeedItems = (ListView)findViewById(R.id.listViewFeedItems);
			int numFeedItems = lstFeedItems.getCount();
			setTitle(feed.getName() + " (" + Utilities.getNumItems(numFeedItems) + ")");
		}
	}
}
