package com.netowrks.rps1;

import java.util.List;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class Utility extends Activity {

	static boolean wifiStatus = false;

	static String networkSSID = "HelloWorld";
	static String networkPass = "TargetPublix";

	public void ConnectTo(WifiManager wifiManager) {
		WifiInfo conn_info = wifiManager.getConnectionInfo();

		if (conn_info.getSSID() == null
				|| !conn_info.getSSID().equals("\"" + networkSSID + "\"")) {
			WifiConfiguration conf = new WifiConfiguration();

			// String should contain ssid in quotes
			conf.SSID = "\"" + networkSSID + "\""; 
			// conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			// conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			conf.preSharedKey = "\"" + networkPass + "\"";
			conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

			wifiManager.addNetwork(conf);
			wifiManager.disconnect();
			wifiManager.enableNetwork(getOurNetID(wifiManager), true);
			wifiManager.reconnect();
		}

		wifiStatus = true;
	}

	public void toggleWifi(WifiManager wifiManager) {
		if (wifiStatus == true) {
			int netID = getOurNetID(wifiManager);
			wifiManager.removeNetwork(netID);
			wifiManager.disableNetwork(netID);
			wifiManager.disconnect();
			wifiStatus = false;
		} else {
			ConnectTo(wifiManager);
		}
	}

	private int getOurNetID(WifiManager wifiManager) {
		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration i : list) {
			if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
				return i.networkId;
			}
		}
		return -1;
	}

	public class LlMl_comm {
		String NP;
		int Recv_ID;
		int type;
		String Buff;
		String ipAddr;
		int port;
		String Comment;
	}

}
