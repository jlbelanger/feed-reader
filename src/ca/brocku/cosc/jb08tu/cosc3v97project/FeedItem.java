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
	
	public FeedItem() {
		this("", "", "", "", "", "", "");
	}
	
	public FeedItem(String i, String f, String t, String p, String l, String d, String c) {
		this(i, f, t, p, l, d, c, false);
	}
	
	public FeedItem(String i, String f, String t, String p, String l, String d, String c, boolean iR) {
		this.id = i;
		this.feedId = f;
		this.title = t;
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz");
		try {
			this.pubDate = dateFormat.parse(p);
		}
		catch(ParseException e) {
			dateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm");
			try {
				this.pubDate = dateFormat.parse(p);
			}
			catch(ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
	
	public String getDate(Context context) {
		SimpleDateFormat dateFormatter = Utilities.getDefaultDateFormatter(context);
		return dateFormatter.format(this.pubDate);
	}
	
	public String getPrettyDate(Context context) {
		if(!this.pubDate.equals("")) {
			SimpleDateFormat dateFormatter = Utilities.getDateFormatter(context);
			return dateFormatter.format(this.pubDate);
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
