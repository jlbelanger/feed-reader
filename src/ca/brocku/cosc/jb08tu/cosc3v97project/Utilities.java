package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Utilities {
	public static void returnToMain(final Activity activity) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		dialogBuilder.setMessage(R.string.message_no_network);
		dialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent intent = new Intent(activity, MainActivity.class);
				activity.startActivityForResult(intent, 0);
				activity.finish();
			}
		});
		dialogBuilder.show();
	}
	
	public static void downloadNewFeedItems(Context context, FeedDatabaseHelper mDatabase, SQLiteDatabase mDB, Feed feed) {
		// get new feed items
		final List<FeedItem> feedItems = UtilitiesXML.getNewFeedItems(context, mDatabase, mDB, feed);
		
		// add new items to database
		mDatabase.addNewFeedItemsToDatabase(feedItems, context, mDB);
	}
	
	public static void sendNotification(Context context, String feedName) {
		NotificationCompat.Builder notificationBuilder = new Builder(context);
		notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
		notificationBuilder.setContentTitle(context.getResources().getString(R.string.app_name));
		notificationBuilder.setContentText("New feed items from " + feedName);
		
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		notificationBuilder.setContentIntent(pendingIntent);
		notificationBuilder.setAutoCancel(true);
		
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, notificationBuilder.build());
	}
	
	public static String getNumItems(int num) {
		String numItems = "" + num;
		numItems += " item";
		if(num != 1) {
			numItems += "s";
		}
		return numItems;
	}
	
	public static String chop(String s) {
		int end = s.length();
		int max = 30;
		if(end > max) {
			end = max;
		}
		return s.substring(0, end);
	}
	
	public static void setIntentOnMenuItem(Menu menu, int menuId, Intent intent) {
		MenuItem menuItem = menu.findItem(menuId);
		if(menuItem != null) {
			menuItem.setIntent(intent);
		}
	}

	public static int loadFeedItemsFromDatabase(final Activity activity, final FeedDatabaseHelper mDatabase, final SQLiteDatabase mDB, Cursor mCursor, int listView, final Feed feed) {
		int count = mCursor.getCount();
		if(count > 0) {
			// get feed items
			List<Map<String, String>> feedItemsList = mDatabase.getFeedItemMap(activity, mDB, mCursor, Feeds.FEED_ITEM_PUB_DATE);
			
			// get feed id list
			final List<String> feedIds = new LinkedList<String>();
			mCursor.moveToFirst();
			for(int i = 0; i < count; i++) {
				feedIds.add(mCursor.getString(mCursor.getColumnIndex(Feeds._ID)));
				mCursor.moveToNext();
			}
			
			// add feed items to ListView
			SimpleAdapter adapter = new SimpleAdapter(activity, feedItemsList, android.R.layout.simple_list_item_2, new String[] {Feeds.FEED_ITEM_TITLE, Feeds.FEED_ITEM_PUB_DATE}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeedItems = (ListView)activity.findViewById(listView);
			lstFeedItems.setAdapter(adapter);
			
			lstFeedItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String currentFeedItemId = feedIds.get(position);
					FeedItem currentFeedItem = mDatabase.getFeedItem(mDB, currentFeedItemId);
					Bundle bundle = new Bundle();
					bundle.putSerializable("feed", feed);
					bundle.putSerializable("feedItem", currentFeedItem);
					Intent intent = new Intent(parent.getContext(), FeedItemActivity.class);
					intent.putExtras(bundle);
					activity.startActivityForResult(intent, 0);
				}
			});
		}
		return count;
	}

	public static int loadFeedItemsFromDatabase(final Activity activity, final FeedDatabaseHelper mDatabase, final SQLiteDatabase mDB, Cursor mCursor, int listView) {
		int count = mCursor.getCount();
		if(count > 0) {
			// get feed items
			List<Map<String, String>> feedItemsList = mDatabase.getFeedItemMap(activity, mDB, mCursor, Feeds.FEED_ITEM_FEED_ID);
			
			// get feed id list
			final List<String> feedIds = new LinkedList<String>();
			mCursor.moveToFirst();
			for(int i = 0; i < count; i++) {
				feedIds.add(mCursor.getString(mCursor.getColumnIndex(Feeds._ID)));
				mCursor.moveToNext();
			}
			
			// add feed items to ListView
			SimpleAdapter adapter = new SimpleAdapter(activity, feedItemsList, android.R.layout.simple_list_item_2, new String[] {Feeds.FEED_ITEM_TITLE, Feeds.FEED_ITEM_FEED_ID}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeedItems = (ListView)activity.findViewById(listView);
			lstFeedItems.setAdapter(adapter);
			
			lstFeedItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String currentFeedItemId = feedIds.get(position);
					FeedItem currentFeedItem = mDatabase.getFeedItem(mDB, currentFeedItemId);
					Bundle bundle = new Bundle();
					Feed feed = mDatabase.getFeed(mDB, currentFeedItem.getFeedId());
					bundle.putSerializable("feed", feed);
					bundle.putSerializable("feedItem", currentFeedItem);
					Intent intent = new Intent(parent.getContext(), FeedItemActivity.class);
					intent.putExtras(bundle);
					activity.startActivityForResult(intent, 0);
				}
			});
		}
		return count;
	}
	
	public static boolean hasNetworkConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
	
	public static String getValidURL(final String url) {
		AsyncTask<String, Void, String> result = new NetworkingThread().execute("0", url);
		try {
			return result.get();
		}
		catch(InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static DateFormat getDateFormatter(Context context) {
		SharedPreferences preferences = context.getSharedPreferences("preferences", 0);
		String dateFormat = preferences.getString("date_format", context.getString(R.string.default_date_format));
		String timeFormat = preferences.getString("time_format", context.getString(R.string.default_time_format));
		return new SimpleDateFormat(dateFormat + ", " + timeFormat);
	}
	
	public static DateFormat getDateFormatter(String format) {
		return new SimpleDateFormat(format);
	}
	
	public static DateFormat getDateFormatter() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
}
