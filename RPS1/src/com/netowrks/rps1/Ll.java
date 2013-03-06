package com.netowrks.rps1;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;

public class Ll extends Utility {

	private int nodeID = 2533;
	private int bcastAddr = 99999;
	private int myPort = 8888;
	private ServerSocket servSock;
	
	Ll () {
		try {
			servSock = new ServerSocket(myPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Ll_close () {
		try {
             // make sure you close the socket upon exiting
			if (servSock != null) {
				servSock.close();
			}
        } catch (IOException e) {
             e.printStackTrace();
        }
	}
	
	public class send extends AsyncTask<LlMl_comm, Void, Void> {
		protected Void doInBackground(LlMl_comm... params) {
			try {
				LlMl_comm in = params[0];
				
				/* Create and prepare sending socket */
				Socket sendSock = new Socket(in.ipAddr, in.port);
				OutputStream os = sendSock.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);
				
				/* Create and fill the outgoing packet */
				packetFormat outYouGo = new packetFormat();
				outYouGo.Buff = in.Buff;
				outYouGo.ID = nodeID;
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

	public class receive extends AsyncTask<Void, Integer, LlMl_comm> {
		@Override
		protected LlMl_comm doInBackground(Void... params) {

			LlMl_comm sendToMl = new LlMl_comm();
			try {
				if (servSock == null) {
					servSock = new ServerSocket(myPort);
				}
				Socket receiveSock = servSock.accept();
				InputStream is = receiveSock.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				packetFormat inYouCome = (packetFormat) ois.readObject();
				sendToMl.Buff = inYouCome.Buff;

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