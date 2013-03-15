package com.netowrks.rps1;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import android.os.AsyncTask;

public class LowerLayer {//extends WiFiUtility { // extends is not needed

	private int nodeID = 2533;
	private int bcastAddr = 99999;
	final int port = 8888;
	private ServerSocket servSock;
	
	LowerLayer () {
		try {
			servSock = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void Ll_close () {
		try {
             //make sure you close the socket upon exiting
			if (servSock != null) {
				servSock.close();
			}
        } catch (IOException e) {
             e.printStackTrace();
        }
	}
	
	public class SendHelper extends AsyncTask<LlPacket, Void, Void> {
		protected Void doInBackground(LlPacket... params) {
			try {
				LlPacket out = params[0];

				/* Create and prepare sending socket */
				Socket sendSock = new Socket(out.ipAddr, port);
				OutputStream os = sendSock.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);
				
				/* Create and fill the outgoing packet */
				/* Creating a new variable is necessary */
				LlPacket sendPkt = new LlPacket();
				sendPkt.fromID = nodeID;
				sendPkt.toID = out.toID;
				sendPkt.payload = out.payload;
				sendPkt.type = out.type;
				
				oos.writeObject(sendPkt);
				
				/* Close */
				oos.close();
				os.close();
				sendSock.close();
				
				return null;
			} catch (Exception e) {
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
				if (servSock == null) {
					servSock = new ServerSocket(port);
				}
				Socket receiveSock = servSock.accept();
				InputStream is = receiveSock.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				LlPacket recvPkt = (LlPacket) ois.readObject();

				/* Have commented out sections that do the validation etc */

				if (recvPkt != null && recvPkt.toID == bcastAddr) {
					sendToMl.type = recvPkt.type;
					sendToMl.payload = recvPkt.payload;

					// See if the ID matches the current node ID
				} else if (recvPkt != null && recvPkt.toID == nodeID) {
					sendToMl.type = recvPkt.type;
					sendToMl.payload = recvPkt.payload;

					// Else this is a wrongly delivered packet. Return null.
				} else {
				}

				is.close();
				receiveSock.close();

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			// Return according to what happened during packet reception.
			return sendToMl;
		}

	}
	
	private String intToIp(int ipAddrs) {

		return ((ipAddrs & 0xFF) + "." + ((ipAddrs >> 8) & 0xFF) + "."
				+ ((ipAddrs >> 16) & 0xFF) + "." + ((ipAddrs >> 24) & 0xFF));
	}

}