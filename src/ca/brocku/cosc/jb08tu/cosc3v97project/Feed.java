package ca.brocku.cosc.jb08tu.cosc3v97project;

import java.io.Serializable;

public class Feed implements Comparable<Feed>, Serializable {
	private static final long	serialVersionUID	= 1L;
	private String				id;
	private String				name;
	private String				url;
	
	public Feed(String i, String n, String u) {
		this.id = i;
		this.name = n;
		this.url = u;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getURL() {
		return this.url;
	}
	
	@Override public int compareTo(Feed feed2) {
		String name1 = this.getName();
		String name2 = feed2.getName();
		return name1.compareTo(name2);
	}
	
	public String toString() {
		return this.getName();
	}
}
