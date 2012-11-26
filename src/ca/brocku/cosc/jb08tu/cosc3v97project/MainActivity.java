package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.Serializable;
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
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

// TODO 
// gestures

// bug: subscribe hangs, mark as read hangs
// bug: hangs when click on feed sometimes

// bug: unknown host exception: host is unresolved (feeds.feedburner.com) from service
// bug: when click on notification, taken to main activity, doesn't show correct number of items

// move network, database stuff off UI thread
// display some message if not connected to a network?
// update number of items when a new feed item is found
// landscape mode

public class MainActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
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
															adapter.notifyDataSetChanged(); // TODO don't think this works
														}
													};
												};
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		openDatabase();
		// mDB.execSQL("DELETE FROM " + Feeds.FEED_ITEMS_TABLE_NAME + ";");
		runService();
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
		getMenuInflater().inflate(R.menu.activity_main, menu);
		Utilities.setIntentOnMenuItem(menu, R.id.menu_subscribe, new Intent(this, SubscribeActivity.class));
		Utilities.setIntentOnMenuItem(menu, R.id.menu_settings, new Intent(this, SettingsActivity.class));
		Utilities.setIntentOnMenuItem(menu, R.id.menu_aggregated_view, new Intent(this, AggregatedActivity.class));
		return true;
	}
	
	private void runService() {
		// get all feeds
		List<Feed> feeds = mDatabase.getFeedList(mDB);
		
		// get preferences, update interval
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
	
	private void displayActivity() {
		// get feeds
		final List<Map<String, String>> feedMap = mDatabase.getFeedMap(mDB);
		int numFeeds = feedMap.size();
		final Button btnSubscribe = (Button)findViewById(R.id.buttonSubscribe);
		
		if(numFeeds > 0) {
			// hide subscribe button
			btnSubscribe.setVisibility(View.INVISIBLE);
			
			// add feeds to ListView
			adapter = new SimpleAdapter(this, feedMap, android.R.layout.simple_list_item_2, new String[] {Feeds.FEED_NAME, "numItems"}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeeds = (ListView)findViewById(R.id.listViewFeeds);
			lstFeeds.setAdapter(adapter);
			
			// create feed ListView item listener
			lstFeeds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// view selected feed's activity
					String currentFeedId = mDatabase.getFeedId(mDB, position);
					Bundle bundle = new Bundle();
					bundle.putString(Feeds._ID, currentFeedId);
					Intent intent = new Intent(parent.getContext(), FeedActivity.class);
					intent.putExtras(bundle);
					startActivityForResult(intent, 0);
				}
			});
		}
		else {
			// show subscribe button
			btnSubscribe.setText(R.string.menu_subscribe);
			btnSubscribe.setVisibility(View.VISIBLE);
			
			// create subscribe button listener
			btnSubscribe.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					// go to subscribe activity
					Intent intent = new Intent(v.getContext(), SubscribeActivity.class);
					startActivityForResult(intent, 0);
				}
			});
		}
	}
}
