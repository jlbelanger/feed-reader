package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

// TODO 
// gestures
// full view option for FeedActivity

// bug: subscribe hangs, mark as read hangs
//move network, database stuff off UI thread

// update number of items when a new feed item is found
// maybe add a setting, delete feed items older than X days
// nicer icon
// landscape mode

public class MainActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected Cursor				mCursor		= null;
	protected SQLiteDatabase		mDB			= null;
	private SimpleAdapter			adapter		= null;
	
	private Handler					handler		= new Handler() {
													public void handleMessage(Message message) {
														Feed feed = (Feed)message.obj;
														if(message.arg1 == RESULT_OK && feed.getName() != null) {
															SharedPreferences preferences = getApplicationContext().getSharedPreferences("preferences", 0);
															boolean enableNotifications = preferences.getBoolean("enable_notifications", true);
															if(enableNotifications) {
																Utilities.sendNotification(getApplicationContext(), feed.getName());
															}
															adapter.notifyDataSetChanged(); // TODO don't know if this works
														}
													};
												};
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getReadableDatabase();

		// mDB.execSQL("DELETE FROM " + Feeds.FEED_ITEMS_TABLE_NAME + ";");
		
		runService();
	}
	
	@Override public void onStart() {
		super.onStart();
		displayFeeds();
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		Utilities.setIntentOnMenuItem(menu, R.id.menu_subscribe, new Intent(this, SubscribeActivity.class));
		Utilities.setIntentOnMenuItem(menu, R.id.menu_settings, new Intent(this, SettingsActivity.class));
		Utilities.setIntentOnMenuItem(menu, R.id.menu_aggregated_view, new Intent(this, AggregatedActivity.class));
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
	
	private void runService() {
		// get all feeds
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(Feeds.FEEDS_TABLE_NAME);
		String columns[] = {Feeds._ID};
		mCursor = queryBuilder.query(mDB, columns, null, null, null, null, Feeds.FEEDS_DEFAULT_SORT_ORDER);
		List<Feed> feeds = new LinkedList<Feed>();
		mCursor.moveToFirst();
		int count = mCursor.getCount();
		for(int i = 0; i < count; i++) {
			feeds.add(mDatabase.getFeed(mDB, mCursor.getString(mCursor.getColumnIndex(Feeds._ID))));
			mCursor.moveToNext();
		}
		mCursor.close();
		
		// get preferences
		Context context = this.getApplicationContext();
		SharedPreferences preferences = context.getSharedPreferences("preferences", 0);
		int updateInterval = Integer.parseInt(preferences.getString("update_interval", context.getString(R.string.default_update_interval)));
		
		// run service
		Intent intent = new Intent(this, FeedService.class);
		Messenger messenger = new Messenger(handler);
		intent.putExtra("messenger", messenger);
		intent.putExtra("feeds", (Serializable)feeds);
		intent.putExtra("updateInterval", updateInterval);
		startService(intent);
	}
	
	private void displayFeeds() {
		final TextView txtNoNetwork = (TextView)findViewById(R.id.textViewNetworkConnection);
		final Button btnRefresh = (Button)findViewById(R.id.buttonRefresh);
		
		if(Utilities.hasNetworkConnection(this)) {
			txtNoNetwork.setVisibility(View.INVISIBLE);
			btnRefresh.setVisibility(View.INVISIBLE);
			
			loadFeedsFromDatabase();
		}
		else {
			txtNoNetwork.setVisibility(View.VISIBLE);
			btnRefresh.setVisibility(View.VISIBLE);
			btnRefresh.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					displayFeeds();
				}
			});
		}
	}
	
	private void loadFeedsFromDatabase() {
		// query database
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(Feeds.FEEDS_TABLE_NAME);
		String columns[] = {Feeds._ID, Feeds.FEED_NAME};
		mCursor.close();
		mCursor = queryBuilder.query(mDB, columns, null, null, null, null, Feeds.FEEDS_DEFAULT_SORT_ORDER);
		
		final Button btnSubscribe = (Button)findViewById(R.id.buttonRefresh);
		if(mCursor.getCount() > 0) {
			btnSubscribe.setVisibility(View.INVISIBLE);
			
			// get feeds
			final List<Map<String, String>> feedList = mDatabase.getFeedMap(mDB, mCursor);
			
			// add feed to ListView
			adapter = new SimpleAdapter(this, feedList, android.R.layout.simple_list_item_2, new String[] {Feeds.FEED_NAME, "numItems"}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeeds = (ListView)findViewById(R.id.listViewFeeds);
			lstFeeds.setAdapter(adapter);
			
			lstFeeds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Bundle bundle = new Bundle();
					mCursor.moveToPosition(position);
					bundle.putString(Feeds._ID, mCursor.getString(mCursor.getColumnIndex(Feeds._ID)));
					bundle.putString("numItems", feedList.get(position).values().toArray()[0].toString());
					Intent intent = new Intent(parent.getContext(), FeedActivity.class);
					intent.putExtras(bundle);
					startActivityForResult(intent, 0);
				}
			});
		}
		else {
			btnSubscribe.setText(R.string.menu_subscribe);
			btnSubscribe.setVisibility(View.VISIBLE);
			btnSubscribe.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), SubscribeActivity.class);
					startActivityForResult(intent, 0);
				}
			});
		}
	}
}
