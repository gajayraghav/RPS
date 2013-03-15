package com.netowrks.rps1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.rps.utilities.PacketTypes;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BasicChat extends Activity {// extends Utility {

	TextView textOut;
	EditText textIn, ipIn, portIn;
	WiFiUtility myUtility = new WiFiUtility();
	WifiManager wifiManager;
	private Handler handler = new Handler();
	LowerLayer Ll_instance = new LowerLayer();
	LowerLayer.RecieveHelper receiveInstance = Ll_instance.new RecieveHelper();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_chat);

		textIn = (EditText) findViewById(R.id.textin);
		ipIn = (EditText) findViewById(R.id.ipin);
		portIn = (EditText) findViewById(R.id.portin);

		Button buttonSend = (Button) findViewById(R.id.send);
		Button connWifi = (Button) findViewById(R.id.connWifi);

		textOut = (TextView) findViewById(R.id.textout);
		buttonSend.setOnClickListener(buttonSendOnClickListener);
		connWifi.setOnClickListener(buttonConnWifiOnClickListener);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		myUtility.connectToFerryNetwork(wifiManager);
		Thread fst = new Thread(new BasicReciever());
		fst.start();
	}

	/* This function takes care of the sending of chat message */
	private Button.OnClickListener buttonSendOnClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			//Ajay - refactor this into a send utility that the chat application can call 
			//
			
			Socket socket = null;
			DataOutputStream dataOutputStream = null;
			DataInputStream dataInputStream = null;

			try {
				LowerLayer Ll_instance = new LowerLayer();
				LowerLayer.SendHelper Send_instance = Ll_instance.new SendHelper();
				LlPacket send_pkt = new LlPacket();
				send_pkt.payload = textIn.getText().toString();//ChatMessage object
				//Assumption: Can be of CHAT_MESSAGE type only because only chat will call send
				//GPS_LIST and SENDER_GPS are sent automatically by lower layer
				send_pkt.type = 0;
				send_pkt.ipAddr = ipIn.getText().toString();
				send_pkt.toID = 0;
/*				
				try {
					send_pkt.port = Integer
							.valueOf(portIn.getText().toString());
				} catch (NumberFormatException e) {
					return;
				}
*/
				
				Send_instance.execute(send_pkt);
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
									case 2:
										// Call Ajay's method
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

}
