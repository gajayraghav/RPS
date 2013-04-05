package com.rps.utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.content.ContentResolver;

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

	public ConvMessage processMessagePayload(Object payload, String phNum) {
		ChatMessage recievedMessage = (ChatMessage) payload;
		boolean left = true;
		String comment;
		boolean isImage;
		Date timestamp = recievedMessage.getTimestamp();

		if (recievedMessage.getMessageType() == ChatMessageTypes.TEXT) {
			comment = (String) recievedMessage.getContent();
			isImage = false;
		} else {
			BitmapWrapper wrappedImage = (BitmapWrapper) recievedMessage
					.getContent();
			comment = wrappedImage.saveImageToSDcard(contentResolve);
			isImage = true;			
		}

		ConvMessage newMessage = new ConvMessage(left, comment, isImage,
				timestamp);
		addMessageToConversationWith(phNum, newMessage);
		return newMessage;
	}

	public ArrayList<ConvMessage> getConversationsWith(String phNum) {
		ArrayList<ConvMessage> conv;
		if (phNumConvMap.containsKey(phNum))
			conv = phNumConvMap.get(phNum);
		else {
			conv = new ArrayList<ConvMessage>();
			phNumConvMap.put(phNum, conv);
		}
		return conv;
	}

	public void addMessageToConversationWith(String phNum, ConvMessage msg) {
		ArrayList<ConvMessage> conv;
		if (phNumConvMap.containsKey(phNum))
			conv = phNumConvMap.get(phNum);
		else {
			conv = new ArrayList<ConvMessage>();
			phNumConvMap.put(phNum, conv);
		}
		conv.add(msg);
	}

	public int getMessageCountWith(String phNum) {
		ArrayList<ConvMessage> conv;
		if (phNumConvMap.containsKey(phNum)) {
			conv = phNumConvMap.get(phNum);
			return conv.size();
		} else
			return 0;
	}

	public List<String> getPhoneNumbers() {
		ArrayList<String> list = new ArrayList<String>(phNumConvMap.keySet());
		return list;
	}

}
