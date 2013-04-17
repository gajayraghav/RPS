package com.netowrks.rps1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.rps.utilities.ConvMessage;
import com.rps.utilities.ConversationLists;

import android.R.color;
import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Home extends Activity {

	public static int thisPhoneNumber=0;
	WiFiUtility myUtility = new WiFiUtility();
	WifiManager wifiManager;
	private Handler handler = new Handler();
	LowerLayer.RecieveHelper receiveInstance = new LowerLayer().new RecieveHelper();
	private GPSTracker gps;
	Singleton tmp = Singleton.getInstance();

	ArrayAdapter<String> nameAdapter;
	ListView phNumListView;
	ConversationLists conversations;
	String myPhoneNumber;
	
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
		conversations.populateFromFile();
		nameAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, conversations.getNames());
		try {
			phNumListView = (ListView) findViewById(R.id.phoneNumberListView);
			phNumListView
					.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			phNumListView.setBackground(getResources().getDrawable(R.drawable.bubble_yellow));
			phNumListView.setAdapter(nameAdapter);
			// nameAdapter.add("Myself");
			// conversations.putNamePhnum("Myself", "4049160131");
			phNumListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String name = nameAdapter.getItem(position);
					System.out.println("Item at " + position + " " + name);
					moveToConversationChat(conversations.getPhoneNumber(name),
							name);
				}
			});

			phNumListView
					.setOnItemLongClickListener(new OnItemLongClickListener() {
						@Override
						public boolean onItemLongClick(AdapterView<?> parent,
								View view, int position, long id) {
							String name = nameAdapter.getItem(position);
							String number = conversations.getPhoneNumber(name);
							deleteWithConfirmation(name, number);
							return false;
						}
					});

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		myPhoneNumber = getMyPhoneNumber();
		initServices();
	}

	private String getMyPhoneNumber() {
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, 1, 1, "Show Map");
		menu.add(1, 2, 2, "Register");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Intent intShowMap = new Intent(this, Map.class);
			Toast.makeText(getApplicationContext(),
					"you clicked" + item.getTitle(), Toast.LENGTH_SHORT).show();
			intShowMap.putExtra("myPhone", myPhoneNumber);
			this.startActivity(intShowMap);
			return true;
		case 2:
			Toast.makeText(getApplicationContext(),
					"you clicked" + item.getTitle(), Toast.LENGTH_SHORT).show();

			Log.v("Nodeid", RegistrationNodeID.NodeId);
			if (RegistrationNodeID.NodeId.contains("null")) {
				Context context1 = getApplicationContext();

				try {
					FileInputStream fos2;
					fos2 = context1.openFileInput("NodeID.txt");
					StringBuffer fileContent = new StringBuffer("");

					byte[] buffer = new byte[20];

					while (fos2.read(buffer) != -1) {
						fileContent.append(new String(buffer));
					}
					fos2.close();
					if (fileContent.toString().length() > 1) {
						RegistrationNodeID.NodeId = fileContent.toString();
						Toast.makeText(getApplicationContext(),
								"You have already registered!",
								Toast.LENGTH_LONG).show();
						break;
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Intent intReg = new Intent(this, Registration.class);
				this.startActivity(intReg);

			} else {
				Toast.makeText(getApplicationContext(), "You have registered!",
						Toast.LENGTH_LONG).show();
			}
		}
		return super.onOptionsItemSelected(item);

	}

	private void deleteWithConfirmation(final String name, final String number) {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("Delete Confirmation");
		myAlertDialog
				.setMessage("Are you sure you want to delete the conversation with "
						+ name);

		myAlertDialog.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						conversations.deleteConversation(name, number);
						nameAdapter.remove(name);
					}
				});

		myAlertDialog.setNegativeButton("No", null);
		myAlertDialog.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		conversations.writeToFile();
	};

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

			final String name;
			String fullPhNum = "", id;

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
						fullPhNum = pCur.getString(pCur
								.getColumnIndex(Phone.NUMBER));
						fullPhNum = fullPhNum.replaceAll("[^\\d]", "");
						if (fullPhNum.length() > 10)
							fullPhNum = fullPhNum
									.substring(fullPhNum.length() - 10);
						allNumbers.add(fullPhNum);
						System.out.println("Got phone number : " + fullPhNum);
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
										nameAdapter.add(name);
										conversations.putNamePhnum(name,
												phoneNumber);
										moveToConversationChat(phoneNumber,
												name);
									}
								});
						Dialog dia = builder.create();
						dia.show();
					} else {
						String phoneNumber = allNumbers.get(0);
						System.out.println("Contact number : " + phoneNumber);
						nameAdapter.add(name);
						moveToConversationChat(phoneNumber, name);
					}
				}
			}
		}
	}

	private void moveToConversationChat(String phoneNumber, String name) {
		Intent convIntent = new Intent(this, Chat.class);
		convIntent.putExtra("phoneNum", phoneNumber);
		convIntent.putExtra("name", name);
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
											nameAdapter.add(conversations
													.getName(phNum));

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
										LinkedHashMap<Long, String> gpsList = (LinkedHashMap<Long, String>) recv_pkt.payload;
										tmp.putHashMap(gpsList);
										Iterator<Entry<Long, String>> gpsIter = gpsList
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
