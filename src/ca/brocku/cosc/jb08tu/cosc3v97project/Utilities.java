package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utilities {
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
	
	public static List<FeedItem> getNewFeedItems(Feed feed) {
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
