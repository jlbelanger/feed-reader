package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

public class UtilitiesXML {
	public static String getFeedTitle(String sURL) {
		AsyncTask<String, Void, String> result = new NetworkingThread().execute("1", sURL);
		String title = "";
		try {
			title = result.get();
		}
		catch(InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return title;
	}
	
	public static List<FeedItem> getNewFeedItems(Context context, FeedDatabaseHelper mDatabase, SQLiteDatabase mDB, Feed feed) {
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
			DateFormat outDateFormat = Utilities.getDateFormatter();
			Date date = null;
			
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
						date = new Date(pubDate);
						pubDate = outDateFormat.format(date);
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
						Log.i("feed", "...new item " + Utilities.chop(title) + " / " + pubDate);
						currentItem = new FeedItem("", feed.getId(), title, pubDate, link, description, contentEncoded, false);
						feedItems.add(currentItem);
						title = "";
						pubDate = "";
						date = null;
						link = "";
						description = "";
						contentEncoded = "";
					}
				}
				eventType = xmlPullParser.next();
			}
			
			if(!title.equals("")) {
				if(!mDatabase.doesFeedItemExist(mDB, feed.getId(), title, pubDate)) {
					Log.i("feed", "...new item " + Utilities.chop(title) + " / " + pubDate);
					currentItem = new FeedItem("", feed.getId(), title, pubDate, link, description, contentEncoded, false);
					feedItems.add(currentItem);
				}
			}
		}
		catch(XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return feedItems;
	}
}
