package ca.brocku.cosc.jb08tu.cosc3v97project;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;
import android.os.Bundle;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditFeedActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			feedId		= "";
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_feed);
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
		getMenuInflater().inflate(R.menu.activity_edit_feed, menu);
		return true;
	}
	
	private void displayActivity() {
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed
			feedId = bundle.getString(Feeds._ID);
			Feed feed = mDatabase.getFeed(mDB, feedId);
			
			// set activity title
			setTitle("Edit " + feed.getName());
			
			// get EditText
			final EditText txtName = (EditText)findViewById(R.id.editTextName);
			final EditText txtURL = (EditText)findViewById(R.id.editTextURL);
			
			// set EditText values
			txtName.setText(feed.getName());
			txtURL.setText(feed.getURL());
			
			// create submit button listener
			final Button btnEditFeed = (Button)findViewById(R.id.buttonEditFeed);
			btnEditFeed.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// get EditText values
					String name = txtName.getText().toString();
					String url = txtURL.getText().toString();
					
					// update database
					mDatabase.editFeed(mDB, feedId, name, url);
					
					// return to feed activity
					finish();
				}
			});
		}
	}
}
