package com.example.googlemapcopy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity   {

	EditText words;
	AutoCompleteTextView atvPlaces;
	PlacesTask placesTask;
    ParserTask parserTask;
    boolean checked=false;
    List<HashMap<String, String>> list;
	protected static final int REQUEST_OK = 1;	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_search);
		
		
		
		//findViewById(R.id.speak).setOnClickListener(this);
		
		atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
		atvPlaces.setThreshold(1);
		 
        atvPlaces.addTextChangedListener(new TextWatcher() {
 
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                placesTask.execute(s.toString());
            }
 
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
                // TODO Auto-generated method stub
            }
 
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });
		
	}
	
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
 
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
 
            // Connecting to url
            urlConnection.connect();
 
            // Reading data from url
            iStream = urlConnection.getInputStream();
 
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
 
            StringBuffer sb = new StringBuffer();
 
            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }
 
            data = sb.toString();
 
            br.close();
 
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
 
    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String>{
 
        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";
 
            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=YOUR_API_KEY";
 
            String input="";
 
            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
 
            // place type to be searched
            String types = "types=geocode";
 
            // Sensor enabled
            String sensor = "sensor=false";
 
            // Building the parameters to the web service
            String parameters = input+"&"+types+"&"+sensor+"&"+key;
 
            // Output format
            String output = "json";
 
            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;
 
            try{
                // Fetching the data from we service
                data = downloadUrl(url);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
 
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
 
            // Creating ParserTask
            parserTask = new ParserTask();
 
            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }
    /** A class to parse the Google Places in JSON format */
    class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{
 
        JSONObject jObject;
 
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
 
            List<HashMap<String, String>> places = null;
 
            AutocompleteJSONParser placeJsonParser = new AutocompleteJSONParser();
 
            try{
                jObject = new JSONObject(jsonData[0]);
 
                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);
 
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }
 
        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
 
            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };
 
            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);
 
            // Setting the adapter
            atvPlaces.setAdapter(adapter);
            list=result;
        }
    }
	
	  public boolean onOptionsItemSelected(MenuItem item) {
			
			
			
		            return super.onOptionsItemSelected(item);
		     } 

	
	public void Speak(View v) {
	Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	         i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
	        	 try {
	             startActivityForResult(i, REQUEST_OK);
	         } catch (Exception e) {
	        	 	Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
	         }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        if (requestCode==REQUEST_OK  && resultCode==RESULT_OK) {
	        		final ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	        		//((TextView)findViewById(R.id.text1)).setText(thingsYouSaid.get(0));
	        		
	        		//EditText searchField = (EditText) findViewById(R.id.text1);  
	        		atvPlaces.setText(thingsYouSaid.get(0));
	        }
	    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	  
	String reference="";
	public void location(View view)
	 {
		//final EditText infoField=null;// = (EditText) findViewById(R.id.text1);  
		// String info = infoField.getText().toString();
		String info = atvPlaces.getText().toString(); 
		String Eng_info="";
		
			Eng_info +=info;
		 System.out.println(Eng_info);
		 char ch;
		 int i=0;
		 if(!checked)
		 {
		 /*while(i<Eng_info.length())
		 {
			 ch=Eng_info.charAt(i);
			 if(ch==' ')
			 { String Text = "Product name should not contain spaces";
	        	Toast.makeText( getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
			 return;
			 }
			 i++;
		 }*/
		 
		 if(!testDetails(Eng_info))
		 {
			 Toast.makeText(getApplicationContext(), "Product search should only contain alphabets", Toast.LENGTH_SHORT).show();
			 return;
		 }
		 //Create the bundle
		  Bundle bundle = new Bundle();
		  //Add your data to bundle
		  bundle.putString("type", "product");
		  bundle.putString("stuff",Eng_info); 
		 
		  Intent intent = new Intent(this, MyLocation.class);
		  intent.putExtras(bundle);
		  	startActivity(intent);
		  	this.finish();	 
		 }
		 else
		 {
			 Iterator it=list.iterator();
			 
			 while(it.hasNext())
			 {
				 HashMap<String,String> h= (HashMap<String,String>)it.next();
				 if(info.equals(h.get("description")));
				 {reference=h.get("reference");}
			 }
			 StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
		        //sb.append("reference="+reference);
				sb.append("reference="+reference);
		       // sb.append("&sensor=true");
		        sb.append("&key=YOUR_API_KEY");
		     // Creating a new non-ui thread task to download Google place details
		        PlacesTask1 placesTask1 = new PlacesTask1();
		 
		        // Invokes the "doInBackground()" method of the class PlaceTask
		        System.out.println("URL : "+sb.toString());
		        placesTask1.execute(sb.toString());
		 }
		
		
		    
		   

	 }
	 
	 		
		
	 public boolean testDetails(String str)
	 {
		 return str.matches("^[a-zA-Z]+$");
	 }
	 
	 public void onCheckboxClicked(View view)
	 {
		 
		 checked = ((CheckBox) view).isChecked();
	 }
		
	 
	 /*All tasks with one are to handle internet access for places data*/
	 
	 
	    /** A class, to download Google Place Details */
	    class PlacesTask1 extends AsyncTask<String, Integer, String>{
	 
	        String data = null;
	 
	        // Invoked by execute() method of this object
	        @Override
	        protected String doInBackground(String... url) {
	            try{
	                data = downloadUrl(url[0]);
	            }catch(Exception e){
	                Log.d("Background Task",e.toString());
	            }
	            return data;
	        }
	 
	        // Executed after the complete execution of doInBackground() method
	        @Override
	        protected void onPostExecute(String result){
	            ParserTask1 parserTask1 = new ParserTask1();
	 
	            // Start parsing the Google place details in JSON format
	            // Invokes the "doInBackground()" method of the class ParseTask
	            parserTask1.execute(result);
	        }
	    }
	 
	    /** A class to parse the Google Place Details in JSON format */
	    class ParserTask1 extends AsyncTask<String, Integer, HashMap<String,String>>{
	 
	        JSONObject jObject;
	 
	        // Invoked by execute() method of this object
	        @Override
	        protected HashMap<String,String> doInBackground(String... jsonData) {
	 
	            HashMap<String, String> hPlaceDetails = null;
	            PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
	 
	            try{
	                jObject = new JSONObject(jsonData[0]);
	                System.out.println("JObject Google view : "+jObject);
	                // Start parsing Google place details in JSON format
	                hPlaceDetails = placeDetailsJsonParser.parse(jObject);
	 
	            }catch(Exception e){
	                Log.d("Exception",e.toString());
	            }
	            return hPlaceDetails;
	        }
	 
	        // Executed after the complete execution of doInBackground() method
	        @Override
	        protected void onPostExecute(HashMap<String,String> hPlaceDetails){
	 
	        	String imgSrcHtml;
	            String name = hPlaceDetails.get("name");
	            if(name==null)
	            {
	            	Toast.makeText(getApplicationContext(), "No data received", Toast.LENGTH_LONG);
	            }
	            else
	            {
	            String icon = hPlaceDetails.get("icon");
	            String vicinity = hPlaceDetails.get("vicinity");
	            String lat = hPlaceDetails.get("lat");
	            String lng = hPlaceDetails.get("lng");
	            String formatted_address = hPlaceDetails.get("formatted_address");
	            String formatted_phone = hPlaceDetails.get("formatted_phone");
	            String website = hPlaceDetails.get("website");
	            String rating = hPlaceDetails.get("rating");
	            String international_phone_number = hPlaceDetails.get("international_phone_number");
	            String url = hPlaceDetails.get("url");
	            
	            System.out.println("name :"+name+" lat : "+lat+" lng "+lng);
	            Bundle bundle = new Bundle();
	            bundle.putString("type", "place");
	            double mLatitude=Double.parseDouble(lat);
	            double mLongitude=Double.parseDouble(lng);
	            LatLng coordinate=new LatLng(mLatitude,mLongitude);
	            bundle.putString("lat", lat);
	            bundle.putString("lng", lng);
	            bundle.putString("reference", reference);
	            bundle.putString("name",name);
	            
	            Intent intent = new Intent(MainActivity.this, MyLocation.class);
	  		    intent.putExtras(bundle);
	  		  	startActivity(intent);
	  		  	//this.finish();
	            }
	           
	        }
	    }
	 
	 
		}
		



