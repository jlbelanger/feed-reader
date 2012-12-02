package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.AsyncTask;

public class GetFeedTitleTask extends AsyncTask<String, Void, String> {
	private String	sURL;
	
	public GetFeedTitleTask(String u) {
		super();
		this.sURL = u;
	}
	
	// read the XML file and look for the title of the feed
	protected String doInBackground(String... arg) {
		try {
			XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
			xmlPullParserFactory.setNamespaceAware(true);
			XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
			URL url = new URL(this.sURL);
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
			
			inputStreamReader.close();
			inputStream.close();
		}
		catch(Exception e) {}
		return this.sURL;
	}
}
