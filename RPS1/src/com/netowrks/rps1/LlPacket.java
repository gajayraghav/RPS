package com.netowrks.rps1;

import java.io.Serializable;

import com.rps.utilities.PacketTypes;

@SuppressWarnings("serial")
public class LlPacket implements Serializable {
	String fromID; /* This is the ID of the sender (nodeID) */
	String toID; /* This is the ID of the receiver, which is mostly "0" denoting ferry */
	String Recv_No; /* The phone number of the receiver */
	int type; /* Identifier for the type of payload */
	Object payload;	/* The actual content */
}
