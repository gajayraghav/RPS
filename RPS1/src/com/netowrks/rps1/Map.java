package com.netowrks.rps1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.netowrks.rps1.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Map extends Activity implements OnClickListener, OnItemSelectedListener {

	ImageView img;
	Button bNavigateTo, bRefresh;
	Spinner spPhoneNumbers;
	HashMap<Long, String> GPSData3 = new HashMap<Long, String>();
	HashMap<Long, String> GPSData2 = new HashMap<Long, String>();
	HashMap<Long, String> GPSData1 = new HashMap<Long, String>();
	LinkedList<Long> PhoneNumbers = new LinkedList<Long>();// = new List<Integer>();
	LinkedList<String> GPSStrings = new LinkedList<String>();
	LinkedList<Float> Lati = new LinkedList<Float>();
	LinkedList<Float> Longi = new LinkedList<Float>();
	LinkedList<Float> X = new LinkedList<Float>();
	LinkedList<Float> Y = new LinkedList<Float>();
	
	
//	private boolean numberSelected = false;
	private boolean spinnerfill = false;
	Paint p;
	int screenwidth, screenheight, scalefactor;
	Canvas c;
	Bitmap bmp;
	// GPSTracker class
	GPSTracker gps;

	private String myPhoneNumber;
	long selectedPhoneNumber;
	int selectedIndex;
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		myPhoneNumber = getIntent().getStringExtra("myPhone");
		selectedIndex = -1;
		//Toast.makeText(getApplicationContext(), "My Phone-"+myPhoneNumber, Toast.LENGTH_SHORT).show();
		setTitle("- Map");
		bNavigateTo = (Button) findViewById(R.id.bGONavi);
		bNavigateTo.setOnClickListener(this);
		// convertGPStoXY();
		Display display = getWindowManager().getDefaultDisplay();
		Point pt = new Point();
		display.getSize(pt);
		screenwidth = pt.x;
		screenheight = pt.y;
		if (screenwidth < screenheight)
			scalefactor = screenwidth;
		else
			scalefactor = screenheight;

		spPhoneNumbers = (Spinner) findViewById(R.id.spinPhoneNumbers);
		spPhoneNumbers.setOnItemSelectedListener(this);
		
		img = (ImageView) findViewById(R.id.imgMAP);
		bmp = Bitmap.createBitmap(scalefactor, scalefactor, Config.RGB_565);
		c = new Canvas(bmp);
		c.drawColor(Color.BLACK);

		p = new Paint();

		Drawable drawable = new BitmapDrawable(getResources(), bmp);
		// img.setBackground(drawable);
		img.setBackgroundColor(Color.BLACK);

		bRefresh = (Button) findViewById(R.id.bRefreshMap);
		bRefresh.setOnClickListener(this);

		renderMap();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bGONavi:
			selectedPhoneNumber = 0;
			Intent intNavigate = new Intent(this, Navigate.class);
			if (spPhoneNumbers == null)
				return;
			if (spPhoneNumbers.getCount() <= 0) {
				Toast.makeText(getApplicationContext(),
						"Cant Naviage w/o data ", Toast.LENGTH_SHORT).show();
				return;
			}

			if (spPhoneNumbers != null)
				if (spPhoneNumbers.getCount() > 0) {
					if (spPhoneNumbers.getSelectedItem() != null) {
						selectedPhoneNumber = Long.parseLong(spPhoneNumbers
								.getSelectedItem().toString());
					}

				}
			intNavigate.putExtra("Phone", Long.parseLong(spPhoneNumbers
					.getSelectedItem().toString()));

			String pos1 = String.valueOf(Math.random() + ";"
					+ String.valueOf(Math.random()));
			String pos2 = String.valueOf(Math.random() + ";"
					+ String.valueOf(Math.random()));
			
			intNavigate.putExtra("myLoc", Singleton.getLocation_3(Long.parseLong(myPhoneNumber)));
			intNavigate.putExtra("Position1", pos1);
//					Singleton.getLocation_1(selectedPhoneNumber));
			intNavigate.putExtra("Position2", pos2);
//					Singleton.getLocation_2(selectedPhoneNumber));
			intNavigate.putExtra("Position3",
					Singleton.getLocation_3(selectedPhoneNumber));
			Toast.makeText(
					getApplicationContext(),
					"Navigate " + selectedPhoneNumber + ","
							+ Singleton.getLocation_1(selectedPhoneNumber)
							+ ","
							+ Singleton.getLocation_2(selectedPhoneNumber)
							+ ","
							+ Singleton.getLocation_3(selectedPhoneNumber),
					Toast.LENGTH_SHORT).show();
			/*
			 * intNavigate.putExtra("Position2", "-35.7872128;-84.4046034");
			 * intNavigate.putExtra("Position3", "35.7872128;84.4046034");
			 */
			this.startActivity(intNavigate);
			break;
		case R.id.bRefreshMap:
			spinnerfill = true;
			stubGPS();
			renderMap();
			break;
		}
	}

	private void renderMap() {

		
		GPSData3 = Singleton.getHashMap_3();
		GPSData2 = Singleton.getHashMap_2();
		GPSData1 = Singleton.getHashMap_1();
		// Toast.makeText(getApplicationContext(),
		// "Put "+selectedPhoneNumber+","+Singleton.getLocation_1(selectedPhoneNumber)+","+Singleton.getLocation_2(selectedPhoneNumber)+","+Singleton.getLocation_3(selectedPhoneNumber),
		// Toast.LENGTH_SHORT).show();
		gps = new GPSTracker(this);

		if (GPSData3.size() == 0) {
			Toast.makeText(getApplicationContext(), "No GPS data Yet !",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(getApplicationContext(), "GPS Size "+GPSData3.size(), Toast.LENGTH_SHORT).show();
/*		if(PhoneNumbers != null)
		{
			PhoneNumbers.clear();
			GPSStrings.clear();
		}
*/		X.clear();
		Y.clear();
		Longi.clear();
		Lati.clear();
		PhoneNumbers.clear();
		GPSStrings.clear();
		PhoneNumbers.addAll(GPSData3.keySet());// = (LinkedList<Long>) GPSData3.keySet();
		GPSStrings.addAll(GPSData3.values());// = (LinkedList<String>) GPSData3.values();
		
		if(GPSStrings.size() != PhoneNumbers.size())
		{
			Toast.makeText(getApplicationContext(), "PhoneNumber.size != GPS.size", Toast.LENGTH_SHORT).show();
		}
		/*for(int ll =0; ll<PhoneNumbers.size();ll++)
		{
			Log.v("Phone", PhoneNumbers.get(ll).toString());
		}*/
		int indexOfMyPhone = PhoneNumbers.indexOf(Long.parseLong(myPhoneNumber));
		if(indexOfMyPhone<0)
		{
			Toast.makeText(getApplicationContext(), "No Phone in "+PhoneNumbers.size(), Toast.LENGTH_SHORT).show();
		}
		
		
		if(spinnerfill == true)
		{
		//	spinnerfill = true;
			ArrayAdapter<Long> adapterPhoneNumbers = new ArrayAdapter<Long>(
					this, android.R.layout.simple_list_item_single_choice,
					PhoneNumbers);
			spPhoneNumbers.setAdapter(adapterPhoneNumbers);	
		
	/*		numberSelected = false;
			spinnerfill = true;
	*/	}

		String temp[] = new String[2];
		for (String s : GPSStrings) {
			temp = s.split(";");
			Lati.add(-Float.parseFloat(temp[0]));
			Longi.add(Float.parseFloat(temp[1]));

		}
		// check if GPS enabled
		/* if (gps.canGetLocation()) */{

			/*
			 * double latitude = (double) gps.getLatitude(); double longitude =
			 * (double) gps.getLongitude();
			 * 
			 * // \n is for new line Toast.makeText( getApplicationContext(),
			 * "Your Location is - \nLat: " + latitude + "\nLong: " + longitude,
			 * Toast.LENGTH_SHORT).show(); Lati.add((float) (-latitude));
			 * Longi.add((float) (longitude));
			 * System.out.println("GPS at Lo-"+longitude +" La-"+latitude);
			 */
			convertGPStoXY();

			Drawable loc = getResources().getDrawable(R.drawable.loc);
			Resources res = getResources();
			Bitmap location_img = BitmapFactory.decodeResource(res,
					R.drawable.loc);
			Bitmap mylocation_img = BitmapFactory.decodeResource(res,
					R.drawable.loc_me);
			Bitmap selectedlo_img = BitmapFactory.decodeResource(res,
					R.drawable.loc_sel);
			

			float movelocimg = (float)(3.0 * scalefactor / 100);
			c.drawColor(Color.BLACK);
			p.setColor(Color.CYAN);
			p.setTextSize(30);
			for (int index = 0; index < X.size(); index++) {
				if(index == indexOfMyPhone)
				{
					c.drawBitmap(mylocation_img, (X.get(index) - movelocimg),
							(Y.get(index) - movelocimg), p);
					c.drawText("" + (index+1), (X.get(index) - movelocimg) + 5,
							(Y.get(index) - movelocimg), p);					
				}
				else if (index == selectedIndex)
				{
					c.drawBitmap(selectedlo_img, (X.get(index) - movelocimg),
							(Y.get(index) - movelocimg), p);
					c.drawText("" + (index+1), (X.get(index) - movelocimg) + 5,
							(Y.get(index) - movelocimg), p);
					
				}
				else
				{
					c.drawBitmap(location_img, (X.get(index) - movelocimg),
							(Y.get(index) - movelocimg), p);
					c.drawText("" + (index+1), (X.get(index) - movelocimg) + 5,
							(Y.get(index) - movelocimg), p);
				}

				System.out.println("Plotting at X-"
						+ (X.get(index) - movelocimg) + " Y-"
						+ (Y.get(index) - movelocimg));
			}
			if (X.size() <= 0) {
				Toast.makeText(getApplicationContext(),
						"No GPS data to be plotted", Toast.LENGTH_SHORT).show();
			}
			Drawable drawable = new BitmapDrawable(getResources(), bmp);
			img.setBackground(drawable);
			spinnerfill = false;

		}

	}

	private void convertGPStoXY() {
		// TODO Auto-generated method stub
		float maxLati = Collections.max(Lati);
		float minLati = Collections.min(Lati);
		float maxLongi = Collections.max(Longi);
		float minLongi = Collections.min(Longi);
		float temp;
		X.clear();
		Y.clear();
		for (float f : Longi) {
			if (maxLongi == minLongi) {
				temp = 0.0f;
			} else {
				temp = ((f - minLongi) / (maxLongi - minLongi)) * scalefactor; // .max((maxLongi
																				// -
																				// minLongi),(minLongi-maxLongi)))
																				// *
																				// scalefactor;
			}
			X.add(temp);
			System.out.println("Longi~~" + temp);
		}

		for (float f : Lati) {
			if (maxLati == minLati) {
				temp = 0.0f;
			} else {
				temp = ((f - minLati) / (maxLati - minLati)) * scalefactor;// Math.max((maxLongi
																			// -
																			// minLongi),(minLongi-maxLongi)))
																			// *
																			// scalefactor;
			}
			Y.add(temp);
			System.out.println("Lati~~" + temp);
		}
		System.out.println("Count X" + X.size() + " Y" + Y.size());
	}

	void stubGPS() {
		long PhoneMask = 14049160122L;
		Singleton.clearAll(3);
		for (int i = 0; i < 15; i++) {
			Singleton.addData(
					(i + PhoneMask),
					String.valueOf(Math.random() + ";"
							+ String.valueOf(Math.random())));
		}
		//spinnerfill = false;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View arg1, int index,
			long value) {
		// TODO Auto-generated method stub
		/*if(spinnerfill == true)
		{
			Toast.makeText(getApplicationContext(), "Filled", Toast.LENGTH_SHORT).show();
			spinnerfill = false;
			
			return;
		}*/
		View v = (parent.getChildAt(0)); //.setTextColor(Color.BLUE);
		TextView tv = (TextView) v;
		tv.setTextColor(Color.CYAN);
		Toast.makeText(getApplicationContext(), "Selected "+index, Toast.LENGTH_SHORT).show();
		//if(spinnerfill == false)
		if(selectedIndex != index)
		{
			
		//	numberSelected = true;
			c.drawColor(Color.DKGRAY);
			selectedIndex = index;
			renderMap();
		}
		//spinnerfill = false;
		//renderMap();
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Selected Nothing", Toast.LENGTH_SHORT).show();
		
	}
}
