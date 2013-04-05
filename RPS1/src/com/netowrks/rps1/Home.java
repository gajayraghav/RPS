package com.netowrks.rps1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.rps.utilities.ConvMessage;
import com.rps.utilities.ConversationLists;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Home extends Activity {

	WiFiUtility myUtility = new WiFiUtility();
	WifiManager wifiManager;
	private Handler handler = new Handler();
	LowerLayer.RecieveHelper receiveInstance = new LowerLayer().new RecieveHelper();
	private GPSTracker gps;
	Singleton tmp = Singleton.getInstance();

	ArrayAdapter<String> phoneNumAdapter;
	ListView phNumListView;
	ConversationLists conversations;

	private void initServices() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		LowerLayer.wifiManager = wifiManager;
		myUtility.connectToFerryNetwork(wifiManager);
		Thread fst = new Thread(new BasicReciever());
		fst.start();

		// LowerLayer.location = getGPSLocation();
		Thread fst1 = new Thread(new startGpsService());
		fst1.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		conversations = ConversationLists.getInstance(getContentResolver());
		phoneNumAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				conversations.getPhoneNumbers());
		try {
			phNumListView = (ListView) findViewById(R.id.phoneNumberListView);
			phNumListView
					.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

			phNumListView.setAdapter(phoneNumAdapter);
			phoneNumAdapter.add("4049160131");
			phNumListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String phoneNumber = phoneNumAdapter.getItem(position);
					System.out.println("Item at " + position + " "
							+ phoneNumber);
					moveToConversationChat(phoneNumber);
				}
			});
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		initServices();
	}

	public void addNewMessage(View v) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, 1);
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			Uri contactData = data.getData();
			System.out.println("Got contact : " + contactData);
			Cursor contact = getContentResolver().query(contactData, null,
					null, null, null);

			String name, number, phone = "", id;

			if (contact.moveToFirst()) {
				name = contact.getString(contact
						.getColumnIndex(Phone.DISPLAY_NAME));
				System.out.println("Contact name : " + name);

				id = contact.getString(contact
						.getColumnIndex(ContactsContract.Contacts._ID));
				if (Integer
						.parseInt(contact.getString(contact
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = getContentResolver().query(Phone.CONTENT_URI,
							null, Phone.CONTACT_ID + " = ?",
							new String[] { id }, null);

					final ArrayList<String> allNumbers = new ArrayList<String>();
					while (pCur.moveToNext()) {
						phone = pCur.getString(pCur
								.getColumnIndex(Phone.NUMBER));
						phone = phone.replaceAll("[^\\d]", "");
						if (phone.length() > 10)
							phone = phone.substring(phone.length() - 10);
						allNumbers.add(phone);
						System.out.println("Got phone number : " + phone);
					}

					if (allNumbers.size() > 1) {
						String[] arr = new String[allNumbers.size()];
						AlertDialog.Builder builder = new AlertDialog.Builder(
								this);
						builder.setTitle("Pick number");
						builder.setItems(allNumbers.toArray(arr),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										String phoneNumber = allNumbers
												.get(which);
										System.out.println("Contact number : "
												+ phoneNumber);
										phoneNumAdapter.add(phoneNumber);
										moveToConversationChat(phoneNumber);
									}
								});
						Dialog dia = builder.create();
						dia.show();
					} else {
						String phoneNumber = allNumbers.get(0);
						System.out.println("Contact number : " + phoneNumber);
						phoneNumAdapter.add(phoneNumber);
						moveToConversationChat(phoneNumber);
					}
					// String number = contact .getString(contact
					// .getColumnIndex(ContactsContract
					// .CommonDataKinds.Phone.DATA));

				}
			}
		}
	}

	private void moveToConversationChat(String phoneNumber) {
		Intent convIntent = new Intent(this, Chat.class);
		convIntent.putExtra("phoneNum", phoneNumber);
		startActivity(convIntent);
	}

	// This is a function that runs in the background waiting for incoming chat
	// messages

	private class BasicReciever implements Runnable {
		// @Override
		public void run() {
			try {
				while (true) {
					// Call receive function only if the wifi is connected to
					// some network
					if (wifiManager.getConnectionInfo().getNetworkId() != -1) {
						final LlPacket recv_pkt = receiveInstance
								.doInBackground();
						// runOnUiThread(new Runnable() {
						handler.post(new Runnable() {
							@SuppressLint("NewApi")
							@Override
							public void run() {
								if (recv_pkt != null) {

									switch (recv_pkt.type) {
									case 0:
										// Call the Archana's method
										System.out.println("Recieved : "
												+ recv_pkt.payload);
										String phNum = recv_pkt.Send_No;
										if (conversations
												.getMessageCountWith(phNum) == 0)
											phoneNumAdapter.add(phNum);

										ConvMessage msg = conversations
												.processMessagePayload(
														recv_pkt.payload, phNum);
										if (Chat.currentPhoneNumber != null
												&& Chat.currentPhoneNumber
														.equals(phNum))
											Chat.convAdapter.add(msg);
										else {
											// Prepare intent which is triggered
											// if the
											// notification is selected

											Intent intent = getIntent();
											PendingIntent pIntent = PendingIntent
													.getActivity(
															getApplicationContext(),
															0, intent, 0);

											// Build notification
											// Actions are just fake
											Notification noti = new Notification.Builder(
													getApplicationContext())
													.setContentTitle(
															"New message : "
																	+ phNum)
													.setContentText("")
													.setSmallIcon(
															R.drawable.noti_icon)
													.setContentIntent(pIntent)
													.build();

											NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

											// Hide the notification after its
											// selected
											noti.flags |= Notification.FLAG_AUTO_CANCEL;

											notificationManager.notify(0, noti);
										}

										Toast.makeText(
												getApplicationContext(),
												"Recieved message from "
														+ phNum,
												Toast.LENGTH_SHORT).show();
										break;
									case 1:
										// case is not possible because ferry
										// wont send its gps
										break;
									case 2:
										@SuppressWarnings("unchecked")
										HashMap<Integer, String> gpsList = (HashMap<Integer, String>) recv_pkt.payload;
										tmp.putHashMap(gpsList);
										Iterator<Entry<Integer, String>> gpsIter = gpsList
												.entrySet().iterator();
										while (gpsIter.hasNext()) {
											Toast.makeText(
													getApplicationContext(),
													gpsIter.next().getValue(),
													Toast.LENGTH_LONG).show();
										}
										// Call Ajay's method
										break;
									default:
										break;
									}

								}
							}
						});
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class startGpsService implements Runnable {
		public void run() {
			try {
				while (true) {
					handler.post(new Runnable() { // This thread runs in the UI
						@Override
						public void run() {

							gps = new GPSTracker(Home.this);
							if (gps.canGetLocation()) {
								LowerLayer.location = gps.getLocation();
								Toast.makeText(
										getApplicationContext(),
										"GPS updated : "
												+ Double.toString(LowerLayer.location
														.getLatitude()),
										Toast.LENGTH_LONG).show();
							} else {

							}
						}
					});
					Thread.sleep(1 * 60 * 1000);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
