package com.rps.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class ConversationLists {

	private HashMap<String, ArrayList<ConvMessage>> phNumConvMap;
	ContentResolver contentResolve;
	private static ConversationLists singletonObject;

	private ConversationLists() {
		phNumConvMap = new HashMap<String, ArrayList<ConvMessage>>();
	}

	public static ConversationLists getInstance(ContentResolver contentResolve) {
		if (singletonObject == null)
			singletonObject = new ConversationLists();

		singletonObject.setContentResolver(contentResolve);
		return singletonObject;
	}

	private void setContentResolver(ContentResolver contentResolve) {
		this.contentResolve = contentResolve;
	}

	public ArrayList<ConvMessage> getConversationsWith(String phNum) {
		return phNumConvMap.get(phNum);
	}

	public void processMessagePayload(Object payload) {
		ChatMessage recievedMessage = (ChatMessage) payload;
		ArrayList<ConvMessage> conv = phNumConvMap.get(recievedMessage
				.getPhoneNum());
		boolean left = true;
		String comment;
		boolean isImage;
		Date timestamp = recievedMessage.getTimestamp();

		if (recievedMessage.getMessageType() == ChatMessageTypes.TEXT) {
			comment = (String) recievedMessage.getContent();
			isImage=false;
		} else {
			Bitmap image = (Bitmap) recievedMessage.getContent();
			long recievedTime = System.currentTimeMillis();
			String photoPath = Environment.getExternalStorageDirectory()
					+ File.separator + "trialDir" + File.separator;

			File sdImageFile = new File(photoPath);
			sdImageFile.mkdirs();

			photoPath += "recieve_" + recievedTime + ".jpeg";
			sdImageFile = new File(photoPath);

			Uri outputFileUri = Uri.fromFile(sdImageFile);
			System.out.println("Cam : " + outputFileUri);
			OutputStream imageFileOS;
			try {
				imageFileOS = contentResolve.openOutputStream(outputFileUri);
				image.compress(Bitmap.CompressFormat.JPEG, 100, imageFileOS);
				imageFileOS.flush();
				imageFileOS.close();
				System.out.println("Image saved");

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isImage=true;
			comment = photoPath;
		}
		
		ConvMessage newMessage = new ConvMessage(left, comment, isImage,
				timestamp);
		conv.add(newMessage);

	}
}
