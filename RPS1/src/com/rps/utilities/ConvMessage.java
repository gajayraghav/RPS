package com.rps.utilities;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

@SuppressWarnings("serial")
public class ConvMessage implements Comparator<ConvMessage>, Serializable {
	public boolean left;
	public String comment;
	public boolean isImage;
	public Date timestamp;

	//Used while sending
	public ConvMessage(boolean left, String comment, boolean isImage) {
		super();
		this.left = left;
		this.comment = comment;
		this.isImage = isImage;
		this.timestamp = new Date();
	}
	
	//Used while message is recieved and while object is created form XML file 
	public ConvMessage(boolean left, String comment, boolean isImage, Date timestamp) {
		super();
		this.left = left;
		this.comment = comment;
		this.isImage = isImage;
		this.timestamp = timestamp;
	}

	public int compare(ConvMessage lhs, ConvMessage rhs) {
		return lhs.timestamp.compareTo(rhs.timestamp);
	}

}