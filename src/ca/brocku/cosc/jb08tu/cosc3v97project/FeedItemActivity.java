package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;

public class FeedItemActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			feedItemId	= "";
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_item);
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
		getMenuInflater().inflate(R.menu.activity_feed_item, menu);
		
		// create mark as read option
		MenuItem menuItem = menu.findItem(R.id.menu_mark_as_read);
		menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override public boolean onMenuItemClick(MenuItem item) {
				mDatabase.markFeedItemAsRead(mDB, feedItemId);
				finish();
				return true;
			}
		});
		
		return true;
	}
	
	private void displayActivity() {
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed item
			feedItemId = bundle.getString(Feeds._ID);
			FeedItem feedItem = mDatabase.getFeedItem(mDB, feedItemId);
			
			// get feed
			Feed feed = mDatabase.getFeed(mDB, feedItem.getFeedId());
			
			// set activity title
			setTitle(feed.getName());
			
			// get TextView
			final TextView txtTitle = (TextView)findViewById(R.id.textViewTitle);
			final TextView txtDate = (TextView)findViewById(R.id.textViewDate);
			final WebView txtContent = (WebView)findViewById(R.id.textViewContent);
			
			// set TextView values
			txtTitle.setText(Html.fromHtml("<a href=\"" + feedItem.getLink() + "\">" + feedItem.getTitle() + "</a>"));
			txtTitle.setMovementMethod(LinkMovementMethod.getInstance());
			txtDate.setText(feedItem.getPrettyDate(this.getApplicationContext()));
			String content = feedItem.getContent();
			try {
				// bug fix from http://code.google.com/p/android-rss/issues/detail?id=15
				txtContent.loadData(URLEncoder.encode(content, "utf-8").replaceAll("\\+", " "), "text/html", "utf-8");
			}
			catch(UnsupportedEncodingException e) {
				txtContent.loadData("Error reading description.", "text/html", "utf-8");
			}
			
			// get preferences and this feed item as read
			SharedPreferences preferences = getApplicationContext().getSharedPreferences("preferences", 0);
			boolean autoMarkAsRead = preferences.getBoolean("auto_mark_as_read", false);
			if(autoMarkAsRead) {
				mDatabase.markFeedItemAsRead(mDB, feedItemId);
			}
		}
	}
}
