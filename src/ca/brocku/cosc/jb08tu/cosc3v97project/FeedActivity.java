package ca.brocku.cosc.jb08tu.cosc3v97project;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class FeedActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			feedId			= "";
	private static Feed				feed		= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			feedId = "" + bundle.getLong("id");
			feed = mDatabase.getFeed(mDB, feedId);
			setTitle(feed.getName());
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
						Log.i("feed", "unsubscribe from feed id = " + feedId);
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
