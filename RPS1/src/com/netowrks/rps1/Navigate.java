package com.netowrks.rps1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Navigate extends Activity implements OnClickListener {

	ImageView imgBearing;
	ImageView imgNavigateMap;
	TextView lPhoneNumber;
	SensorManager mSensorManager;
	LocationManager locationManager;
	Navigation Navi;
	SharedPreferences settings;
	Bitmap compassImageNorth;
	Bitmap compassImageNorthEast;
	Bitmap compassImageNorthWest;
	Bitmap compassImageEast;
	Bitmap compassImageWest;
	Bitmap compassImageSouth;
	Bitmap compassImageSouthEast;
	Bitmap compassImageSouthWest;
	Button bNavi, bRefresh;
	TextView lDistance;

	// /////////
	ArrayList<Float> Lati = new ArrayList<Float>();
	ArrayList<Float> Longi = new ArrayList<Float>();
	ArrayList<Float> X = new ArrayList<Float>();
	ArrayList<Float> Y = new ArrayList<Float>();
	Paint p;
	int screenwidth, screenheight, scalefactor;
	Canvas c;
	Bitmap bmp;
	ArrayList<String> positions = new ArrayList<String>();
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navigate);

		imgBearing = (ImageView) findViewById(R.id.imgbearing);
		compassImageNorth = BitmapFactory.decodeResource(getResources(),
				R.drawable.compass_n);
		compassImageNorthEast = BitmapFactory.decodeResource(getResources(),
				R.drawable.compass_ne);
		compassImageNorthWest = BitmapFactory.decodeResource(getResources(),
				R.drawable.compass_nw);
		compassImageEast = BitmapFactory.decodeResource(getResources(),
				R.drawable.compass_e);
		compassImageWest = BitmapFactory.decodeResource(getResources(),
				R.drawable.compass_w);
		compassImageSouth = BitmapFactory.decodeResource(getResources(),
				R.drawable.compass_s);
		compassImageSouthEast = BitmapFactory.decodeResource(getResources(),
				R.drawable.compass_se);
		compassImageSouthWest = BitmapFactory.decodeResource(getResources(),
				R.drawable.compass_sw);

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		settings = getPreferences(Context.MODE_PRIVATE);
		Navi = new Navigation(locationManager, mSensorManager, settings,
				getApplicationContext());
		Navi.onCreate(imgBearing, compassImageNorth, compassImageNorthEast,
				compassImageNorthWest, compassImageEast, compassImageWest,
				compassImageSouth, compassImageSouthEast, compassImageSouthWest);

		bRefresh = (Button) findViewById(R.id.bRefreshNavigation);
		bNavi = (Button) findViewById(R.id.bNavi);
		
		//Navi.onSetLocation(0.0, 0.0);
		bNavi.setOnClickListener(this);
		bRefresh.setOnClickListener(this);
		Integer phone = getIntent().getIntExtra("Phone", 0); 
		positions.add(getIntent().getStringExtra("Position1"));
		positions.add(getIntent().getStringExtra("Position2"));
		positions.add(getIntent().getStringExtra("Position3"));
		String temp[] = new String[2];
		temp = positions.get(positions.size()-1).split(";");

		lPhoneNumber = (TextView) findViewById(R.id.lPhoneNumber);
		lPhoneNumber.setText(phone.toString());

		Navi.onSetLocation(Double.parseDouble(temp[1]),
				Double.parseDouble(temp[0]));
		Navi.updateDesiredLocation();
		Toast.makeText(getApplicationContext(), "Navigate to " + positions.get(2),
				Toast.LENGTH_SHORT).show();
		
		lDistance = (TextView) findViewById(R.id.lNaviDistance);
		lDistance.setText("Distance = "+Navi.tvDistance);
		imgNavigateMap = (ImageView) findViewById(R.id.imgNavigateMap);

		// //////

		Display display = getWindowManager().getDefaultDisplay();
		Point pt = new Point();
		display.getSize(pt);
		screenwidth = pt.x;
		screenheight = pt.y;
		if (screenwidth < screenheight)
			scalefactor = screenwidth;
		else
			scalefactor = screenheight;
		imgNavigateMap = (ImageView) findViewById(R.id.imgNavigateMap);
		bmp = Bitmap.createBitmap(scalefactor, scalefactor, Config.RGB_565);
		c = new Canvas(bmp);
		c.drawColor(Color.WHITE);

		p = new Paint();

		Drawable drawable = new BitmapDrawable(getResources(), bmp);
		imgNavigateMap.setBackground(drawable);
		imgNavigateMap.setBackgroundColor(Color.WHITE);
		renderMap();

	}

	@Override
	protected void onResume() {
		Navi.onResume();
		super.onResume();
	}

	@Override
	protected void onStop() {
		Navi.onStop();
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.bNavi) {
			/*
			 * Navi.onSetLocation(Double.parseDouble(lati.getText().toString()),
			 * Double.parseDouble(lati.getText().toString()));
			 * Navi.updateDesiredLocation();
			 */}
	}

	private void renderMap() {

		String temp[] = new String[2];
		for(String s:positions)
		{
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
			imgNavigateMap.setBackground(drawable);
			lDistance.setText("Distance = "+Navi.tvDistance);


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
		for (int i = 0; i < 10; i++) {
			Singleton.addData(
					i + 4004004,
					String.valueOf(Math.random() + ";"
							+ String.valueOf(Math.random())));
		}
	}

}
