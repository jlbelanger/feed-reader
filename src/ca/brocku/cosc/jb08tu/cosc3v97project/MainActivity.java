package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.util.ArrayList;
import java.util.HashMap;
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
import android.widget.TableLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected Cursor				mCursor		= null;
	protected SQLiteDatabase		mDB			= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getReadableDatabase();
		
		//mDB.execSQL("DELETE FROM " + Feeds.FEED_ITEMS_TABLE_NAME + ";");
	}
	
	@Override public void onStart() {
		super.onStart();
		checkForNetworkConnection();
	}
	
	private void checkForNetworkConnection() {
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
					checkForNetworkConnection();
				}
			});
		}
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
		if(mDB != null) {
			mDB.close();
		}
		if(mDatabase != null) {
			mDatabase.close();
		}
	}
	
	private void loadFeedsFromDatabase() {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(Feeds.FEEDS_TABLE_NAME);
		
		String columns[] = {Feeds._ID, Feeds.FEED_NAME};
		mCursor = queryBuilder.query(mDB, columns, null, null, null, null, Feeds.FEEDS_DEFAULT_SORT_ORDER);
		startManagingCursor(mCursor);
		
		if(mCursor.getCount() > 0) {
			// create feed item map for adapter
			List<Map<String, String>> feedList = new ArrayList<Map<String, String>>();
			mCursor.moveToFirst();
			int count = mCursor.getCount();
			String[] numItems = new String[count];
			int num = 0;
			for(int i = 0; i < count; i++) {
				num = mDatabase.getNumUnreadFeedItems(mDB, mCursor.getString(0));
				numItems[i] = num + " item";
				if(num != 1) {
					numItems[i] = numItems[i] + "s";
				}
				Map<String, String> item = new HashMap<String, String>(2);
				item.put("title", mCursor.getString(1));
				item.put("numItems", numItems[i]);
				feedList.add(item);
				mCursor.moveToNext();
			}
			
			// add feed items to ListView
			SimpleAdapter adapter = new SimpleAdapter(this, feedList, android.R.layout.simple_list_item_2, new String[] {"title", "numItems"}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeeds = (ListView)findViewById(R.id.listViewFeeds);
			lstFeeds.setAdapter(adapter);
			
			lstFeeds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Bundle bundle = new Bundle();
					mCursor.moveToPosition(position);
					bundle.putString("id", mCursor.getString(0));
					Intent intent = new Intent(parent.getContext(), FeedActivity.class);
					intent.putExtras(bundle);
					startActivityForResult(intent, 0);
				}
			});
		}
		else {
			Button btnSubscribe = new Button(this);
			btnSubscribe.setText(R.string.menu_subscribe);
			TableLayout layout = (TableLayout)findViewById(R.id.layoutMain);
			layout.addView(btnSubscribe);
			
			btnSubscribe.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), SubscribeActivity.class);
					startActivityForResult(intent, 0);
				}
			});
		}
	}
}
