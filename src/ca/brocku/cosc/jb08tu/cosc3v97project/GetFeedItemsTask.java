package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class GetFeedItemsTask extends AsyncTask<String, Void, List<FeedItem>> {
	private Feed				feed;
	private FeedDatabaseHelper	mDatabase;
	private SQLiteDatabase		mDB;
	
	public GetFeedItemsTask(FeedDatabaseHelper fDH, SQLiteDatabase sLD, Feed f) {
		super();
		this.feed = f;
		this.mDatabase = fDH;
		this.mDB = sLD;
	}
	
	// read the XML file and return a list of feed items consisting of the data contained in the XML file
	protected List<FeedItem> doInBackground(String... arg) {
		List<FeedItem> feedItems = new LinkedList<FeedItem>();
		try {
			XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
			xmlPullParserFactory.setNamespaceAware(true);
			XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
			URL url = new URL(this.feed.getURL());
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
						if(mDatabase.doesFeedItemExist(mDB, this.feed.getId(), title, pubDate)) {
							break;
						}
						currentItem = new FeedItem("", this.feed.getId(), title, pubDate, link, description, contentEncoded, false);
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
				if(!mDatabase.doesFeedItemExist(mDB, this.feed.getId(), title, pubDate)) {
					currentItem = new FeedItem("", this.feed.getId(), title, pubDate, link, description, contentEncoded, false);
					feedItems.add(currentItem);
				}
			}
			
			inputStreamReader.close();
			inputStream.close();
		}
		catch(Exception e) {}
		return feedItems;
	}
}
