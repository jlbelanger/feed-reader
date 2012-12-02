package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.util.ArrayList;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
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
	private static FeedItem			feedItem	= null;
	private static boolean			showAll		= false;
	private static GestureLibrary	gestureLibrary;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_item);
		openDatabase();
		detectGestures();
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
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed item
			feedItemId = bundle.getString(Feeds._ID);
			feedItem = mDatabase.getFeedItem(mDB, feedItemId);
			if(feedItem.isRead()) {
				menu.getItem(0).setEnabled(false);
				
				// create mark as unread option
				MenuItem menuItem = menu.findItem(R.id.menu_mark_as_unread);
				menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override public boolean onMenuItemClick(MenuItem item) {
						mDatabase.markFeedItemAsRead(mDB, feedItemId, false);
						finish();
						return true;
					}
				});
			}
			else {
				menu.getItem(1).setEnabled(false);
				
				// create mark as read option
				MenuItem menuItem = menu.findItem(R.id.menu_mark_as_read);
				menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override public boolean onMenuItemClick(MenuItem item) {
						mDatabase.markFeedItemAsRead(mDB, feedItemId, true);
						finish();
						return true;
					}
				});
			}
		}
		return true;
	}
	
	private void detectGestures() {
		gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if(!gestureLibrary.load()) {
			finish();
		}
		GestureOverlayView gestureOverlayView = (GestureOverlayView)findViewById(R.id.gestures);
		gestureOverlayView.addOnGesturePerformedListener(new OnGesturePerformedListener() {
			public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
				ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
				if(predictions.size() > 0 && predictions.get(0).score > 1.0) {
					String result = predictions.get(0).name;
					if(result.equalsIgnoreCase("next")) {
						FeedItem nextFeedItem = mDatabase.getNextFeedItem(mDB, feedItem, false, showAll);
						viewFeedItem(nextFeedItem, showAll);
					}
					else if(result.equalsIgnoreCase("previous")) {
						FeedItem previousFeedItem = mDatabase.getNextFeedItem(mDB, feedItem, true, showAll);
						viewFeedItem(previousFeedItem, showAll);
					}
					else if(result.equalsIgnoreCase("view")) {
						viewFeedItemOnline(feedItem);
					}
				}
			}
			
			private void viewFeedItem(FeedItem feedItem, boolean showAll) {
				if(feedItem != null) {
					Bundle bundle = new Bundle();
					bundle.putString(Feeds._ID, feedItem.getId());
					Intent intent = new Intent(getApplicationContext(), FeedItemActivity.class);
					intent.putExtras(bundle);
					intent.putExtra("showAll", showAll);
					startActivityForResult(intent, 0);
				}
				finish();
			}
			
			private void viewFeedItemOnline(FeedItem feedItem) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(feedItem.getLink()));
				startActivity(intent);
				finish();
			}
		});
	}
	
	private void displayActivity() {
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed item
			feedItemId = bundle.getString(Feeds._ID);
			feedItem = mDatabase.getFeedItem(mDB, feedItemId);
			showAll = bundle.getBoolean("showAll");
			
			// get feed
			Feed feed = mDatabase.getFeed(mDB, feedItem.getFeedId());
			
			// set activity title
			setTitle(feed.getName());
			
			// get TextView, WebView
			final TextView txtTitle = (TextView)findViewById(R.id.textViewTitle);
			final TextView txtDate = (TextView)findViewById(R.id.textViewDate);
			final WebView txtContent = (WebView)findViewById(R.id.textViewContent);
			
			// set TextView, WebView values
			txtTitle.setText(Html.fromHtml("<a href=\"" + feedItem.getLink() + "\">" + feedItem.getTitle() + "</a>"));
			txtTitle.setMovementMethod(LinkMovementMethod.getInstance());
			txtDate.setText(feedItem.getPrettyDate(this.getApplicationContext()));
			String content = feedItem.getContent();
			txtContent.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
			
			// get preferences; if necessary, set this feed item as read
			SharedPreferences preferences = getApplicationContext().getSharedPreferences("preferences", 0);
			boolean autoMarkAsRead = preferences.getBoolean("auto_mark_as_read", false);
			if(autoMarkAsRead) {
				mDatabase.markFeedItemAsRead(mDB, feedItemId, true);
			}
		}
	}
}
