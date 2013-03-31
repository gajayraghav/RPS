package com.netowrks.rps1;

/*import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
*/
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
//import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
//import android.os.StrictMode;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Registration extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		//final Textview nameField = (Textview) findViewById(R.id.txtName);
		final EditText Name   = (EditText)findViewById(R.id.txtName);
		final EditText Phone   = (EditText)findViewById(R.id.editText1);
		final EditText Email   = (EditText)findViewById(R.id.editText2);
		Button  rtn = (Button)findViewById(R.id.bRegister);
		
	

	   rtn.setOnClickListener(new View.OnClickListener()
		        {
		            public void onClick(View view)
		            {
		                Log.v("EditText", Name.getText().toString());
		                Log.v("EditText", Phone.getText().toString());
		                Log.v("EditText", Email.getText().toString());
		               // URL url = null;
		                final String urlbuilder="http://www.klusterkloud.com/RPS/api/create.json?Name="+Name.getText().toString()+"&Phone="+Phone.getText().toString()+"&Email="+Email.getText().toString();
		                Log.v("URL", urlbuilder.toString());
		                Thread thread = new Thread()
		                {
		                    @Override
		                    public void run() {
		                        try {
		                        	try {
		                        	    HttpClient client = new DefaultHttpClient();  
		                        	   // String getURL = "http://www.google.com";
		                        	    HttpGet get = new HttpGet(urlbuilder);
		                        	    HttpResponse responseGet = client.execute(get);  
		                        	    HttpEntity resEntityGet = responseGet.getEntity();  
		                        	    if (resEntityGet != null) {  
		                        	        // do something with the response
		                        	        String response = EntityUtils.toString(resEntityGet);
		                        	       /* try {
		                        	            JSONObject json= (JSONObject) new JSONTokener(result).nextValue();
		                        	            JSONObject json2 = json.getJSONObject("results");
		                        	            test = (String) json2.get("name");
		                        	        }
		                        	        */
		                        	        try {
		                        	            JSONObject json= (JSONObject) new JSONTokener(response).nextValue();
		                        	            JSONObject json2 = json.getJSONObject("Name");
		                        	            String Nodeid = (String) json2.get("NodeID");
		                        	            Log.i("Node id",Nodeid );
		                        	            
		                        	            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "registration.xml");
		                 	                    file.createNewFile();
		                        	          
		                        	           
		                        	            FileOutputStream fileos = null;
		                        	            try{
		                        	                fileos = new FileOutputStream(file);

		                        	            }catch(FileNotFoundException e)
		                        	            {
		                        	                Log.e("FileNotFoundException",e.toString());
		                        	            }
		                        	            XmlSerializer serializer = Xml.newSerializer();
		                        	            try{
		                        	            serializer.setOutput(fileos, "UTF-8");
		                        	            serializer.startDocument(null, Boolean.valueOf(true));
		                        	            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		                        	            serializer.startTag(null, "NodeID");
		                        	           
		                        	            serializer.text(Nodeid);
		                        	           
		                        	            serializer.endTag(null,"NodeID");
		                        	            serializer.startTag(null, "Name");
			                        	           
		                        	            serializer.text(Name.getText().toString());
		                        	           
		                        	            serializer.endTag(null,"Name");
		                        	            serializer.startTag(null, "Phone");
			                        	           
		                        	            serializer.text(Phone.getText().toString());
		                        	           
		                        	            serializer.endTag(null,"Phone");
		                        	            serializer.startTag(null, "Email");
			                        	           
		                        	            serializer.text(Email.getText().toString());
		                        	           
		                        	            serializer.endTag(null,"Email");
		                        	            serializer.endDocument();
		                        	            serializer.flush();
		                        	            fileos.close();
		                        	            //TextView tv = (TextView)findViewById(R.);

		                        	            }catch(Exception e)
		                        	            {
		                        	                Log.e("Exception","Exception occured in wroting");
		                        	            }
		                        	        }
		                        	    catch(Exception e)
		                        	    {
		                        	    	e.printStackTrace();	
		                        	    }
		                        	    
		                        	            
		                        	            
		                        	        
		                        	       
		                        	      //  Log.i("GET RESPONSE", response);
		                        	    }
		                        	} catch (Exception e) {
		                        	    e.printStackTrace();
		                        	}
		                            }                               
		                        catch (Exception e) {
		                            e.printStackTrace();
		                        }
		                    }
		                };

		                thread.start(); 

		            }
		        });

}
	

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
}