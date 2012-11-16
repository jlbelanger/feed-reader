package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v4.app.NavUtils;

public class FeedActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			feedId		= "";
	private static Feed				feed		= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
	}
	
	@Override public void onStart() {
		super.onStart();
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed id
			feedId = "" + bundle.getLong("id");
			feed = mDatabase.getFeed(mDB, feedId);
			
			// update interface
			setTitle(feed.getName());
			
			// read feed items
			final List<FeedItem> feedItems = Utilities.getFeedItems(feed.getURL());
			
			// create feed item map for adapter
			List<Map<String, String>> feedItemsList = new ArrayList<Map<String, String>>();
			int numItems = feedItems.size();
			for(int i = 0; i < numItems; i++) {
				Map<String, String> item = new HashMap<String, String>(2);
				item.put("title", feedItems.get(i).getTitle());
				item.put("pubDate", feedItems.get(i).getPubDate(this));
				feedItemsList.add(item);
			}
			
			// add feed items to ListView
			SimpleAdapter adapter = new SimpleAdapter(this, feedItemsList, android.R.layout.simple_list_item_2, new String[] {"title", "pubDate"}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeedItems = (ListView)findViewById(R.id.listViewFeedItems);
			lstFeedItems.setAdapter(adapter);
			
			lstFeedItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					FeedItem currentFeedItem = feedItems.get(position);
					Bundle bundle = new Bundle();
					bundle.putSerializable("feedItem", currentFeedItem);
					Intent intent = new Intent(parent.getContext(), FeedItemActivity.class);
					intent.putExtras(bundle);
					startActivityForResult(intent, 0);
				}
			});
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
		if(mDB != null) {
			mDB.close();
		}
		if(mDatabase != null) {
			mDatabase.close();
		}
	}
}
