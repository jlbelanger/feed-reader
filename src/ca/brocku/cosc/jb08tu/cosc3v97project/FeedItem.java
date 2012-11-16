package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

public class FeedItem implements Serializable {
	private static final long	serialVersionUID	= 1L;
	private String				title;
	private String				pubDate;
	private String				link;
	private String				description;
	
	public FeedItem() {
		this("", "", "", "");
	}
	
	public FeedItem(String t, String p, String l, String d) {
		this.title = t;
		this.pubDate = p;
		this.link = l;
		this.description = d;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getPubDate(Context context) {
		SimpleDateFormat dateFormatter = Utilities.getDateFormatter(context);
		return dateFormatter.format(new Date(this.pubDate));
	}
	
	public String getLink() {
		return this.link;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String toString() {
		return this.getTitle();
	}
}
