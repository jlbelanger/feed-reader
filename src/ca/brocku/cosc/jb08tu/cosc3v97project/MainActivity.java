package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.util.List;
import java.util.Map;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

// TODO 
// maybe add a setting, delete feed items older than X days
// displaying images in FeedItemActivity
// back stack

public class MainActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected Cursor				mCursor		= null;
	protected SQLiteDatabase		mDB			= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getReadableDatabase();
		
		// mDB.execSQL("DELETE FROM " + Feeds.FEED_ITEMS_TABLE_NAME + ";");
	}
	
	@Override public void onStart() {
		super.onStart();
		displayFeeds();
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		setIntentOnMenuItem(menu, R.id.menu_subscribe, new Intent(this, SubscribeActivity.class));
		setIntentOnMenuItem(menu, R.id.menu_settings, new Intent(this, SettingsActivity.class));
		return true;
	}
	
	private void setIntentOnMenuItem(Menu menu, int menuId, Intent intent) {
		MenuItem menuItem = menu.findItem(menuId);
		if(menuItem != null) {
			menuItem.setIntent(intent);
		}
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
		mCursor = queryBuilder.query(mDB, columns, null, null, null, null, Feeds.FEEDS_DEFAULT_SORT_ORDER);
		
		if(mCursor.getCount() > 0) {
			// get feed items
			List<Map<String, String>> feedList = Utilities.getFeedList(mDatabase, mDB, mCursor);
			
			// add feed items to ListView
			SimpleAdapter adapter = new SimpleAdapter(this, feedList, android.R.layout.simple_list_item_2, new String[] {"name", "numItems"}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeeds = (ListView)findViewById(R.id.listViewFeeds);
			lstFeeds.setAdapter(adapter);
			
			lstFeeds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Bundle bundle = new Bundle();
					mCursor.moveToPosition(position);
					bundle.putString("id",  mCursor.getString(mCursor.getColumnIndex(Feeds._ID)));
					Intent intent = new Intent(parent.getContext(), FeedActivity.class);
					intent.putExtras(bundle);
					startActivityForResult(intent, 0);
				}
			});
		}
		else {
			final Button btnSubscribe = (Button)findViewById(R.id.buttonRefresh);
			btnSubscribe.setText(R.string.menu_subscribe);
			btnSubscribe.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), SubscribeActivity.class);
					startActivityForResult(intent, 0);
				}
			});
		}
	}
}
