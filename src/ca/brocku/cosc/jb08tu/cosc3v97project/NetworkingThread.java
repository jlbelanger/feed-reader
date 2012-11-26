package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.AsyncTask;

public class NetworkingThread extends AsyncTask<String, Void, String> {
	@Override protected String doInBackground(String... arg) {
		if(arg[0].equals("0")) {
			return getValidURL(arg[1]);
		}
		else if(arg[0].equals("1")) {
			return getFeedTitle(arg[1]);
		}
		return "";
	}
	
	private static String getFeedURLFromHTML(String sURL) {
		String line, newURL;
		Pattern pattern;
		Matcher matcher;
		try {
			URL url = new URL(sURL);
			InputStream inputStream = url.openStream();
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			while((line = dataInputStream.readLine()) != null) {
				if(line.contains("type=\"application/rss+xml\"")) {
					pattern = Pattern.compile("<link.*href=\"([^\"]+)\".*>");
					matcher = pattern.matcher(line);
					if(matcher.find()) {
						newURL = matcher.group(1);
						if(!newURL.substring(0, 4).equals("http")) {
							newURL = Utilities.appendURLs(sURL, newURL);
						}
						return newURL;
					}
				}
			}
		}
		catch(MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	private static String getValidURL(String sURL) {
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
			httpURLConnection.setRequestMethod("HEAD");
			httpURLConnection.connect();
			if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				Map<String, List<String>> headerFields = httpURLConnection.getHeaderFields();
				String contentType = headerFields.get("content-type").toString();
				if(contentType.contains("text/xml")) {
					return sURL;
				}
				else {
					String alternateURL = getFeedURLFromHTML(sURL);
					if(!alternateURL.equals("")) {
						return getValidURL(alternateURL);
					}
					return "";
				}
			}
		}
		catch(MalformedURLException e) {}
		catch(ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	private static String getFeedTitle(String sURL) {
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
		return sURL;
	}
}
