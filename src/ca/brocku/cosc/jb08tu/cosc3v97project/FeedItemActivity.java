package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;

public class FeedItemActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			feedId		= "";
	private static String			feedItemId	= "";
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_item);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
	}
	
	@Override public void onStart() {
		super.onStart();
		displayFeedItem();
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_feed_item, menu);
		
		// mark as read
		MenuItem menuItem = menu.findItem(R.id.menu_mark_as_read);
		menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override public boolean onMenuItemClick(MenuItem item) {
				mDatabase.markFeedItemAsRead(mDB, feedItemId);
				
				Bundle bundle = new Bundle();
				bundle.putString("id", feedId);
				Intent intent = new Intent(FeedItemActivity.this, FeedActivity.class);
				intent.putExtras(bundle);
				startActivityForResult(intent, 0);
				
				return true;
			}
		});
		
		return true;
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
	
	private void displayFeedItem() {
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed
			Feed feed = (Feed)bundle.getSerializable("feed");
			feedId = feed.getId();
			
			// get feed item
			FeedItem feedItem = (FeedItem)bundle.getSerializable("feedItem");
			feedItemId = feedItem.getId();
			
			// update interface
			setTitle(feed.getName());
			
			// get TextView
			final TextView txtTitle = (TextView)findViewById(R.id.textViewTitle);
			final TextView txtDate = (TextView)findViewById(R.id.textViewDate);
			final WebView txtContent = (WebView)findViewById(R.id.textViewContent);
			
			// set TextView
			txtTitle.setText(Html.fromHtml("<a href=\"" + feedItem.getLink() + "\">" + feedItem.getTitle() + "</a>"));
			txtTitle.setMovementMethod(LinkMovementMethod.getInstance());
			txtDate.setText(feedItem.getPrettyDate(this.getApplicationContext()));
			String content = feedItem.getContent();
			try {
				// bug fix from http://code.google.com/p/android-rss/issues/detail?id=15
				txtContent.loadData(URLEncoder.encode(content, "utf-8").replaceAll("\\+", " "), "text/html", "utf-8");
			}
			catch(UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
