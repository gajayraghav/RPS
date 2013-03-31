package com.netowrks.rps1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class LowerLayer {

	/* Node details */
	static public String nodeID = "2533";
	static public String ferryID = "0";
	final int port = 8888;

	/* Listening Socket */
	private ServerSocket servSock;

	/* Buffer related parameters */
	static int availableBuffer = 10000000; // 10MB
	static int sizeOfDataSent = 0;
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
			
			/* Not sure how to check for an error here */
			nodeID = getNodeIDfromxml();			

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

	public boolean send (LlPacket sendPkt) {
		SendHelper sendHelper = new SendHelper();
		/*
		 * Fill the outgoing packet : Note - this needs to come above
		 * the socket creation part
		 */
		sendPkt.fromID = nodeID;
		sendPkt.toID = ferryID;
		int objSize = sendPkt.toString().length();
		if (objSize > availableBuffer) {
			return false;
		} else {
			availableBuffer -= objSize;
			sendHelper.execute(sendPkt);
			return true;
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
				sendPkt = params[0];

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

					/* Update the sent data size keeper */
					sizeOfDataSent += sendPkt.toString().length();
					
					/* Dummy return */
					return null;

				} else { /* If not connected to the ferry, store it in the list */
					outputQueue.add(sendPkt);
					return null;
				}

			} catch (Exception e) {
				outputQueue.add(sendPkt);
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
			sendToMl.type = -1;
			
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
				if (recvPkt != null && recvPkt.toID.equals(nodeID)) {

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
	 * This is function runs in the background waiting to send out packets in
	 * the output queue/buffer
	 */
	private class QueueHandler implements Runnable {
		// @Override

		/* To detect if we are meeting a new ferry */
		boolean locationSent = false;
		boolean inRange = false;
		
		public void run() {
			while (true) {
				try {
					Thread.sleep(2 * 1000);
					String SSID = wifiManager.getConnectionInfo().getSSID();
					if (SSID.equalsIgnoreCase(WiFiUtility.networkSSID)) {

						Iterator<LlPacket> queueIterator = outputQueue
								.iterator();

						if (locationSent == false && location != null) {
							LlPacket sendPkt = new LlPacket();
							sendPkt.fromID = LowerLayer.nodeID;
							sendPkt.toID = ferryID;
							sendPkt.type = 1;
							sendPkt.payload = Double.toString(location
									.getLatitude())
									+ ","
									+ Double.toString(location.getLongitude());

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

							locationSent = true;
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
							sizeOfDataSent += out.toString().length();
						}
						
						/* if all the above happens smoothly, then the node is in range */
						inRange = true;
						
					} else {
						
						/* The ferry came in contact and is now gone :( */
						if (inRange == true) {
							/* Update the available buffer */
							availableBuffer += sizeOfDataSent;
							/* Reset the send size keeper */
							sizeOfDataSent = 0;
							/* Resend location when ferry comes in contact again */
							locationSent = false;
							/* Ferry not in range any more */
							inRange = false;
						}

						continue;
					}
				} catch (Exception e) {
					/* The ferry came in contact and is now gone :( (known thru socket error) */
					if (inRange == true) {
						/* Update the available buffer */
						availableBuffer += sizeOfDataSent;
						/* Reset the send size keeper */
						sizeOfDataSent = 0;
						/* Resend location when ferry comes in contact again */
						locationSent = false;
						/* Ferry not in range any more */
						inRange = false;
					}
					e.printStackTrace();
				}
			}
		}
	}

	/* Helper function for Int to IP address converter */
	private String intToIp(int ipAddrs) {

		return ((ipAddrs & 0xFF) + "." + ((ipAddrs >> 8) & 0xFF) + "."
				+ ((ipAddrs >> 16) & 0xFF) + "." + ((ipAddrs >> 24) & 0xFF));
	}
	
	/* Function to parse the NodeID - Provided by Shriram */
	public static String getNodeIDfromxml() throws IOException{
    	String s="";
    	String checker ="<";
    	String FinalNodeID="";
    	int flag = 0;
    	
		try {
			BufferedReader buf = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory() + File.separator + "registration.xml"));
				while((s = buf.readLine())!=null){
				
				String[] tokens =  s.split("<NodeID>") ;
				for(int i = 0 ; i < tokens.length ; i ++){
					if(!tokens[i].startsWith(checker))
					{
						//System.out.println(tokens[i]);
						//FinalNodeID=tokens[i];
						for(int j=0;j<tokens[i].length();j++)
						{
							if(tokens[i].charAt(j)!='<')
							{
							//System.out.print(tokens[i].charAt(j));
							FinalNodeID=FinalNodeID.concat(Character.toString(tokens[i].charAt(j)));
							}
							else
							{
								flag=1;
								break;
							}
						}
					}
				}
		}
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return FinalNodeID;	
	}
}