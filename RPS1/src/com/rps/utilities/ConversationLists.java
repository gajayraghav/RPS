package com.rps.utilities;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

public class ConversationLists {

	private HashMap<String, ArrayList<ConvMessage>> phNumConvMap;
	private HashMap<String, String> namePhNumMap;
	ContentResolver contentResolve;
	private static ConversationLists singletonObject;

	private ConversationLists() {
		phNumConvMap = new HashMap<String, ArrayList<ConvMessage>>();
		namePhNumMap = new HashMap<String, String>();
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

	public List<String> getNames() {
		ArrayList<String> list = new ArrayList<String>(namePhNumMap.keySet());
		return list;
	}

	public void putNamePhnum(String name, String phNum) {
		namePhNumMap.put(name, phNum);
	}

	public String getPhoneNumber(String name) {
		return namePhNumMap.get(name);
	}

	public String getName(String phNum) {
		String name = "", num;
		if (namePhNumMap.containsValue(phNum))
			for (String nameKey : namePhNumMap.keySet()) {
				num = namePhNumMap.get(nameKey);
				if (num.equals(phNum)) {
					name = nameKey;
					break;
				}
			}
		else {
			name = getContactDisplayNameByNumber(phNum);
			namePhNumMap.put(name, phNum);
		}
		return name;
	}

	private String getContactDisplayNameByNumber(String number) {
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String name = "?";

		Cursor contactLookup = contentResolve.query(uri, new String[] {
				BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
				null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				name = contactLookup.getString(contactLookup
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				// String contactId =
				// contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}
		System.out.println("Matched phone number " + number + " to " + name);
		return name;
	}

	public void writeToFile() {
		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "messages.ser");
		if (file.exists())
			file.delete();
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		FileOutputStream fileos = null;
		try {
			fileos = new FileOutputStream(file);

		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException : " + e.toString());
			e.printStackTrace();
		}
		try {
			ObjectOutputStream oos = new ObjectOutputStream(fileos);
			// oos.writeObject(phNumConvMap);
			for (String phNum : phNumConvMap.keySet()) {
				oos.writeObject(phNum);
				oos.writeObject(getName(phNum));
				oos.writeObject(phNumConvMap.get(phNum));
				System.out.println("Writing : " + phNum);
			}
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done Writing");
	}

	@SuppressWarnings("unchecked")
	public void populateFromFile() {
		File messagesFile = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "messages.ser");
		if (!messagesFile.exists())
			return;

		FileInputStream messagesStream = null;
		try {
			messagesStream = new FileInputStream(messagesFile);
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException : " + e.toString());
			e.printStackTrace();
		}
		try {
			ObjectInputStream ois = new ObjectInputStream(messagesStream);
			String name, number;
			ArrayList<ConvMessage> conv;
			while (true) {
				try {
					number = (String) ois.readObject();
					name = (String) ois.readObject();
					conv = (ArrayList<ConvMessage>) ois.readObject();
					phNumConvMap.put(number, conv);
					namePhNumMap.put(name, number);
					System.out.println("Read : " + name + number);
				} catch (EOFException end) {
					break;
				}
			}
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Done Reading");
	}

	public void deleteConversation(String name, String number) {
		phNumConvMap.remove(number);
		namePhNumMap.remove(name);
	}
	/*
	 * private void writeToXMLfile() { File file = new
	 * File(Environment.getExternalStorageDirectory() + File.separator +
	 * "messages.xml"); if (file.exists()) file.delete(); try {
	 * file.createNewFile(); } catch (IOException e1) { catch block
	 * e1.printStackTrace(); }
	 * 
	 * FileOutputStream fileos = null; try { fileos = new
	 * FileOutputStream(file);
	 * 
	 * } catch (FileNotFoundException e) {
	 * System.out.println("FileNotFoundException : " + e.toString());
	 * e.printStackTrace(); }
	 * 
	 * XmlSerializer serializer = Xml.newSerializer(); try {
	 * serializer.setOutput(fileos, "UTF-8"); serializer.startDocument(null,
	 * Boolean.valueOf(true)); serializer.setFeature(
	 * "http://xmlpull.org/v1/doc/features.html#indent-output", true);
	 * 
	 * serializer.startTag(null, "AllConversations"); for (String phNum :
	 * phNumConvMap.keySet()) {
	 * 
	 * serializer.startTag(null, "ConversationWith");
	 * 
	 * serializer.startTag(null, "Phone"); serializer.text(phNum);
	 * serializer.endTag(null, "Phone");
	 * 
	 * serializer.startTag(null, "Name"); serializer.text(getName(phNum));
	 * serializer.endTag(null, "Name");
	 * 
	 * serializer.startTag(null, "ConvObject"); // serializer.
	 * (phNumConvMap.get(phNum)); serializer.endTag(null, "ConvObject");
	 * 
	 * serializer.endTag(null, "ConversationWith"); } serializer.endTag(null,
	 * "AllConversations"); serializer.endDocument(); serializer.flush();
	 * fileos.close(); } catch (Exception e) {
	 * System.out.println("Exception occured in writing message xml"); } }
	 */
}
