package com.netowrks.rps1;

/*import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlSerializer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Registration extends Activity implements OnClickListener {

	public class Example {
		protected Context context;

		public Example(Context context) {
			this.context = context;
		}
	}

	static String getEmail(Context context) {
	    AccountManager accountManager = AccountManager.get(context); 
	    Account account = getAccount(accountManager);

	    if (account == null) {
	      return null;
	    } else {
	      return account.name;
	    }
	  }

	  private static Account getAccount(AccountManager accountManager) {
	    Account[] accounts = accountManager.getAccountsByType("com.google");
	    Account account;
	    if (accounts.length > 0) {
	      account = accounts[0];      
	    } else {
	      account = null;
	    }
	    return account;
	  }


	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		// final Textview nameField = (Textview) findViewById(R.id.txtName);
		// TelephonyManager tMgr
		// =(TelephonyManager)mAppContext.getSystemService(Context.TELEPHONY_SERVICE);
		// mPhoneNumber = tMgr.getLine1Number();
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		final EditText Name = (EditText) findViewById(R.id.txtName);
//		final String Phone = (String) mTelephonyMgr.getLine1Number();
		final EditText Email = (EditText) findViewById(R.id.txtEmail);
		final EditText Phone = (EditText) findViewById(R.id.txtPhone);
		if(mTelephonyMgr.getLine1Number().toString().length() > 0)
		{
			Phone.setText(mTelephonyMgr.getLine1Number().toString());
			Phone.setEnabled(false);
		}
		else
		{
			Phone.setHint("Phone Number");
		}
		String emailid = getEmail(getApplicationContext());
		if(emailid!=null)
		{
			Email.setText(emailid);
			Email.setEnabled(false);
		}
		else
		{
			Email.setHint("Email-ID");
		}
		Button rtn = (Button) findViewById(R.id.bRegister);

		rtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Log.v("EditText", Name.getText().toString());
				// Log.v("EditText", Phone.getText().toString());
				Log.v("EditText", Email.getText().toString());
				// URL url = null;
				final String urlbuilder = "http://www.klusterkloud.com/RPS/api/create.json?Name="
						+ Name.getText().toString()
						+ "&Phone="
						+ Phone.toString()
						+ "&Email="
						+ Email.getText().toString();
				Log.v("URL", urlbuilder.toString());
				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							try {
								HttpClient client = new DefaultHttpClient();
								// String getURL = "http://www.google.com";
								HttpGet get = new HttpGet(urlbuilder);
								HttpResponse responseGet = client.execute(get);
								HttpEntity resEntityGet = responseGet
										.getEntity();
								if (resEntityGet != null) {
									// do something with the response
									String response = EntityUtils
											.toString(resEntityGet);
									/*
									 * try { JSONObject json= (JSONObject) new
									 * JSONTokener(result).nextValue();
									 * JSONObject json2 =
									 * json.getJSONObject("results"); test =
									 * (String) json2.get("name"); }
									 */
									try {
										JSONObject json = (JSONObject) new JSONTokener(
												response).nextValue();
										JSONObject json2 = json
												.getJSONObject("Name");
										String Nodeid = (String) json2
												.get("NodeID");
										Log.i("Node id", Nodeid);

										// File file = new
										// File(Environment.getExternalStorageDirectory()
										// + File.separator +
										// ".registration.xml");
										// file.createNewFile();

										String FILENAME = "registration.xml";
										String string = "<NodeID>" + Nodeid
												+ "</NodeID>" + "<Phone>"
												+ Phone + "</Phone>"
												+ "<Email>" + Email
												+ "</Email>";

										FileOutputStream fos = openFileOutput(
												FILENAME, Context.MODE_PRIVATE);
										fos.write(string.getBytes());
										fos.close();
										FILENAME = "NodeID.txt";
										FileOutputStream fos1 = openFileOutput(
												FILENAME, Context.MODE_PRIVATE);
										fos1.write(Nodeid.getBytes());
										fos1.close();

										Context context1 = getApplicationContext();
										FileInputStream fos2 = context1
												.openFileInput("NodeID.txt");
										StringBuffer fileContent = new StringBuffer(
												"");

										byte[] buffer = new byte[1024];

										while (fos2.read(buffer) != -1) {
											fileContent.append(new String(
													buffer));
										}
										fos2.close();

										RegistrationNodeID.NodeId = fileContent
												.toString();

										// TextView tv =
										// (TextView)findViewById(R.);
										// new
										// AlertDialog.Builder(this).setTitle("Status").setMessage("Success!").setNeutralButton("Close",
										// null).show();

									} catch (Exception e) {
										Log.e("Exception",
												"Exception occured in wroting");
									}

									// Log.i("GET RESPONSE", response);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};

				thread.start();
				Toast.makeText(getApplicationContext(), "Success!",
						Toast.LENGTH_LONG).show();
			}
		});

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}
}