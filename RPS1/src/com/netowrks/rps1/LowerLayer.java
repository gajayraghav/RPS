package com.netowrks.rps1;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

public class LowerLayer {

	/* Node details */
	static public int nodeID = 2533;
	final int port = 8888;
	
	/* Listening Socket */
	private ServerSocket servSock;
	
	/* Buffer related parameters */
	static int availableBuffer = 10000000; //10MB
	static int instanceCount = 0;
	static private List<LlPacket> outputQueue = new ArrayList<LlPacket>();
	static Location location;
	
	/* This value needs to be populated by the UI thread */
	public static WifiManager wifiManager;
	
	LowerLayer() {
		try {
			servSock = new ServerSocket(port);
			
			/* The Queue handler thread should be started only once */
			if (instanceCount++ == 0) {
				Thread fst = new Thread(new QueueHandler());
				fst.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void Ll_close() {
		try {
			/* Close the Receiver Socket on exit */
			if (servSock != null) {
				servSock.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class SendHelper extends AsyncTask<LlPacket, Void, Void> {
		protected Void doInBackground(LlPacket... params) {
			
			/* Create the out going packet */
			LlPacket sendPkt = new LlPacket();
			
			try {

				/*
				 * Fill the outgoing packet : Note - this needs to come above
				 * the socket creation part
				 */
				sendPkt.fromID = nodeID;
				sendPkt.toID = params[0].toID;
				sendPkt.payload = params[0].payload;
				sendPkt.type = params[0].type;

				/* Check if the ferry is connected and then proceed */
				if (wifiManager.getConnectionInfo().getSSID()
						.equals(WiFiUtility.networkSSID)) {
		
					/* Get the IP address of the ferry */
					String ipAddr = intToIp(wifiManager.getDhcpInfo().gateway);
					
					/* Create and prepare sending socket */
					Socket sendSock = new Socket(ipAddr, port);
					OutputStream os = sendSock.getOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(os);

					/* Write the object into the socket */
					oos.writeObject(sendPkt);

					/* Close */
					oos.close();
					os.close();
					sendSock.close();

					/* Dummy return */
					return null;
					
				} else { /* If not connected to the ferry, store it in the list */
					if (availableBuffer - sendPkt.toString().length() > 0
							&& sendPkt != null) {
						outputQueue.add(sendPkt);
					}
					return null;
				}
				
			} catch (Exception e) {

				/* Store the packet for later transmission if something goes wrong */
				if (availableBuffer - sendPkt.toString().length() > 0
						&& sendPkt != null) {
					outputQueue.add(sendPkt);
				}
				
				e.printStackTrace();
				
				return null;
			}
		}
	}

	public class RecieveHelper extends AsyncTask<Void, Integer, LlPacket> {
		@Override
		protected LlPacket doInBackground(Void... params) {

			/* Creating a new variable for receiving is necessary */
			LlPacket sendToMl = new LlPacket();

			try {
				
				/* Get a port to Listen on */
				if (servSock == null) {
					servSock = new ServerSocket(port);
				}
				
				/* Start Listening */
				Socket receiveSock = servSock.accept();
				
				/* A Packet has arrived, read it! */
				InputStream is = receiveSock.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				LlPacket recvPkt = (LlPacket) ois.readObject();


				/* See if the ID matches the current node ID */
				if (recvPkt != null && recvPkt.toID == nodeID) {
					
					/* Copy Packet to be returned */
					sendToMl = recvPkt;

				/* Else this is a wrongly delivered packet. Return null */
				} else {
					// Do nothing!
				}

				/* Close Sockets */
				is.close();
				receiveSock.close();

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			/* Send it to the middle layer */
			return sendToMl;
		}

	}

	/*
	 * This is function runs in the background waiting to send out
	 * packets in the output queue/buffer
	 */
	private class QueueHandler implements Runnable {
		// @Override
		
		/* To detect if we are meeting a new ferry */
		boolean inRange = false;
		
		public void run() {
			while (true) {
				try {
					Thread.sleep(2 * 1000);
					String SSID = wifiManager.getConnectionInfo().getSSID();
					if (SSID.equalsIgnoreCase(WiFiUtility.networkSSID)) {
						
						Iterator<LlPacket> queueIterator = outputQueue
								.iterator();

						if (inRange == false && location != null) {
							LlPacket sendPkt = new LlPacket();
							sendPkt.fromID = LowerLayer.nodeID;
							sendPkt.toID = 0;
							sendPkt.type = 1;
							sendPkt.payload = Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());

							/* Create and prepare sending socket */
							String ipAddr = intToIp(wifiManager.getDhcpInfo().gateway);
							Socket sendSock = new Socket(ipAddr, port);
							OutputStream os = sendSock.getOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(os);
							oos.writeObject(sendPkt);
							
							/* Close */
							oos.close();
							os.close();
							sendSock.close();

							inRange = true;
						}
						
						/* Send all the piled up data */
						while (queueIterator.hasNext()) {
							
							/* Get the packet from the buffer queue */
							LlPacket out = queueIterator.next();
							
							/* Create and prepare sending socket */
							String ipAddr = intToIp(wifiManager.getDhcpInfo().gateway);
							Socket sendSock = new Socket(ipAddr, port);
							OutputStream os = sendSock.getOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(os);
							oos.writeObject(out);
							
							/* Close */
							oos.close();
							os.close();
							sendSock.close();
							queueIterator.remove();
							availableBuffer += out.toString().length();
						}
					} else {
						
						/* The ferry came in came in contact and is now gone :( */
						inRange = false;
						continue;
					}
				} catch (Exception e) {
					inRange = false;
					e.printStackTrace();
				}
			}
			
			/*
			 * IntentFilter intentFilter = new IntentFilter();
			 * intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			 * Intent intent = new Intent();
			 * 
			 * try { if
			 * (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION
			 * )) { WifiInfo wifiInfo =
			 * intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO); if
			 * (wifiInfo.getSSID() == WiFiUtility.networkSSID) { // Wifi is
			 * connected Log.d("Inetify", "Wifi is connected: "); } } } catch
			 * (Exception e) { e.printStackTrace(); }
			 */
		}
	}

	/* Helper function for Int to IP address converter */
	private String intToIp(int ipAddrs) {

		return ((ipAddrs & 0xFF) + "." + ((ipAddrs >> 8) & 0xFF) + "."
				+ ((ipAddrs >> 16) & 0xFF) + "." + ((ipAddrs >> 24) & 0xFF));
	}

}