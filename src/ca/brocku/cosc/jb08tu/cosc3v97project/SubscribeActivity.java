package ca.brocku.cosc.jb08tu.cosc3v97project;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v4.app.NavUtils;

public class SubscribeActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected Cursor				mCursor		= null;
	protected SQLiteDatabase		mDB			= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subscribe);
	}
	
	@Override public void onStart() {
		super.onStart();
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
		
		final Button btnSubscribe = (Button)findViewById(R.id.buttonSubscribe);
		btnSubscribe.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// get EditText
				final EditText txtURL = (EditText)findViewById(R.id.editTextURL);
				
				// get EditText value
				String url = txtURL.getText().toString();
				
				if(!Utilities.isValidURL(url)) {
					Builder dialog = new AlertDialog.Builder(SubscribeActivity.this);
					dialog.setMessage("This is not a valid RSS file.");
					dialog.setPositiveButton("OK", null);
					dialog.show();
					return;
				}
				
				// get feed name
				String name = Utilities.getFeedTitle(url);
				
				// update database
				mDatabase.addFeed(mDB, name, url);
				
				// return to main activity
				Intent intent = new Intent(v.getContext(), MainActivity.class);
				startActivityForResult(intent, 0);
			}
		});
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_subscribe, menu);
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
