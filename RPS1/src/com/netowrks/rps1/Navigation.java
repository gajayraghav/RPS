package com.netowrks.rps1;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Navigation {
	
	
	private LocationManager locationManager;
	private Location currentLocation;
	private Location desiredLocation;
	SharedPreferences settings;
	Context context;
	private double tvCurrentLat;
	private double tvCurrentLng;
	private double tvCurrentAccuracy;
	private double tvCurrentBearing;
	private double etDesiredLat;
	private double etDesiredLng;
	private double tvDistance;
	private double tvBearing;
	
	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean canGetLocation = false;

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
	
	private ImageView imgBearing;
	private Bitmap compassImageNorth;
	private Bitmap compassImageNorthEast;
	private Bitmap compassImageNorthWest;
	private Bitmap compassImageEast;
	private Bitmap compassImageWest;
	private Bitmap compassImageSouth;
	private Bitmap compassImageSouthEast;
	private Bitmap compassImageSouthWest;

	static final int LOAD_LOCATION_REQUEST = 1;
	static final int SAVE_LOCATION_REQUEST = 2;
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private float currentBearing = 0;
	
	private final SensorEventListener mListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			currentBearing = event.values[0];
			update();
		}
		
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

	
	};
	
	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			currentLocation = location;
			update();
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		
		public void onProviderEnabled(String provider) {
		}
		
		public void onProviderDisabled(String provider) {
			// current location will go out of date soon, so stop showing it now.
			currentLocation = null;
		}
	};
	private com.netowrks.rps1.GPSTracker gps;

	
	public Navigation(LocationManager locationManager2,
			SensorManager mSensorManager2, SharedPreferences settings2, Context context2) {
		// TODO Auto-generated constructor stub
		locationManager = locationManager2;
		mSensorManager = mSensorManager2;
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION); 
		settings = settings2;
		context = context2;
		//updateDesiredLocation();
	}

	public void onSetLocation(double etDesiredLat2, double etDesiredLng2)
	{
		etDesiredLat = etDesiredLat2;
		etDesiredLng = etDesiredLng2;
	}
	public void onCreate(ImageView imgBearing2, Bitmap compassImageNorth2, Bitmap compassImageNorthEast2, Bitmap compassImageNorthWest2, Bitmap compassImageEast2, Bitmap compassImageWest2, Bitmap compassImageSouth2, Bitmap compassImageSouthEast2, Bitmap compassImageSouthWest2) {
		
		//setContentView(R.layout.navigation);
		
/*		tvCurrentLat = (TextView) findViewById(R.id.currentLat);
		tvCurrentLng = (TextView) findViewById(R.id.currentLng);
		tvCurrentAccuracy = (TextView) findViewById(R.id.currentAccuracy);
		tvCurrentBearing =  (TextView) findViewById(R.id.currentBearing);
		
		etDesiredLat = (EditText) findViewById(R.id.desiredLat);
		etDesiredLng = (EditText) findViewById(R.id.desiredLng);*/

/*		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		etDesiredLat.setText(settings.getString("desiredLat",""));
		etDesiredLng.setText(settings.getString("desiredLng",""));
*/		
/*		etDesiredLat.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) { updateDesiredLocation(); }
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){ updateDesiredLocation(); }
		});
		etDesiredLng.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) { updateDesiredLocation(); }
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){ updateDesiredLocation(); }
		}); */
		//////////////// Need to call Update Location
		/*tvDistance = (TextView) findViewById(R.id.desiredDistance);
		tvBearing = (TextView) findViewById(R.id.desiredBearing);*/
		
		/*locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);		
		
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		 Got from Navigate Activity*/
		
		imgBearing = imgBearing2;
		compassImageNorth = compassImageNorth2;//BitmapFactory.decodeResource(getResources(), R.drawable.compass_n);
		compassImageNorthEast = compassImageNorthEast2;//BitmapFactory.decodeResource(getResources(), R.drawable.compass_ne);
		compassImageNorthWest = compassImageNorthWest2;//BitmapFactory.decodeResource(getResources(), R.drawable.compass_nw);
		compassImageEast = compassImageEast2;//BitmapFactory.decodeResource(getResources(), R.drawable.compass_e);
		compassImageWest = compassImageWest2;//BitmapFactory.decodeResource(getResources(), R.drawable.compass_w);
		compassImageSouth = compassImageSouth2;//BitmapFactory.decodeResource(getResources(), R.drawable.compass_s);
		compassImageSouthEast = compassImageSouthEast2;// BitmapFactory.decodeResource(getResources(), R.drawable.compass_se);
		compassImageSouthWest = compassImageSouthWest2;//BitmapFactory.decodeResource(getResources(), R.drawable.compass_sw);
	
		updateDesiredLocation(); // which in turn calls update();
	}


    protected void onResume() {
		mSensorManager.registerListener(mListener, mSensor,SensorManager.SENSOR_DELAY_GAME);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);        
        
    }

    protected void onStop() {
		locationManager.removeUpdates(locationListener);
		mSensorManager.unregisterListener(mListener);
		
		if (desiredLocation instanceof Location) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("desiredLat", Double.toString(desiredLocation.getLatitude()));
			editor.putString("desiredLng", Double.toString(desiredLocation.getLongitude()));
			editor.commit();
		}
	      
		// We may get reloaded later ... but by then the current location is prob out of date.
		currentLocation = null;
		
		
    }
    	
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.map_menu_item:
        	String uri = "geo:"+Double.toString(desiredLocation.getLatitude())
        	    +","+Double.toString(desiredLocation.getLongitude())
        	    +"?q="+Double.toString(desiredLocation.getLatitude())
        	    +","+Double.toString(desiredLocation.getLongitude());
            Intent intent = new Intent(Intent.ACTION_VIEW,  Uri.parse(uri));
            startActivity(intent);
            return true;
        case R.id.current_menu_item:
        	if (currentLocation instanceof Location) {
        		etDesiredLat.setText(Double.toString(currentLocation.getLatitude()));
        		etDesiredLng.setText(Double.toString(currentLocation.getLongitude()));
        		updateDesiredLocation(); // which also calls update()
        	} else {
        		Toast.makeText(this, "Current Location Not Known", Toast.LENGTH_SHORT).show();
        	}
        	return true;
             	
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	*/
/*	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SAVE_LOCATION_REQUEST) {
				
			} else if (requestCode == LOAD_LOCATION_REQUEST) {
				Bundle dataB = data.getExtras();
				etDesiredLat.setText(Double.toString(dataB.getDouble("latitude")));
				etDesiredLng.setText(Double.toString(dataB.getDouble("longitude")));
				updateDesiredLocation();
			}
		}
	}
*/
    
	public void updateDesiredLocation() {
		try { 
			double lat = etDesiredLat;//Double.parseDouble(etDesiredLat.getText().toString());
			double lng = etDesiredLng;//Double.parseDouble(etDesiredLng.getText().toString());
			if (!(desiredLocation instanceof Location)) {
				desiredLocation = new Location(LocationManager.GPS_PROVIDER);
			}
			desiredLocation.setLatitude(lat);
			desiredLocation.setLongitude(lng);		
			Toast.makeText(context, "updated desired Location", Toast.LENGTH_SHORT).show();
		} catch (NumberFormatException e) {
			desiredLocation = null;
			Toast.makeText(context, "update-"+e.toString(), Toast.LENGTH_SHORT).show();
		}		
		update();
	}
	
	public void update() {
		
		System.out.println("in Update-"+currentBearing);
		tvCurrentBearing = (double)currentBearing;//setText(Float.toString(currentBearing));
		
		if(!(currentLocation instanceof Location))
		{
			
			// create class object
	        gps = new GPSTracker(context);

			// check if GPS enabled		
	        if(gps.canGetLocation()){
	        			             
	        	double latitude = gps.getLatitude();
	        	double longitude = gps.getLongitude();
	        	currentLocation = gps.getLocation();
	        	// \n is for new line
	        	Toast.makeText(context, "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();	
	        }else{
	        	// can't get location
	        	// GPS or Network is not enabled
	        	// Ask user to enable GPS/network in settings
	        	gps.showSettingsAlert();
	        }
		}
		
		if (currentLocation instanceof Location) {
					
			double currentLat = currentLocation.getLatitude();
			tvCurrentLat = currentLat;//.setText(Double.toString(currentLat));
								
			double currentLng = currentLocation.getLongitude();
			tvCurrentLng = currentLng;//.setText(Double.toString(currentLng));

			float currentAccuracy = currentLocation.getAccuracy();
			tvCurrentAccuracy = currentAccuracy;//.setText(Float.toString(currentAccuracy));
			
			if (desiredLocation instanceof Location) {

				
				
				float bearing = currentLocation.bearingTo(desiredLocation) ;
				float distance = currentLocation.distanceTo(desiredLocation);
				
				if (distance > 1) {
					tvBearing = bearing;//.setText(Float.toString(bearing));
					tvDistance = distance;//.setText(Float.toString(distance));
					float bearingRelative = bearing - currentBearing;
					while (bearingRelative < 0) bearingRelative =  bearingRelative + 360;
					while (bearingRelative > 360) bearingRelative =  bearingRelative - 360;
					if ( (360 >=bearingRelative && bearingRelative >=337.5)||(0<=bearingRelative && bearingRelative <= 22.5)) {
						imgBearing.setImageBitmap(compassImageNorth);
						imgBearing.setVisibility(View.VISIBLE);
					} else if (22.5 < bearingRelative  && bearingRelative < 67.5) {
						imgBearing.setImageBitmap(compassImageNorthEast);
						imgBearing.setVisibility(View.VISIBLE);
					} else if (67.5 <= bearingRelative && bearingRelative <= 112.5) {
						imgBearing.setImageBitmap(compassImageEast);
						imgBearing.setVisibility(View.VISIBLE);
					} else if (112.5 < bearingRelative && bearingRelative < 157.5) {
						imgBearing.setImageBitmap(compassImageSouthEast);
						imgBearing.setVisibility(View.VISIBLE);
					} else if (157.5 <= bearingRelative && bearingRelative <= 202.5) {
						imgBearing.setImageBitmap(compassImageSouth);
						imgBearing.setVisibility(View.VISIBLE);
					} else if (202.5 < bearingRelative && bearingRelative < 247.5) {
						imgBearing.setImageBitmap(compassImageSouthWest);
						imgBearing.setVisibility(View.VISIBLE);
					} else if (247.5 <= bearingRelative && bearingRelative <= 292.5) {
						imgBearing.setImageBitmap(compassImageWest);
						imgBearing.setVisibility(View.VISIBLE);
					} else if (292.5 < bearingRelative && bearingRelative < 337.5) {
						imgBearing.setImageBitmap(compassImageNorthWest);
						imgBearing.setVisibility(View.VISIBLE);
					} 
				} else {
					tvBearing = 0.0;//setText("");
					tvDistance = 0.0;//.setText("Less than 1");
					imgBearing.setVisibility(View.INVISIBLE);
					Toast.makeText(context, "Distance<1", Toast.LENGTH_SHORT).show();
				}
				
				
			} else {
				Toast.makeText(context, "Destination not Parsed", Toast.LENGTH_SHORT).show();
				/*tvDistance.setText("destination not parsed");
				tvBearing.setText("destination not parsed");*/
			}
			
			
		} else {

			Toast.makeText(context, "Dono", Toast.LENGTH_SHORT).show();
			
/*			tvCurrentLat.setText("dunno");
			tvCurrentLng.setText("dunno");
			tvCurrentAccuracy.setText("dunno");
			tvDistance.setText("dunno");
			tvBearing.setText("dunno");
*/			
		}
		
	}
	
}