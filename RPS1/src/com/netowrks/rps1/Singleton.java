package com.netowrks.rps1;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Singleton {

	private static Singleton singleton = new Singleton();
	private static LinkedHashMap<Long, String> gpsList_1 = new LinkedHashMap<Long, String>();
	private static LinkedHashMap<Long, String> gpsList_2 = new LinkedHashMap<Long, String>();
	private static LinkedHashMap<Long, String> gpsList_3 = new LinkedHashMap<Long, String>();

	/* A private Constructor prevents any other class from instantiating */
	private Singleton() {
	}

	/* Static 'instance' method */
	public static Singleton getInstance() {
		return singleton;
	}

	/*
	 * Function to store the hashmap given by ferry - store the last three
	 * hashmaps
	 */
	
	public static void clearAll(int index)
	{
		switch(index)
		{
		case 1:
			gpsList_1.clear();
			break;
		case 2:
			gpsList_2.clear();
			break;
		case 3:
			gpsList_3.clear();
			break;
		}
	}
	
	public static void addData(long ph, String loc)
	{
		gpsList_3.put(ph, loc);
	}

	public void putHashMap(LinkedHashMap<Long, String> payload) {
		gpsList_1 = gpsList_2;
		gpsList_2 = gpsList_3;
		gpsList_3 = payload;
	}

	/* Function to return the GPS List #3 */
	public static HashMap<Long, String> getHashMap_3() {
		return gpsList_3;
	}

	/* Function to return the GPS List #2 */
	public static HashMap<Long, String> getHashMap_2() {
		return gpsList_2;
	}

	/* Function to return the GPS List #1 */
	public static HashMap<Long, String> getHashMap_1() {
		return gpsList_1;
	}

	/* Function to find the last known location of a node ID */
	public static String getLocation_3(Long key) {
		return gpsList_3.get(key);
	}

	/* Function to find the second last known location of a node ID */
	public static String getLocation_2(Long key) {
		return gpsList_2.get(key);
	}

	/* Function to find the third last known location of a node ID */
	public static String getLocation_1(Long key) {
		return gpsList_1.get(key);
	}

}
