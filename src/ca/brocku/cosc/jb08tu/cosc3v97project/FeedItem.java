package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

public class FeedItem implements Serializable {
	private static final long	serialVersionUID	= 1L;
	private String				id;
	private String				feedId;
	private String				title;
	private Date				pubDate;
	private String				link;
	private String				description;
	private String				contentEncoded;
	private boolean				isRead;
	
	public FeedItem(String i, String f, String t, String p, String l, String d, String c, boolean iR) {
		this.id = i;
		this.feedId = f;
		this.title = t;
		DateFormat inDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			this.pubDate = inDateFormat.parse(p);
		}
		catch(ParseException e) {
			e.printStackTrace();
		}
		this.link = l;
		this.description = d;
		this.contentEncoded = c;
		this.isRead = iR;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getFeedId() {
		return this.feedId;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getSortDate() {
		if(!this.pubDate.equals("")) {
			DateFormat outDateFormat = Utilities.getDateFormatter();
			return outDateFormat.format(this.pubDate);
		}
		return "";
	}
	
	public String getPrettyDate(Context context) {
		if(!this.pubDate.equals("")) {
			DateFormat outDateFormat = Utilities.getDateFormatter(context);
			return outDateFormat.format(this.pubDate);
		}
		return "";
	}
	
	public String getLink() {
		return this.link;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getContentEncoded() {
		return this.contentEncoded;
	}
	
	public String getContent() {
		int descLength = this.description.length();
		int contentLength = this.contentEncoded.length();
		if(descLength > contentLength) {
			return this.description;
		}
		return this.contentEncoded;
	}
	
	public boolean isRead() {
		return this.isRead;
	}
	
	public String toString() {
		return this.getTitle();
	}
}
