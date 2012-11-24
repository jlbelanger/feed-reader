package ca.brocku.cosc.jb08tu.cosc3v97project;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SimpleCursorAdapter;

public class FeedActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected Cursor				mCursor		= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			feedId		= "";
	private static Feed				feed		= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
	}
	
	@Override public void onStart() {
		super.onStart();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed id
			feedId = bundle.getString("id");
			feed = mDatabase.getFeed(mDB, feedId);
			
			if(feed != null) {
				// update interface
				setTitle(feed.getName());
				
				// get new feed items
				if(Utilities.hasNetworkConnection(this)) {
					// read feed items
					final List<FeedItem> feedItems = Utilities.getNewFeedItems(feed);
					
					// add new items to database
					for(FeedItem feedItem : feedItems) {
						if(!mDatabase.doesFeedItemExist(mDB, feedItem.getFeedId(), feedItem.getTitle(), feedItem.getDate(this))) {
							mDatabase.addFeedItem(this, mDB, feedItem);
							Log.i("feed", "add " + feedItem.getTitle() + " to database");
						}
					}
				}
				else {
					Builder dialog = new AlertDialog.Builder(FeedActivity.this);
					dialog.setMessage(R.string.message_no_network);
					dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Intent intent = new Intent(FeedActivity.this, MainActivity.class);
							startActivityForResult(intent, 0);
						}
					});
					dialog.show();
				}
				
				// load feed items
				loadFeedItemsFromDatabase(feed);
			}
			else {
				Log.i("feed", "the feed was null");
			}
		}
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_feed, menu);
		
		// edit feed
		Bundle bundle = this.getIntent().getExtras();
		Intent intent = new Intent(this, EditFeedActivity.class);
		intent.putExtras(bundle);
		setIntentOnMenuItem(menu, R.id.menu_edit_feed, intent);
		
		// delete feed
		MenuItem menuItem = menu.findItem(R.id.menu_unsubscribe);
		menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override public boolean onMenuItemClick(MenuItem item) {
				Builder dialog = new AlertDialog.Builder(FeedActivity.this);
				dialog.setMessage("Unsubscribe from " + feed.getName() + "?");
				dialog.setNegativeButton("Cancel", null);
				dialog.setPositiveButton("Unsubscribe", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mDatabase.deleteFeed(mDB, "" + feedId);
						Intent intent = new Intent(FeedActivity.this, MainActivity.class);
						startActivityForResult(intent, 0);
					}
				});
				dialog.show();
				return true;
			}
		});
		
		return true;
	}
	
	private void setIntentOnMenuItem(Menu menu, int menuId, Intent intent) {
		MenuItem menuItem = menu.findItem(menuId);
		if(menuItem != null) {
			menuItem.setIntent(intent);
		}
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
	
	private int loadFeedItemsFromDatabase(final Feed feed) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(Feeds.FEED_ITEMS_TABLE_NAME);
		
		String columns[] = {Feeds._ID, Feeds.FEED_ITEM_TITLE, Feeds.FEED_ITEM_PUB_DATE};
		mCursor = queryBuilder.query(mDB, columns, Feeds.FEED_ITEM_FEED_ID + "=?", new String[] {feed.getId()}, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
		startManagingCursor(mCursor);
		int count = mCursor.getCount();
		
		if(count > 0) {
			// create feed item map for adapter
			List<Map<String, String>> feedItemsList = new ArrayList<Map<String, String>>();
			mCursor.moveToFirst();
			SimpleDateFormat dateFormatter = Utilities.getDateFormatter(this);
			Date date = null;
			for(int i = 0; i < count; i++) {
				Map<String, String> item = new HashMap<String, String>(2);
				item.put("name", mCursor.getString(1));
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm");
				try {
					date = dateFormat.parse(mCursor.getString(2));
				}
				catch(ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				item.put("date", dateFormatter.format(date));
				feedItemsList.add(item);
				mCursor.moveToNext();
			}
			
			// add feed items to ListView
			SimpleAdapter adapter = new SimpleAdapter(this, feedItemsList, android.R.layout.simple_list_item_2, new String[] {"name", "date"}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeedItems = (ListView)findViewById(R.id.listViewFeedItems);
			lstFeedItems.setAdapter(adapter);
			
			lstFeedItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mCursor.moveToPosition(position);
					String currentFeedItemId = mCursor.getString(mCursor.getColumnIndex(Feeds._ID));
					FeedItem currentFeedItem = mDatabase.getFeedItem(mDB, currentFeedItemId);
					Bundle bundle = new Bundle();
					bundle.putSerializable("feed", feed);
					bundle.putSerializable("feedItem", currentFeedItem);
					Intent intent = new Intent(parent.getContext(), FeedItemActivity.class);
					intent.putExtras(bundle);
					startActivityForResult(intent, 0);
				}
			});
		}
		
		return count;
	}
}
