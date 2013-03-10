package com.netowrks.rps1;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class Navigate extends Activity implements OnClickListener {

	EditText longi, lati;
	ImageView imgBearing;
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
	Button bNavi;

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

		// bNavi = (Button) findViewById(R.id.bNavi);
		bNavi = (Button) findViewById(R.id.bNavi);
		longi = (EditText) findViewById(R.id.txtLongi);
		lati = (EditText) findViewById(R.id.txtLati);
		Navi.onSetLocation(0.0, 0.0);
		bNavi.setOnClickListener(this);
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
			Navi.onSetLocation(Double.parseDouble(lati.getText().toString()),
					Double.parseDouble(lati.getText().toString()));
			Navi.updateDesiredLocation();
		}
	}

}
