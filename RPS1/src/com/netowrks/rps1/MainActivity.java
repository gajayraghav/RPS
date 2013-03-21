package com.netowrks.rps1;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.bBasicChat1).setOnClickListener(this);
		findViewById(R.id.bChat1).setOnClickListener(this);
		findViewById(R.id.bRegistration1).setOnClickListener(this);
		findViewById(R.id.bHome1).setOnClickListener(this);
		findViewById(R.id.bMap1).setOnClickListener(this);
		findViewById(R.id.bNavigate1).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent nextActivity;
		switch(v.getId())
		{
			case R.id.bBasicChat1:
				nextActivity = new Intent(this, BasicChat.class);
				this.startActivity(nextActivity);
				break;
			case R.id.bChat1:
				nextActivity = new Intent(this, Chat.class);
				this.startActivity(nextActivity);
				break;
			
			case R.id.bMap1:
				nextActivity = new Intent(this, Map.class);
				this.startActivity(nextActivity);
				break;
				
			case R.id.bNavigate1:
				nextActivity = new Intent(this, Navigate.class);
				this.startActivity(nextActivity);
				break;
				
			case R.id.bRegistration1:
				nextActivity = new Intent(this, Registration.class);
				this.startActivity(nextActivity);
				break;
				
			case R.id.bHome1:
				nextActivity = new Intent(this, Home.class);
				this.startActivity(nextActivity);
				break;
		}
	}

}
