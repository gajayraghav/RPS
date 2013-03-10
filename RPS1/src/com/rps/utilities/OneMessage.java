package com.rps.utilities;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

@SuppressWarnings("serial")
public class OneMessage implements Comparator<OneMessage>, Serializable {
	public boolean left;
	public String comment;
	public boolean isImage;
	public Date timestamp;

	public OneMessage(boolean left, String comment, boolean isImage) {
		super();
		this.left = left;
		this.comment = comment;
		this.isImage = isImage;
		this.timestamp = new Date();
	}
	
	public OneMessage(boolean left, String comment, boolean isImage, Date timestamp) {
		super();
		this.left = left;
		this.comment = comment;
		this.isImage = isImage;
		this.timestamp = timestamp;
	}

	public int compare(OneMessage lhs, OneMessage rhs) {
		return lhs.timestamp.compareTo(rhs.timestamp);
	}

}