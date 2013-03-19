package com.rps.utilities;

import java.util.ArrayList;
import java.util.HashMap;

public class ConversationLists {

	private HashMap<String, ArrayList<ConvMessage>> phNumConvMap;
	private static ConversationLists singletonObject;

	private ConversationLists() {
		phNumConvMap = new HashMap<String, ArrayList<ConvMessage>>();
	}

	public static ConversationLists getInstance(String[] args) {
		if (singletonObject == null)
			singletonObject = new ConversationLists();

		return singletonObject;
	}

	public ArrayList<ConvMessage> getConversationsWith(String phNum) {
		return phNumConvMap.get(phNum);
	}
	
	public void processMessagePayload(Object payload) {
		ChatMessage recievedMessage = (ChatMessage)payload;
		ArrayList<ConvMessage> conv =  phNumConvMap.get(recievedMessage.getPhoneNum());
		//conv.add(msg);
	}
}
