package ca.brocku.cosc.jb08tu.cosc3v97project;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v4.app.NavUtils;

public class EditFeedActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			id			= "";
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_feed);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			id = "" + bundle.getLong("id");
			Feed feed = mDatabase.getFeed(mDB, id);
			setTitle("Edit " + feed.getName());
			
			final EditText txtName = (EditText)findViewById(R.id.editTextName);
			final EditText txtURL = (EditText)findViewById(R.id.editTextURL);
			
			txtName.setText(feed.getName());
			txtURL.setText(feed.getURL());
		}
		
		final Button btnEditFeed = (Button)findViewById(R.id.buttonEditFeed);
		
		if(bundle != null) {
			btnEditFeed.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final EditText txtName = (EditText)findViewById(R.id.editTextName);
					final EditText txtURL = (EditText)findViewById(R.id.editTextURL);
					
					String name = txtName.getText().toString();
					String url = txtURL.getText().toString();
					
					mDatabase.editFeed(mDB, id, name, url);
					
					Intent intent = new Intent(v.getContext(), MainActivity.class);
					startActivityForResult(intent, 0);
				}
			});
		}
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_edit_feed, menu);
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override protected void onDestroy() {
		super.onDestroy();
		if(mDB != null) {
			mDB.close();
		}
		if(mDatabase != null) {
			mDatabase.close();
		}
	}
	
}
