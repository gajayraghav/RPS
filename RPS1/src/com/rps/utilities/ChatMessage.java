package com.rps.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;

import android.net.Uri;
import android.os.Environment;

@SuppressWarnings("serial")
public class ChatMessage implements Serializable {
	private ChatMessageTypes messageType;
	private String phoneNum;
	private Object content;
	private Date timestamp;

	public ChatMessage(ChatMessageTypes messageType, String phoneNum,
			Object content,Date timestamp) {
		this.messageType = messageType;
		this.phoneNum = phoneNum;
		this.content = content;
		this.timestamp = timestamp;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public ChatMessageTypes getMessageType() {
		return messageType;
	}

	public void setMessageType(ChatMessageTypes messageType) {
		this.messageType = messageType;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public Date getTimestamp() {
		return timestamp;
	}

}
