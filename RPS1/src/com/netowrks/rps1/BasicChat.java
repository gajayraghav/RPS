package com.netowrks.rps1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
//import android.os.Handler;
import android.content.Context;
import android.view.View;
//import android.widget.Button;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BasicChat extends Utility {

	TextView textOut;
	EditText textIn, ipIn, portIn;
	Utility myUtility = new Utility();
	WifiManager wifiManager;
	private Handler handler = new Handler();
	Ll Ll_instance = new Ll();
	Ll.receive Receive_instance = Ll_instance.new receive();

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
		myUtility.ConnectTo(wifiManager);
		Thread fst = new Thread(new myReceive());
		fst.start();
	}

	/* This function takes care of the sending of chat message */
	private Button.OnClickListener buttonSendOnClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {

			// TODO Auto-generated method stub
			Socket socket = null;
			DataOutputStream dataOutputStream = null;
			DataInputStream dataInputStream = null;

			try {

				Ll Ll_instance = new Ll();
				Ll.send Send_instance = Ll_instance.new send();
				LlMl_comm send_pkt = new LlMl_comm();
				send_pkt.Buff = textIn.getText().toString();
				send_pkt.NP = "sakthi";
				send_pkt.type = 1;
				send_pkt.ipAddr = ipIn.getText().toString();
				try {
					send_pkt.port = Integer.valueOf(portIn.getText().toString());
				} catch (NumberFormatException e) {
					return;
				}
				Send_instance.execute(send_pkt);
				textOut.append("\n Me:" + send_pkt.Buff);
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

	
	/* This is a function that runs in the background waiting for incoming chat messages */
	private class myReceive implements Runnable {

		// @Override
		public void run() {
			try {
				while (true) {
					// Call receive function only if the wifi is connected to
					// some network
					if (wifiManager.getConnectionInfo().getNetworkId() != -1) {
						final LlMl_comm recv_pkt = Receive_instance.doInBackground();
						// runOnUiThread(new Runnable() {
						handler.post(new Runnable() {
							@Override
							public void run() {
								if (recv_pkt != null) {
									textOut.append("\n He:" + recv_pkt.Buff);
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
