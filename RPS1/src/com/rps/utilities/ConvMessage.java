package com.rps.utilities;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

@SuppressWarnings("serial")
public class ConvMessage implements Comparator<ConvMessage>, Serializable {
	public boolean left;
	public String comment;
	public Bitmap originalBitmap;
	public boolean isImage;
	public Date timestamp;

	// Used while sending
	public ConvMessage(boolean left, String comment, boolean isImage) {
		super();
		
		setContent(comment, isImage);
		
		this.left = left;
		//this.left = false;
		this.timestamp = new Date();
	}

	// Used while message is recieved and while object is created form XML file
	public ConvMessage(boolean left, String comment, boolean isImage,
			Date timestamp) {
		super();
		
		setContent(comment, isImage);
		this.left = left;
		this.timestamp = timestamp;
	}

	private void setContent(String comment, boolean isImage)
	{
		this.comment = comment;
		this.isImage = isImage;
		if (isImage)
			this.originalBitmap = BitmapFactory.decodeFile(comment);		
	}
	
	public int compare(ConvMessage lhs, ConvMessage rhs) {
		return lhs.timestamp.compareTo(rhs.timestamp);
	}

}