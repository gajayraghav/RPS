package com.netowrks.rps1;

import java.util.HashMap;

public class Singleton {

	private static Singleton singleton = new Singleton();
	private static HashMap<Integer, String> gpsList_1 = new HashMap<Integer, String>();
	private static HashMap<Integer, String> gpsList_2 = new HashMap<Integer, String>();
	private static HashMap<Integer, String> gpsList_3 = new HashMap<Integer, String>();

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
	public static void addData(int ph, String loc)
	{
		gpsList_3.put(ph, loc);
	}

	public void putHashMap(HashMap<Integer, String> payload) {
		gpsList_1 = gpsList_2;
		gpsList_2 = gpsList_3;
		gpsList_3 = payload;
	}

	/* Function to return the GPS List #3 */
	public static HashMap<Integer, String> getHashMap_3() {
		return gpsList_3;
	}

	/* Function to return the GPS List #2 */
	public static HashMap<Integer, String> getHashMap_2() {
		return gpsList_2;
	}

	/* Function to return the GPS List #1 */
	public static HashMap<Integer, String> getHashMap_1() {
		return gpsList_1;
	}

	/* Function to find the last known location of a node ID */
	public static String getLocation_3(Integer key) {
		return gpsList_3.get(key);
	}

	/* Function to find the second last known location of a node ID */
	public static String getLocation_2(Integer key) {
		return gpsList_2.get(key);
	}

	/* Function to find the third last known location of a node ID */
	public static String getLocation_1(Integer key) {
		return gpsList_1.get(key);
	}

}
