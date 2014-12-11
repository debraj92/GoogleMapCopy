package com.example.googlemapcopy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.example.googlemapcopy.AlertDialogRadio.AlertPositiveListener;


public class MyLocation extends FragmentActivity implements AlertPositiveListener {
	
	
	String type="";
	String r="";
	String lat="";
	String lng="";
	String n="";
			
	
	
	GoogleMap mGoogleMap;	
	ArrayList<Polyline> lines = new ArrayList<Polyline>();	
	
	HashMap<String, String> pair = new HashMap<String, String>();
	HashMap<String, LatLng> id_loc = new HashMap<String, LatLng>();
	HashMap<String, String> id_desc = new HashMap<String, String>();
	//HashMap<String, String> desc_id = new HashMap<String, String>();
	ArrayList<String> idlist=new ArrayList<String>();
	String[] mPlaceType=null;
	String[] mPlaceTypeName=null;
	int count_of_parentplaces=0;
	double mLatitude=0;
	double mLongitude=0;
	static String name;
	Button btnFind;
	String ref;
	String ref1;
	LocationManager mlocManager,m1locManager;
	LocationListener mlocListener;
	static Geocoder gcd=null;
	int check=1;
	static boolean walk=false;
	OnClickListener listener=null,listener1=null;
	//int count_parent_place=0;
	HashMap<String, String> mMarkerPlaceLink = new HashMap<String, String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_location);
		if(!isNetworkAvailable())
		{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	 
				// set title
				alertDialogBuilder.setTitle("No internet :(");
	 
				// set dialog message
				alertDialogBuilder
					.setMessage("No internet connectivity. Please try again!!")
					.setCancelable(false)
					.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, close
							// current activity
							MyLocation.this.finish();
						}
					  });
	 
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
	 
					// show it
					alertDialog.show();
		}
		else
		{
		 gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
		Toast.makeText( getApplicationContext(), "Please wait while current location arrives.", Toast.LENGTH_LONG).show();
		/* Use the LocationManager class to obtain GPS locations */
        mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        m1locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        try
        {
      mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
      m1locManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
        }
        catch(Exception e)
        {
        	Toast.makeText( getApplicationContext(), "Cannot fetch Location, please enable GPS and check your internet", Toast.LENGTH_LONG).show();
        }
      //  m1locManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);   
		//Get the bundle
	    Bundle bundle = getIntent().getExtras();

	    type=bundle.getString("type");
	    if(type.equalsIgnoreCase("product"))
	    {
	    	//Extract the data…
	        name = bundle.getString("stuff");
	    }
	    else
	    {
	    	r=bundle.getString("reference");
	    	n=bundle.getString("name");
	    	lat=bundle.getString("lat");
	    	lng=bundle.getString("lng");
	    	
	    }
	    
	    	    	        
		System.out.println("Name in MyLocation  "+name);
		// Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available

        	int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        }else { // Google Play Services are available
        	
	    	// Getting reference to the SupportMapFragment
	    	SupportMapFragment fragment = ( SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
	    			
	    	// Getting Google Map
	    	mGoogleMap = fragment.getMap();
	    			
	    	// Enabling MyLocation in Google Map
	    	mGoogleMap.setMyLocationEnabled(true);
	    	
	    	mGoogleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
	        	 
	            public void onInfoWindowClick(Marker arg0) {
	            	
	            	if(type.equalsIgnoreCase("product"))
	            	{
	            	Intent intent;
	            	
	            	
	            	intent = new Intent(getBaseContext(), GoogleWebviewActivity.class);
            		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            		intent.putExtra("reference", pair.get(arg0.getId()));
            		  
	            	
	                System.out.println("Marker_id "+arg0.getId());
	                String reference = mMarkerPlaceLink.get(arg0.getId());
	               
	                //intent.putExtra("id", reference);
	                intent.putExtra("name",name);
	              
	                //intent.putExtra("flag", fl); // to know if it is listed by google or by shopkeeper
	                System.out.println("name "+name);
	                System.out.println("reference "+ref);
	                // Starting the Place Details Activity
	                startActivity(intent);
	            	}
	            	else
	            	{
	            		
	            		Intent intent;
		           	
		            	intent = new Intent(getBaseContext(), GoogleWebviewActivity.class);
	            		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	            		intent.putExtra("reference", r);
	            		
		                intent.putExtra("name",n);
		          
		                // Starting the Place Details Activity
		                startActivity(intent);
	            	}
	            }
	        });
	    		 //mLatitude=12.93198413;
	    		//mLongitude=77.53458888;
	    		//find();
        
	    	final int position = 0;
      	     listener = new OnClickListener() {
      	            public void onClick(View v) {
      	            	
      	            	if(!lines.isEmpty())
      	            	{
      	            	for(Polyline line : lines)
      	            	{
      	            	    line.remove();
      	            	}
      	            	
      	            	}
      	            	if(type.equalsIgnoreCase("product"))
    	            	{
      	            	if(id_desc.isEmpty())
      	            	{
      	            		Toast.makeText( getApplicationContext(), "No Listings Found", Toast.LENGTH_SHORT).show();
      	            		return;
      	            	}
      	            	
      	            	MyLocation.walk=false;
      	                /** Getting the fragment manager */
      	                FragmentManager manager = getSupportFragmentManager();
      	 
      	                /** Instantiating the DialogFragment class */
      	                AlertDialogRadio alert = new AlertDialogRadio();
      	 
      	                /** Creating a bundle object to store the selected item's index */
      	                Bundle b  = new Bundle();
      	 
      	                /** Storing the selected item's index in the bundle object */
      	                b.putInt("position", position);
      	 
      	                /** Setting the bundle object to the dialog fragment object */
      	                alert.setArguments(b);
      	 
      	                /** Creating the dialog fragment object, which will in turn open the alert dialog window */
      	                alert.show(manager, "Choose Location");
    	            	}
      	            	else
    	            	{
    	            		MyLocation.walk=false;
    	            		LatLng latLng = new LatLng(mLatitude, mLongitude);
    	            		String url = getDirectionsUrl(latLng,new LatLng(Double.parseDouble(lat),Double.parseDouble(lng)));

    	                    DownloadTask downloadTask = new DownloadTask();

    	                    // Start downloading json data from Google Directions API
    	                    System.out.println("Response "+downloadTask.execute(url));
    	            	}
      	            	
      	            }
      	        };
       		
      	        
      	      listener1 = new OnClickListener() {
    	            public void onClick(View v) {
    	            	
    	            	if(!lines.isEmpty())
    	            	{
    	            	for(Polyline line : lines)
    	            	{
    	            	    line.remove();
    	            	}
    	            	
    	            	}
    	            	if(type.equalsIgnoreCase("product"))
    	            	{
    	            	if(id_desc.isEmpty())
    	            	{
    	            		Toast.makeText( getApplicationContext(), "No Listings Found", Toast.LENGTH_SHORT).show();
    	            		return;
    	            	}
    	            	
    	            	MyLocation.walk=true;
    	                /** Getting the fragment manager */
    	                FragmentManager manager = getSupportFragmentManager();
    	 
    	                /** Instantiating the DialogFragment class */
    	                AlertDialogRadio alert = new AlertDialogRadio();
    	 
    	                /** Creating a bundle object to store the selected item's index */
    	                Bundle b  = new Bundle();
    	 
    	                /** Storing the selected item's index in the bundle object */
    	                b.putInt("position", position);
    	 
    	                /** Setting the bundle object to the dialog fragment object */
    	                alert.setArguments(b);
    	 
    	                /** Creating the dialog fragment object, which will in turn open the alert dialog window */
    	                alert.show(manager, "Choose Location");
    	            	}
    	            	else
    	            	{
    	            		MyLocation.walk=true;
    	            		LatLng latLng = new LatLng(mLatitude, mLongitude);
    	            		String url = getDirectionsUrl(latLng,new LatLng(Double.parseDouble(lat),Double.parseDouble(lng)));

    	                    DownloadTask downloadTask = new DownloadTask();

    	                    // Start downloading json data from Google Directions API
    	                    System.out.println("Response "+downloadTask.execute(url));
    	            	}
    	            	}
    	        };
      	        
       		Button btn = (Button) findViewById(R.id.btn_drive);
       		Button btn1 = (Button) findViewById(R.id.btn_walk);;
       	     
       		btn.setBackgroundColor(0x5500ff00);
       		btn1.setBackgroundColor(0x5500ff00);
       		
               /** Setting a button click listener for the choose button */
               btn.setOnClickListener(listener);
               btn1.setOnClickListener(listener1);
        
        }	
        
       
		}
	 
	       
	       // mLatitude=12.93198413;
	       // mLongitude=77.53458888;
     // find(); //remove this from here
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_my_location, menu);
		return true;
	}
	//boolean myflag=true;
	
	public boolean onOptionsItemSelected(MenuItem item) {
      //  super.onOptionsItemSelected(item);
		//System.out.println(item.getTitle());
		if(item.getItemId()==R.id.netloc)
		{
		
		
        	//System.out.println("OK Enter ");
			MyLocation.this.mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
        	MyLocation.this.m1locManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
        	Toast.makeText( MyLocation.this.getApplicationContext(), "Requesting Location Update.", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
 } 	
	
	
	
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
	
	public void find()
	{
		//count_of_parentplaces=0;
		if(type.equalsIgnoreCase("product"))
		{
		System.out.println(1);
		StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
		sb.append("location="+mLatitude+","+mLongitude);
		
			//sb.append("&name="+name);
		name.replace(' ', '+');
			sb.append("&keyword="+name);
			sb.append("&radius=20000");
			//sb.append("&types=store|bakery|bicycle_store|book_store|clothing_store|convenience_store|department_store|establishment|furniture_store|food|florist|grocery_or_supermarket|home_goods_store|meal_delivery|meal_takeaway|pharmacy|restaurant|shoe_store|shopping_mall");
			sb.append("&language=en");
			//sb.append("&sensor=true");
			sb.append("&key=YOUR_API_KEY"); 
				
        PlacesTask placesTask = new PlacesTask();		        			        
        System.out.println(placesTask.execute(sb.toString()));
		}
		else
		{
			double mLatitude=Double.parseDouble(lat);
            double mLongitude=Double.parseDouble(lng);
            LatLng coordinate=new LatLng(mLatitude,mLongitude);
            MarkerOptions markerOptions= new MarkerOptions();
			 // Setting the position for the marker
            markerOptions.position(coordinate);
            //name_loc.put(""+name, latLng);
            
            markerOptions.title(n);
            Marker m = mGoogleMap.addMarker(markerOptions);
            mMarkerPlaceLink.put(m.getId(), r);
            ref=r;
            name=n;
		}
        
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

                StringBuffer sb  = new StringBuffer();

                String line = "";
                while( ( line = br.readLine())  != null){
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

	
	/** A class, to download Google Places */
	private class PlacesTask extends AsyncTask<String, Integer, String>{

		String data = null;
		ProgressDialog progress;
		
		public PlacesTask() {
			   progress = new ProgressDialog(MyLocation.this);
				 progress.setMessage("Searching !! Please Wait ..");
			}
	protected void onPreExecute()
	{
		progress.setCanceledOnTouchOutside(false);
		progress.show();
	}

		@Override
		protected String doInBackground(String... url) {
			try{
				
				data = downloadUrl(url[0]);
			}catch(Exception e){
				 Log.d("Background Task",e.toString());
			}
			System.out.println("DATA "+data);
			
			
			
			
			return data;
		}
		
		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result){	
			
				
				 if(!result.equals(""))
    	    	 {
    	    		 ParserTask parserTask = new ParserTask();
    	 			
    	 			// Start parsing the Google places in JSON format
    	 			// Invokes the "doInBackground()" method of the class ParseTask
    	 			parserTask.execute(result);
    	    	 }
			
				 else
				 {
					 if(idlist.isEmpty())
					 {
						 Toast.makeText(MyLocation.this, "No Listings Available", Toast.LENGTH_LONG);
					 }
					 
					 
					 
					 Collection<String> values= id_desc.values();
		        		String[] arr = values.toArray(new String[values.size()]);
		        		Android.code=arr.clone();
		        		Set<String> keys=id_desc.keySet();
		        		arr = keys.toArray(new String[keys.size()]);
		        		Android.id=arr.clone();
		        		MarkerOptions markerOptions = new MarkerOptions();
					 Iterator it = idlist.iterator();
		        		while(it.hasNext())
		        		{
		        			
		        			String id=(String)it.next();
		        			String desc=id_desc.get(id);
		        			LatLng latlng=(LatLng)id_loc.get(id);
		        			markerOptions.position(latlng);
		                	markerOptions.title(desc);
		                	Marker m = mGoogleMap.addMarker(markerOptions);
		                	pair.put(m.getId(), id);
		        		}
		              
				 }
			
			 progress.dismiss(); 
			
		}
		
	}
	
	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

		JSONObject jObject;
	
		@Override
		protected List<HashMap<String,String>> doInBackground(String... jsonData) {
		
			List<HashMap<String, String>> places = null;			
			PlaceJSONParser placeJsonParser = new PlaceJSONParser();
        
	        
	        	try {
					jObject = new JSONObject(jsonData[0]);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	//System.out.println("Json object "+jObject);
	            /** Getting the parsed data as a List construct */
	            places = placeJsonParser.parse(jObject);
	            //System.out.println("Places "+places);
	            
	            //lat[0]=Double.parseDouble(places.get(0).get("lat"));
	            //lat[1]=Double.parseDouble(places.get(1).get("lat"));
	            //lon[0]=Double.parseDouble(places.get(0).get("lng"));
	            //lon[1]=Double.parseDouble(places.get(1).get("lng"));
	             return places;
		}
		
		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(List<HashMap<String,String>> list){			
			
			// Clears all the existing markers 
		
			mGoogleMap.clear();
			MarkerOptions markerOptions = null;
			try{
			for(int i=0;i<list.size();i++){
			
				// Creating a marker
				markerOptions= new MarkerOptions();
	            
	            // Getting a place from the places list
	            HashMap<String, String> hmPlace = list.get(i);
	
	            // Getting latitude of the place
	            double lat = Double.parseDouble(hmPlace.get("lat"));	            
	            
	            // Getting longitude of the place
	            double lng = Double.parseDouble(hmPlace.get("lng"));
	            
	            // Getting name
	            String name = hmPlace.get("place_name");
	          
	            System.out.println(hmPlace);
	            LatLng latLng = new LatLng(lat, lng);
	            
	            // Setting the position for the marker
	            markerOptions.position(latLng);
	            //name_loc.put(""+name, latLng);
	            idlist.add(hmPlace.get("id"));
	            id_desc.put(hmPlace.get("id"), ""+name);
	            id_loc.put(hmPlace.get("id"),latLng);
	            markerOptions.title(""+name);
	            Marker m = mGoogleMap.addMarker(markerOptions);
	            
           	            
	            pair.put(m.getId(), hmPlace.get("reference"));
	            //System.out.println("ID = "+m.getId()+" id = "+hmPlace.get("id"));
	          
	            // Linking Marker id and place reference
	            mMarkerPlaceLink.put(m.getId(), hmPlace.get("id"));
	        
			}
			  Collection<String> values= id_desc.values();
        		String[] arr = values.toArray(new String[values.size()]);
        		Android.code=arr.clone();
        		Set<String> keys=id_desc.keySet();
        		arr = keys.toArray(new String[keys.size()]);
        		Android.id=arr.clone();
               
        		
        		
					
			
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
        	
        	
		}
		
	}
	}
	
	 /* Class My Location Listener */
    public class MyLocationListener implements LocationListener
    {
     double THRESHOLD=5000;
     double diff;
      public void onLocationChanged(Location loc)
      {
    	  diff=System.currentTimeMillis()-loc.getTime();
if(diff > THRESHOLD)
{
	//Toast.makeText( getApplicationContext(), "Exit : "+ diff, Toast.LENGTH_SHORT).show();
	Toast.makeText( getApplicationContext(), "Old location rejected!! Retrying", Toast.LENGTH_SHORT).show();
	return;
}
       mLatitude = loc.getLatitude();
       mLongitude= loc.getLongitude();
        String Text = "My current location is: " +
        "Latitude = " + loc.getLatitude() +
        "Longitude = " + loc.getLongitude();
        
        if(check<=2)
        {
        	Toast.makeText( getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
        	check++;
        }
        mlocManager.removeUpdates(mlocListener); //GPS updates removed
        m1locManager.removeUpdates(mlocListener); // Network updates removed
        /*Location arrived*/
        
        find();
        
        LatLng coordinate=new LatLng(mLatitude,mLongitude);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 10);
        mGoogleMap.animateCamera(yourLocation);
      }

      public void onProviderDisabled(String provider)
      {
        Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
      }

      
      public void onProviderEnabled(String provider)
      {
        Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
      }

      
      public void onStatusChanged(String provider, int status, Bundle extras)
      {

      }
      
    }
	
    public void onPause()
    {
    	super.onPause();
    	if(mlocManager!=null)
    	mlocManager.removeUpdates(mlocListener);
    	
    }

	
	
	/** Defining button click listener for the OK button of the alert dialog window */

    public void onPositiveClick(int position) {
  
    	System.out.println("Ok called");
    	LatLng latLng = new LatLng(mLatitude, mLongitude);
    	
    	
    	// Getting URL to the Google Directions API
    	//String desc=Android.code[position];
    	
    	String id=Android.id[position];
        String url = getDirectionsUrl(latLng,id_loc.get(id));

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        System.out.println("Response "+downloadTask.execute(url));
    }
	
	
    private String getDirectionsUrl(LatLng origin,LatLng dest){
    	 
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
 
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
 
        // Sensor enabled
        //String sensor = "sensor=false";
        String parameters;
        // check walk or drive
        if(MyLocation.walk)
        {
        	parameters= str_origin+"&"+str_dest+"&mode=walking"+"&key=AIzaSyCjAL0t-UwKwYHBfuCzhw21JRs_tcl7cLw";
        }
        else
        {
        	parameters= str_origin+"&"+str_dest+"&key=AIzaSyCjAL0t-UwKwYHBfuCzhw21JRs_tcl7cLw";
        }
        // Building the parameters to the web service
         
 
        // Output format
        String output = "json";
 
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
 
        return url;
    }
    
    
    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{
 
    	ProgressDialog progress;
    	public DownloadTask() {
    		   progress = new ProgressDialog(MyLocation.this);
    		   progress.setMessage("Fetching Directions..");
    		}
    	
    	protected void onPreExecute()
    	{
    		progress.setCanceledOnTouchOutside(false);
    		progress.show();
    	}
    	
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
 
            // For storing data from web service
            String data = "";
 
            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                System.out.println("data"+data);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
 
        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
 
            ParserDriveTask parserdriveTask = new ParserDriveTask();
 
            // Invokes the thread for parsing the JSON data
            System.out.println("post"+parserdriveTask.execute(result));
            progress.dismiss();
        }
    }
 
    
    /** A class to parse the Google Places in JSON format */
    private class ParserDriveTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
 
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
 
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
 
            try{
                jObject = new JSONObject(jsonData[0]);
                
                String status=(String)jObject.get("status");
                if(status.equals("OK"))
                {DirectionsJSONParser parser = new DirectionsJSONParser();
                               
                // Starts parsing data
                routes = parser.parse(jObject);
                }
                else
                {
                	return null;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
 
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        	if(result==null)
        	{
        		Toast.makeText(getApplicationContext(), "Directions not available currently", Toast.LENGTH_SHORT);
        	}
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions=null;
            MarkerOptions markerOptions = new MarkerOptions();
            
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
 
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
 
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
 
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
 
                    points.add(position);
                }
 
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }
 
            // Drawing polyline in the Google Map for the i-th route
            lines.add(mGoogleMap.addPolyline(lineOptions));
        }
    }
    
	}