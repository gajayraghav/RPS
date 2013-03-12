package com.netowrks.rps1;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


import android.os.AsyncTask;

public class LowerLayer {//extends WiFiUtility { // extends is not needed

	private String nodeID = "2533";
	private int bcastAddr = 99999;
	private int myPort = 8888;
	private ServerSocket servSock;
	
	LowerLayer () {
		try {
			servSock = new ServerSocket(myPort);
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
				LlPacket in = params[0];
				
				/* Create and prepare sending socket */
				Socket sendSock = new Socket(in.ipAddr, in.port);
				OutputStream os = sendSock.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);
				
				/* Create and fill the outgoing packet */
				LlPacket outYouGo = new LlPacket();
				outYouGo.payload = (String) in.payload;
				outYouGo.nodeID = nodeID;
				oos.writeObject(outYouGo);
				
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

			LlPacket sendToMl = new LlPacket();
			try {
				if (servSock == null) {
					servSock = new ServerSocket(myPort);
				}
				Socket receiveSock = servSock.accept();
				InputStream is = receiveSock.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				LlPacket inputPacket = (LlPacket) ois.readObject();
				sendToMl.payload = inputPacket.payload;

				/* Have commented out sections that do the validation etc */
				/*
				if (inYouCome.ID == bcastAddr) {
					sendToMl.ipAddr = s.getRemoteSocketAddress().toString();
					sendToMl.NP = inYouCome.NP;
					sendToMl.type = inYouCome.type;
					sendToMl.Comment = "bCast";

					// See if the ID matches the current node ID
				} else if (inYouCome != null && inYouCome.ID == nodeID) {
					sendToMl.NP = inYouCome.NP;
					sendToMl.Buff = inYouCome.Buff;
					sendToMl.type = inYouCome.type;
					sendToMl.ipAddr = s.getRemoteSocketAddress().toString();
					sendToMl.Comment = "Unicast";

					// Else this is a wrongly delivered packet. Return null.
				} else {
				}
*/
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
	
}