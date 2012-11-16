package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Utilities {
	public static boolean isValidURL(String sURL) {
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
			httpURLConnection.setRequestMethod("HEAD");
			httpURLConnection.connect();
			
			Map headerfields = httpURLConnection.getHeaderFields();
			Set headers = headerfields.entrySet();
			for(Iterator i = headers.iterator(); i.hasNext();) {
				Map.Entry map = (Map.Entry)i.next();
				Log.i("feed", map.getKey() + " : " + map.getValue());
			}
			
			Log.i("feed", headerfields.get(arg0));
			
			if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
		String dateFormat = preferences.getString("date_format", "yyyy/MM/dd");
		String timeFormat = preferences.getString("time_format", "HH:mm");
		return new SimpleDateFormat(dateFormat + ", " + timeFormat);
	}
	
	public static List<FeedItem> getFeedItems(String sURL) {
		List<FeedItem> feedItems = new LinkedList<FeedItem>();
		try {
			XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
			xmlPullParserFactory.setNamespaceAware(true);
			XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
			URL url = new URL(sURL);
			InputStream inputStream = url.openStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			xmlPullParser.setInput(inputStreamReader);
			
			FeedItem currentItem = null;
			int eventType = -1;
			String tagName = "";
			String title = "", pubDate = "", link = "", description = "";
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
				}
				else if(start && eventType == XmlPullParser.END_TAG) {
					tagName = xmlPullParser.getName();
					if(tagName.equals("item")) {
						currentItem = new FeedItem(title, pubDate, link, description);
						feedItems.add(currentItem);
						title = "";
						pubDate = "";
						link = "";
						description = "";
					}
				}
				eventType = xmlPullParser.next();
			}
			
			if(!title.equals("")) {
				currentItem = new FeedItem(title, pubDate, link, description);
				feedItems.add(currentItem);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return feedItems;
	}
}
