package ca.brocku.cosc.jb08tu.cosc3v97project;

import android.os.Bundle;
import android.app.Activity;
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
				Utilities.getValidURL(SubscribeActivity.this, mDatabase, mDB, url);
			}
		});
	}
}
