package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.util.ConcurrentModificationException;
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
				Log.i("feed", i + ": checking feeds...");
				try {
					for(Feed feed : feeds) {
						if(mDatabase.doesFeedExist(mDB, feed.getId())) {
							feedItems = Utilities.getNewFeedItems(mDatabase, mDB, feed);
							if(feedItems.size() > 0) {
								mDatabase.addNewFeedItemsToDatabase(mDB, feedItems);
								Message message = Message.obtain();
								message.arg1 = Activity.RESULT_OK;
								message.obj = feed;
								try {
									messenger.send(message);
								}
								catch(android.os.RemoteException e) {}
							}
						}
						else {
							feeds.remove(feed);
						}
					}
				}
				catch(ConcurrentModificationException e) {}
				SystemClock.sleep(updateInterval);
				i++;
			}
		}
	}
}
