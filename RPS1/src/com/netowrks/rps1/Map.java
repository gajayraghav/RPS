package com.netowrks.rps1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Map extends Activity implements OnClickListener {

	ImageView img;
	Button bNavigateTo, bRefresh;
	Spinner spPhoneNumbers;
	HashMap<Integer, String> GPSData = new HashMap<Integer, String>();
	ArrayList<Integer> PhoneNumbers;// = new ArrayList<Integer>();
	ArrayList<String> GPSStrings;
	ArrayList<Float> Lati = new ArrayList<Float>();
	ArrayList<Float> Longi = new ArrayList<Float>();
	ArrayList<Float> X = new ArrayList<Float>();
	ArrayList<Float> Y = new ArrayList<Float>();
	Paint p;
	int screenwidth, screenheight, scalefactor;
	Canvas c;
	Bitmap bmp;
	// GPSTracker class
	GPSTracker gps;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
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
		img = (ImageView) findViewById(R.id.imgMAP);
		bmp = Bitmap.createBitmap(scalefactor, scalefactor, Config.RGB_565);
		c = new Canvas(bmp);
		c.drawColor(Color.WHITE);

		p = new Paint();

		Drawable drawable = new BitmapDrawable(getResources(), bmp);
//		img.setBackground(drawable);
		img.setBackgroundColor(Color.WHITE);

		bRefresh = (Button) findViewById(R.id.bRefreshMap);
		bRefresh.setOnClickListener(this);

		//renderMap();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bGONavi:
			Toast.makeText(
					getApplicationContext(),
					"Navigate to" + spPhoneNumbers.getSelectedItem().toString(),
					Toast.LENGTH_SHORT).show();
			Intent intNavigate = new Intent(this, Navigate.class);
			intNavigate.putExtra("Phone", Integer.parseInt(spPhoneNumbers
					.getSelectedItem().toString()));
			/*intNavigate.putExtra("Position1", Singleton.getLocation_1(Integer
					.parseInt(spPhoneNumbers.getSelectedItem().toString())));
			
			intNavigate.putExtra("Position2", Singleton.getLocation_2(Integer
					.parseInt(spPhoneNumbers.getSelectedItem().toString())));
			intNavigate.putExtra("Position3", Singleton.getLocation_3(Integer
					.parseInt(spPhoneNumbers.getSelectedItem().toString())));
			*
			*/
			intNavigate.putExtra("Position1", "35.7872128;-84.4046034");
			intNavigate.putExtra("Position2", "-35.7872128;-84.4046034");
			intNavigate.putExtra("Position3", "35.7872128;84.4046034");
			
			this.startActivity(intNavigate);
			break;
		case R.id.bRefreshMap:
			stubGPS();
			renderMap();
			break;
		}
	}

	private void renderMap() {

		GPSData = Singleton.getHashMap_3();
		PhoneNumbers = new ArrayList<Integer>(GPSData.keySet());
		GPSStrings = new ArrayList<String>(GPSData.values());
		spPhoneNumbers = (Spinner) findViewById(R.id.spinPhoneNumbers);
		ArrayAdapter<Integer> adapterPhoneNumbers = new ArrayAdapter<Integer>(
				this, android.R.layout.simple_list_item_single_choice,
				PhoneNumbers);
		spPhoneNumbers.setAdapter(adapterPhoneNumbers);
		gps = new GPSTracker(this);
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

			int movelocimg = 3 * scalefactor / 100;
			c.drawColor(Color.WHITE);
			p.setColor(Color.BLACK);
			p.setTextSize(40);
			for (int index = 0; index < X.size(); index++) {
				c.drawBitmap(location_img, (X.get(index) - movelocimg),
						(Y.get(index) - movelocimg), p);
				c.drawText("" + index, (X.get(index) - movelocimg) + 5,
						(Y.get(index) - movelocimg), p);

				System.out.println("Plotting at X-"
						+ (X.get(index) - movelocimg) + " Y-"
						+ (Y.get(index) - movelocimg));
			}

			Drawable drawable = new BitmapDrawable(getResources(), bmp);
			img.setBackground(drawable);

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
	
	void stubGPS()
	{
		for(int i = 0; i<10;i++)
		{
			Singleton.addData(i+4004004, String.valueOf(Math.random()+";"+String.valueOf(Math.random())));
		}
	}
}
