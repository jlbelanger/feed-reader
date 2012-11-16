package ca.brocku.cosc.jb08tu.cosc3v97project;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

//TODO check for valid xml files
//show subscribe link if not subscribed to any feeds
//check for network connection

public class MainActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected Cursor				mCursor		= null;
	protected SQLiteDatabase		mDB			= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override public void onStart() {
		super.onStart();
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getReadableDatabase();
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(Feeds.FEEDS_TABLE_NAME);
		
		String columns[] = {Feeds._ID, Feeds.FEED_NAME};
		mCursor = queryBuilder.query(mDB, columns, null, null, null, null, Feeds.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursor);
		
		ListAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, mCursor, new String[] {Feeds.FEED_NAME}, new int[] {android.R.id.text1});
		
		final ListView lstContacts = (ListView)findViewById(R.id.listViewFeeds);
		lstContacts.setAdapter(adapter);
		
		lstContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle bundle = new Bundle();
				bundle.putLong("id", id);
				Intent intent = new Intent(parent.getContext(), FeedActivity.class);
				intent.putExtras(bundle);
				startActivityForResult(intent, 0);
			}
		});
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
}
