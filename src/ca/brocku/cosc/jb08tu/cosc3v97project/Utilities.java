package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Utilities {
	public static List<Map<String, String>> getFeedList(FeedDatabaseHelper mDatabase, SQLiteDatabase mDB, Cursor mCursor) {
		List<Map<String, String>> feedList = new ArrayList<Map<String, String>>();
		mCursor.moveToFirst();
		int count = mCursor.getCount();
		String[] numItems = new String[count];
		int num = 0;
		for(int i = 0; i < count; i++) {
			num = mDatabase.getNumUnreadFeedItems(mDB, mCursor.getString(mCursor.getColumnIndex(Feeds._ID)));
			numItems[i] = num + " item";
			if(num != 1) {
				numItems[i] = numItems[i] + "s";
			}
			Map<String, String> item = new HashMap<String, String>(2);
			item.put("name", mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_NAME)));
			item.put("numItems", numItems[i]);
			feedList.add(item);
			mCursor.moveToNext();
		}
		return feedList;
	}
	
	public static int loadFeedItemsFromDatabase(final Activity activity, final FeedDatabaseHelper mDatabase, final SQLiteDatabase mDB, final Cursor mCursor, final Feed feed) {
		int count = mCursor.getCount();
		if(count > 0) {
			// create feed item map for adapter
			List<Map<String, String>> feedItemsList = new ArrayList<Map<String, String>>();
			mCursor.moveToFirst();
			SimpleDateFormat dateFormatter = Utilities.getDateFormatter(activity);
			Date date = null;
			for(int i = 0; i < count; i++) {
				Map<String, String> item = new HashMap<String, String>(2);
				item.put("name", mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_TITLE)));
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm");
				try {
					date = dateFormat.parse(mCursor.getString(mCursor.getColumnIndex(Feeds.FEED_ITEM_PUB_DATE)));
				}
				catch(ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				item.put("date", dateFormatter.format(date));
				feedItemsList.add(item);
				mCursor.moveToNext();
			}
			
			// add feed items to ListView
			SimpleAdapter adapter = new SimpleAdapter(activity, feedItemsList, android.R.layout.simple_list_item_2, new String[] {"name", "date"}, new int[] {android.R.id.text1, android.R.id.text2});
			final ListView lstFeedItems = (ListView)activity.findViewById(R.id.listViewFeedItems);
			lstFeedItems.setAdapter(adapter);
			
			lstFeedItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mCursor.moveToPosition(position);
					String currentFeedItemId = mCursor.getString(mCursor.getColumnIndex(Feeds._ID));
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
	
	public static boolean isValidURL(String sURL) {
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
			httpURLConnection.setRequestMethod("HEAD");
			httpURLConnection.connect();
			
			if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				Map<String, List<String>> headerfields = httpURLConnection.getHeaderFields();
				String contentType = headerfields.get("content-type").toString();
				if(!contentType.contains("text/xml")) {
					return false;
				}
				return true;
			}
		}
		catch(MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public static String getFeedTitle(String sURL) {
		try {
			XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
			xmlPullParserFactory.setNamespaceAware(true);
			XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
			URL url = new URL(sURL);
			InputStream inputStream = url.openStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			xmlPullParser.setInput(inputStreamReader);
			int eventType = -1;
			String tagName = "";
			
			while(eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG) {
					tagName = xmlPullParser.getName();
				}
				else if(eventType == XmlPullParser.TEXT) {
					if(tagName.equals("title")) {
						return xmlPullParser.getText();
					}
				}
				eventType = xmlPullParser.next();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static SimpleDateFormat getDateFormatter(Context context) {
		SharedPreferences preferences = context.getSharedPreferences("preferences", 0);
		String dateFormat = preferences.getString("date_format", context.getString(R.string.default_date));
		String timeFormat = preferences.getString("time_format", context.getString(R.string.default_time));
		return new SimpleDateFormat(dateFormat + ", " + timeFormat);
	}
	
	public static SimpleDateFormat getDefaultDateFormatter(Context context) {
		String dateFormat = context.getString(R.string.default_date);
		String timeFormat = context.getString(R.string.default_time);
		return new SimpleDateFormat(dateFormat + ", " + timeFormat);
	}
	
	public static List<FeedItem> getNewFeedItems(FeedDatabaseHelper mDatabase, SQLiteDatabase mDB, Feed feed) {
		List<FeedItem> feedItems = new LinkedList<FeedItem>();
		try {
			XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
			xmlPullParserFactory.setNamespaceAware(true);
			XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
			URL url = new URL(feed.getURL());
			InputStream inputStream = url.openStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			xmlPullParser.setInput(inputStreamReader);
			
			FeedItem currentItem = null;
			int eventType = -1;
			String tagName = "";
			String title = "", pubDate = "", link = "", description = "", contentEncoded = "";
			boolean start = false;
			
			while(eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG) {
					tagName = xmlPullParser.getName();
					if(tagName.equals("item")) {
						start = true;
					}
				}
				else if(start && eventType == XmlPullParser.TEXT) {
					if(tagName.equals("title") && title.equals("")) {
						title = xmlPullParser.getText();
					}
					else if(tagName.equals("pubDate") && pubDate.equals("")) {
						pubDate = xmlPullParser.getText();
					}
					else if(tagName.equals("link") && link.equals("")) {
						link = xmlPullParser.getText();
					}
					else if(tagName.equals("description") && description.equals("")) {
						description = xmlPullParser.getText();
					}
					else if(tagName.equals("encoded") && contentEncoded.equals("")) {
						contentEncoded = xmlPullParser.getText();
					}
				}
				else if(start && eventType == XmlPullParser.END_TAG) {
					tagName = xmlPullParser.getName();
					if(tagName.equals("item")) {
						if(mDatabase.doesFeedItemExist(mDB, feed.getId(), title, pubDate)) {
							break;
						}
						currentItem = new FeedItem("", feed.getId(), title, pubDate, link, description, contentEncoded);
						feedItems.add(currentItem);
						title = "";
						pubDate = "";
						link = "";
						description = "";
						contentEncoded = "";
					}
				}
				eventType = xmlPullParser.next();
			}
			
			if(!title.equals("")) {
				currentItem = new FeedItem("", feed.getId(), title, pubDate, link, description, contentEncoded);
				feedItems.add(currentItem);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return feedItems;
	}
}
