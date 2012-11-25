package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Utilities {
	public static void downloadNewFeedItems(Context context, FeedDatabaseHelper mDatabase, SQLiteDatabase mDB, Feed feed) {
		// get new feed items
		final List<FeedItem> feedItems = UtilitiesXML.getNewFeedItems(context, mDatabase, mDB, feed);
		
		// add new items to database
		mDatabase.addNewFeedItemsToDatabase(feedItems, context, mDB);
	}
	
	public static void sendNotification(Context context, String feedName) {
		NotificationCompat.Builder builder = new Builder(context);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle("FeedMe");
		builder.setContentText("New feed items from " + feedName);
		
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		builder.setContentIntent(pendingIntent);
		builder.setAutoCancel(true);
		
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, builder.build());
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
	
	public static int loadFeedItemsFromDatabase(final Activity activity, final FeedDatabaseHelper mDatabase, final SQLiteDatabase mDB, Cursor mCursor, final Feed feed) {
		int count = mCursor.getCount();
		if(count > 0) {
			// get feed items
			List<Map<String, String>> feedItemsList = mDatabase.getFeedItemMap(activity, mCursor);
			
			// get feed id list
			final List<String> feedIds = new LinkedList<String>();
			mCursor.moveToFirst();
			for(int i = 0; i < count; i++) {
				feedIds.add(mCursor.getString(mCursor.getColumnIndex(Feeds._ID)));
				mCursor.moveToNext();
			}
			
			// add feed items to ListView
			SimpleAdapter adapter = new SimpleAdapter(activity, feedItemsList, android.R.layout.simple_list_item_2, new String[] {"title", "pubDate"}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeedItems = (ListView)activity.findViewById(R.id.listViewFeedItems);
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
	
	public static boolean hasNetworkConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
	
	public static boolean isValidURL(final String sURL) {
		AsyncTask<String, Void, String> result = new NetworkingThread().execute("0", sURL);
		String isValid = "";
		try {
			isValid = result.get();
		}
		catch(InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(isValid.equals("true")) {
			return true;
		}
		return false;
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
