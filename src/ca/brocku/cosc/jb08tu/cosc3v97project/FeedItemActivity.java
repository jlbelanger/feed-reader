package ca.brocku.cosc.jb08tu.cosc3v97project;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;

public class FeedItemActivity extends Activity {
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_item);
	}
	
	@Override public void onStart() {
		super.onStart();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed
			Feed feed = (Feed)bundle.getSerializable("feed");
			
			// get feed item
			FeedItem feedItem = (FeedItem)bundle.getSerializable("feedItem");
			
			// update interface
			setTitle(feed.getName());
			
			// get TextView
			final TextView txtTitle = (TextView)findViewById(R.id.textViewTitle);
			final TextView txtDate = (TextView)findViewById(R.id.textViewDate);
			final TextView txtContent = (TextView)findViewById(R.id.textViewContent);
			
			// set TextView
			txtTitle.setText(Html.fromHtml("<a href=\"" + feedItem.getLink() + "\">" + feedItem.getTitle() + "</a>"));
			txtTitle.setMovementMethod(LinkMovementMethod.getInstance());
			txtDate.setText(feedItem.getPubDate(this));
			txtContent.setText(Html.fromHtml(feedItem.getContent()));
			txtContent.setMovementMethod(new ScrollingMovementMethod());
		}
		
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_feed_item, menu);
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
}
