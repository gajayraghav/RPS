package com.rps.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

import android.net.Uri;
import android.os.Environment;

@SuppressWarnings("serial")
public class ChatMessage implements Serializable {
	private ChatMessageTypes messageType;
	private String phoneNum;
	private Object content;

	public ChatMessage(ChatMessageTypes messageType, String phoneNum,
			Object content) {
		// parse message
		// if type is image
		// store in gallery and save path
		String photoPath = Environment.getExternalStorageDirectory()
				+ File.separator + "trialDir" + File.separator;

		File sdImageMainDirectory = new File(photoPath);
		sdImageMainDirectory.mkdirs();

		long captureTime = System.currentTimeMillis();
		photoPath += "pic" + captureTime + ".jpeg";
		File picFile = new File(photoPath);
//		Uri uriSavedImage = Uri.fromFile(picFile);
//		OutputStream imageFileOS;
//		try {
//			imageFileOS = getContentResolver().openOutputStream(uriSavedImage);
//			imageFileOS.write(arg0);
//			imageFileOS.flush();
//			imageFileOS.close();
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

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

}
