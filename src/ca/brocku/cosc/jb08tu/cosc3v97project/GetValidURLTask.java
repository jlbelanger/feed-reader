package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class GetValidURLTask extends AsyncTask<String, Void, String> {
	private final Activity		activity;
	private FeedDatabaseHelper	mDatabase;
	private SQLiteDatabase		mDB;
	private String				sURL;
	private ProgressDialog		dialog;
	
	public GetValidURLTask(Activity a, FeedDatabaseHelper fDH, SQLiteDatabase sLD, String u) {
		super();
		this.activity = a;
		this.mDatabase = fDH;
		this.mDB = sLD;
		this.sURL = u;
		this.dialog = new ProgressDialog(this.activity);
	}
	
	protected String doInBackground(String... arg) {
		this.getValidURL(this.sURL);
		this.finish();
		return this.sURL;
	}
	
	private void getValidURL(String inURL) {
		try {
			URL url = new URL(inURL);
			HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
			httpURLConnection.setRequestMethod("HEAD");
			httpURLConnection.connect();
			if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				Map<String, List<String>> headerFields = httpURLConnection.getHeaderFields();
				String contentType = headerFields.get("content-type").toString();
				if(contentType.contains("xml")) {
					this.sURL = inURL;
				}
				else {
					String alternateURL = this.getFeedURLFromHTML(inURL);
					if(!alternateURL.equals("")) {
						this.sURL = alternateURL;
						httpURLConnection.disconnect();
						return;
					}
					this.sURL = "";
				}
			}
			httpURLConnection.disconnect();
			return;
		}
		catch(Exception e) {}
		this.sURL = "";
		return;
	}
	
	@Override protected void onPreExecute() {
		super.onPreExecute();
		if(!this.dialog.isShowing()) {
			this.dialog.show();
			this.dialog.setMessage(this.activity.getResources().getString(R.string.message_validating_url));
		}
	}
	
	private void finish() {
		if(this.sURL.equals("")) {
			// not valid; do not allow submit
			if(this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			this.activity.runOnUiThread(new Runnable() {
				@Override public void run() {
					Builder alertDialog = new AlertDialog.Builder(activity);
					alertDialog.setMessage(R.string.message_invalid_rss);
					alertDialog.setPositiveButton(R.string.button_ok, null);
					alertDialog.show();
				}
			});
			this.activity.finish();
			return;
		}
		
		// retrieve feed title from XML file
		String title = Utilities.getFeedTitle(this.sURL);
		
		// update database
		Feed feed = this.mDatabase.addFeed(this.mDB, title, this.sURL);
		
		// retrieve feed items from XML file
		if(Utilities.hasNetworkConnection(this.activity)) {
			Utilities.downloadAndSaveNewFeedItems(this.mDatabase, this.mDB, feed);
		}
		
		// close dialog
		if(this.dialog.isShowing()) {
			this.dialog.dismiss();
		}
		
		// return to main activity
		Intent intent = new Intent(this.activity, MainActivity.class);
		this.activity.startActivityForResult(intent, 0);
		this.activity.finish();
	}
	
	private String getFeedURLFromHTML(String inURL) {
		String line, newURL;
		Pattern pattern;
		Matcher matcher;
		try {
			URL url = new URL(inURL);
			InputStream inputStream = url.openStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader dataInputStream = new BufferedReader(inputStreamReader);
			
			while((line = dataInputStream.readLine()) != null) {
				if(line.contains("type=\"application/rss+xml\"")) {
					pattern = Pattern.compile("<link.*href=\"([^\"]+)\".*>");
					matcher = pattern.matcher(line);
					if(matcher.find()) {
						newURL = matcher.group(1);
						if(!newURL.substring(0, 4).equals("http")) {
							newURL = Utilities.appendURLs(inURL, newURL);
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
}
