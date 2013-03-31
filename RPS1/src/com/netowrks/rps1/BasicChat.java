package com.netowrks.rps1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.rps.utilities.PacketTypes;

import android.location.Location;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BasicChat extends Activity {// extends Utility {

	TextView textOut;
	EditText textIn, phIn, portIn;
	WiFiUtility myUtility = new WiFiUtility();
	WifiManager wifiManager;
	private Handler handler = new Handler();
	LowerLayer Ll_instance = new LowerLayer();
	LowerLayer.RecieveHelper receiveInstance = Ll_instance.new RecieveHelper();
	private com.netowrks.rps1.GPSTracker gps;
	Singleton tmp = Singleton.getInstance();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_chat);

		textIn = (EditText) findViewById(R.id.textin);
		phIn = (EditText) findViewById(R.id.phin);
		portIn = (EditText) findViewById(R.id.portin);

		Button buttonSend = (Button) findViewById(R.id.send);
		Button connWifi = (Button) findViewById(R.id.connWifi);

		textOut = (TextView) findViewById(R.id.textout);
		buttonSend.setOnClickListener(buttonSendOnClickListener);
		connWifi.setOnClickListener(buttonConnWifiOnClickListener);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		LowerLayer.wifiManager = wifiManager;
		myUtility.connectToFerryNetwork(wifiManager);
		Thread fst = new Thread(new BasicReciever());
		fst.start();

		// LowerLayer.location = getGPSLocation();
		Thread fst1 = new Thread(new startGpsService());
		fst1.start();

		/* Send the GPS location */

		/*
		 * LlPacket sendPkt = new LlPacket(); sendPkt.fromID =
		 * LowerLayer.nodeID; sendPkt.payload =
		 * Double.toString(getGPSLocation().getLatitude()); sendPkt.toID = 0;
		 * sendPkt.type = 1; LowerLayer.SendHelper Send_instance =
		 * Ll_instance.new SendHelper(); Send_instance.execute(sendPkt);
		 */
	}

	/* This function takes care of the sending of chat message */
	private Button.OnClickListener buttonSendOnClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {

			Socket socket = null;
			DataOutputStream dataOutputStream = null;
			DataInputStream dataInputStream = null;

			try {
				LlPacket send_pkt = new LlPacket();
				send_pkt.payload = textIn.getText().toString();// ChatMessage
																// object
				// Assumption: Can be of CHAT_MESSAGE type only because only
				// chat will call send
				// GPS_LIST and SENDER_GPS are sent automatically by lower layer
				send_pkt.type = 0;
				send_pkt.Recv_No = phIn.getText().toString(); // Get the number from text box

				/*
				 * try { send_pkt.port = Integer
				 * .valueOf(portIn.getText().toString()); } catch
				 * (NumberFormatException e) { return; }
				 */
//				LowerLayer.SendHelper Send_instance = Ll_instance.new SendHelper();
//				Send_instance.execute(send_pkt);
				
				if (!Ll_instance.send(send_pkt)) {
					Toast.makeText(
							getApplicationContext(),
							"Sending failed",
							Toast.LENGTH_LONG).show();
				}
				textOut.append("\n Me:" + send_pkt.payload);
			}

			finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (dataOutputStream != null) {
					try {
						dataOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (dataInputStream != null) {
					try {
						dataInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	};

	/* This function takes care of toggling the Wifi State */
	private Button.OnClickListener buttonConnWifiOnClickListener = new Button.OnClickListener() {
		public void onClick(View arg0) {
			myUtility.toggleWifi(wifiManager);
		}
	};

	/*
	 * This is a function that runs in the background waiting for incoming chat
	 * messages
	 */
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
							@Override
							public void run() {
								if (recv_pkt != null) {

									switch (recv_pkt.type) {
									case 0:
										// Call the Archana's method
										textOut.append("\n He:"
												+ recv_pkt.payload.toString());
										break;
									case 1:
										// case is not possible because ferry
										// wont send its gps
										break;
									case 2:
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

							gps = new GPSTracker(BasicChat.this);
							if (gps.canGetLocation()) {
								LowerLayer.location = gps.getLocation();
								Toast.makeText(
										getApplicationContext(),
										"GPS updated : "
												+ Double.toString(LowerLayer.location
														.getLatitude()),
										Toast.LENGTH_LONG).show();
							} else {
								/* Do nothing */
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
