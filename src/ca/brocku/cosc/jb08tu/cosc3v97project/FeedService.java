package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.util.List;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;

public class FeedService extends IntentService {
	private int	result	= Activity.RESULT_CANCELED;
	
	public FeedService() {
		this("FeedService");
	}
	
	public FeedService(String name) {
		super(name);
	}
	
	@Override protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		if(extras != null) {
			Messenger messenger = (Messenger)extras.get("messenger");
			List<Feed> feeds = (List<Feed>)extras.get("feeds");
			int updateInterval = (Integer)extras.get("updateInterval") * 1000;
			FeedDatabaseHelper mDatabase = new FeedDatabaseHelper(getApplicationContext());
			SQLiteDatabase mDB = mDatabase.getReadableDatabase();
			List<FeedItem> feedItems;
			int i = 0;
			while(true) {
				for(Feed feed : feeds) {
					Log.i("feed", i + ": checking feed " + feed.getName());
					feedItems = UtilitiesXML.getNewFeedItems(this, mDatabase, mDB, feed);
					if(feedItems.size() > 0) {
						mDatabase.addNewFeedItemsToDatabase(feedItems, getApplicationContext(), mDB);
						result = Activity.RESULT_OK;
						Message msg = Message.obtain();
						msg.arg1 = result;
						msg.obj = feed.getName();
						try {
							messenger.send(msg);
						}
						catch(android.os.RemoteException e) {}
					}
				}
				SystemClock.sleep(updateInterval);
				i++;
			}
		}
	}
}
