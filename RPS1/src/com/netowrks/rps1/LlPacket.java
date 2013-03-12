package com.netowrks.rps1;

import java.io.Serializable;

import com.rps.utilities.PacketTypes;

@SuppressWarnings("serial")
public class LlPacket implements Serializable {
	//Sender's node ID when node sends
	//Reciever's ID when ferry forwards - so that reciever can check if the packet is his
	String nodeID;
	PacketTypes type;
	Object payload;
	
//Following elements are just for testing purposes	
	int Recv_ID;
	String ipAddr;
	int port;
}
