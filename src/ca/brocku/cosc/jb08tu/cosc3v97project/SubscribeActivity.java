package ca.brocku.cosc.jb08tu.cosc3v97project;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SubscribeActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subscribe);
		openDatabase();
	}
	
	@Override public void onStart() {
		super.onStart();
		openDatabase();
		displayActivity();
	}
	
	@Override public void onPause() {
		super.onPause();
		closeDatabase();
	}
	
	@Override protected void onDestroy() {
		super.onDestroy();
		closeDatabase();
	}
	
	private void openDatabase() {
		if(mDatabase == null || mDB == null || !mDB.isOpen()) {
			mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
			mDB = mDatabase.getReadableDatabase();
		}
	}
	
	private void closeDatabase() {
		if(mDB != null) {
			mDB.close();
		}
		if(mDatabase != null) {
			mDatabase.close();
		}
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_subscribe, menu);
		return true;
	}
	
	private void displayActivity() {
		// create submit button listener
		final Button btnSubscribe = (Button)findViewById(R.id.buttonSubscribe);
		btnSubscribe.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// get EditText
				final EditText txtURL = (EditText)findViewById(R.id.editTextURL);
				
				// get EditText value
				String url = txtURL.getText().toString().trim();
				if(!url.equals("") && !url.startsWith("http")) {
					url = "http://" + url;
				}
				
				// check for valid URL
				url = Utilities.getValidURL(url);
				if(url.equals("")) {
					// not valid; do not allow submit
					Builder dialog = new AlertDialog.Builder(SubscribeActivity.this);
					dialog.setMessage(R.string.message_invalid_rss);
					dialog.setPositiveButton(R.string.button_ok, null);
					dialog.show();
					return;
				}
				
				// retrieve feed title from XML file
				String title = UtilitiesXML.getFeedTitle(url);
				
				// update database
				Feed feed = mDatabase.addFeed(mDB, title, url);
				
				// retrieve feed items from XML file
				if(Utilities.hasNetworkConnection(getApplicationContext())) {
					Utilities.downloadNewFeedItems(getApplicationContext(), mDatabase, mDB, feed);
				}
				
				// return to main activity
				Intent intent = new Intent(SubscribeActivity.this, MainActivity.class);
				startActivityForResult(intent, 0);
				finish();
			}
		});
	}
}
