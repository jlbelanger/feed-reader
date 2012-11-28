package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.Menu;
import android.view.MenuItem;

public class Utilities {
	public static List<FeedItem> getNewFeedItems(FeedDatabaseHelper mDatabase, SQLiteDatabase mDB, Feed feed) {
		AsyncTask<String, Void, List<FeedItem>> task = new GetFeedItemsTask(mDatabase, mDB, feed);
		task.execute();
		try {
			return task.get();
		}
		catch(Exception e) {}
		return null;
	}
	
	public static void getValidURL(SubscribeActivity activity, FeedDatabaseHelper mDatabase, SQLiteDatabase mDB, String url) {
		AsyncTask<String, Void, String> task = new GetValidURLTask(activity, mDatabase, mDB, url);
		task.execute();
	}
	
	public static String getFeedTitle(String url) {
		AsyncTask<String, Void, String> task = new GetFeedTitleTask(url);
		task.execute();
		try {
			return task.get();
		}
		catch(Exception e) {}
		return "";
	}
	
	public static void downloadAndSaveNewFeedItems(FeedDatabaseHelper mDatabase, SQLiteDatabase mDB, Feed feed) {
		// get new feed items
		final List<FeedItem> feedItems = getNewFeedItems(mDatabase, mDB, feed);
		
		// add new items to database
		mDatabase.addNewFeedItemsToDatabase(mDB, feedItems);
	}
	
	public static void sendNotification(Context context, String feedName) {
		NotificationCompat.Builder notificationBuilder = new Builder(context);
		notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
		notificationBuilder.setContentTitle(context.getResources().getString(R.string.app_name));
		notificationBuilder.setContentText(context.getResources().getString(R.string.notification_body) + feedName);
		
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
	
	public static String appendURLs(String base, String ext) {
		if(ext.substring(0, 1).equals("/")) {
			ext = ext.substring(1, ext.length());
		}
		if(base.substring(base.length() - 1, base.length()).equals("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/" + ext;
	}
	
	public static boolean hasNetworkConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
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
	
	public static DateFormat getDateFormatter() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
}
