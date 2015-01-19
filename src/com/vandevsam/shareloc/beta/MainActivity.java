package com.vandevsam.shareloc.beta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vandevsam.shareloc.beta.data.MarkerDataManager;
import com.vandevsam.shareloc.beta.data.MyMarkerObj;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.app.DialogFragment;
//import android.app.FragmentManager;
//import android.app.FragmentManager;
//import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends FragmentActivity implements LocationListener,
                                NoticeDialogFragment.NoticeDialogListener {

	Context context = this;
	private GoogleMap map;	
	private static final int zoom = 13;
	
	//TODO just in case
	private static final String Vancouver = "49.25 -123.1";
		
	SessionManager session;
	
	//TODO move to utilities class?
	private LatLng loc;			
	public LatLng getLoc() {
		return loc;
	}
	public void setLoc(LatLng loc) {
		this.loc = loc;
	}	
	
	private String[] mListTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;	
    private CharSequence mTitle;
    
	MarkerDataManager data;
	private ArrayAdapter<String> mDrawerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);			
		/**
		 * Here I'm initialzing drawer pane list variables and setting the
		 * onItemclicklistnere method
		 */		
		session = new SessionManager(getApplicationContext());
		
        if (session.checkLogin())
		   mListTitles = getResources().getStringArray(R.array.sidepane_array);		 
        else
           mListTitles = getResources().getStringArray(R.array.sidepane_array_guest);			
		
		mDrawerAdapter = new ArrayAdapter<String>(this,
				                                  R.layout.drawer_list_item, 
				                                  mListTitles);
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // Set the adapter for the list view
        mDrawerList.setAdapter(mDrawerAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	 @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {  
        		 //String item = mDrawerAdapter.getItem(position);
        		 selectItem(mDrawerAdapter.getItem(position),position);
             }
		});    
        
        //location preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String city = prefs.getString(getString(R.string.pref_location_key),
        		   getString(R.string.pref_location_default));  
        String gps = prefs.getString(getString(R.string.pref_gps_key),
        		   getString(R.string.pref_gps_default));
        
        /**
		 * Map initialization
		 */	             
        //related to main framgment: map
		//map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();	
		
		SupportMapFragment fm = (SupportMapFragment)   getSupportFragmentManager().findFragmentById(R.id.map);
		map = fm.getMap();		
		
		 // Enabling MyLocation in Google Map
        map.setMyLocationEnabled(true);
        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Getting Current Location From GPS
        Location location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 20000, 0, this);
        // Find location from gps
        if(location!=null && gps.equalsIgnoreCase("enabled")){
            onLocationChanged(location);
        }	else {
        	//TODO: fix and clean up code        	
        	  try {
        		  //Zoom to selected location in settings
				zoomToLocation(geoCode(city));
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), 
		    			  "Failed to load geocoder, zooming in to Vancouver", 
		    			  Toast.LENGTH_LONG).show();
				zoomToLocation(Vancouver);				
			}
        }
              
		//create new database to store markers
        /**
         * SQLITE database open
         */
		data = new MarkerDataManager(context);		    
		try {
			data.open();
		} catch (Exception e){
			Log.i("Error!", "Cannot open SQLite DB");
		}			
		//show available markers
		//listMarker();
				
		//receiving intent workaround solution TODO temp fix
		try {		 
		   onActivityResult(90, RESULT_OK, this.getIntent());	
		} catch (Exception e){
			Log.i("Error!", "No intent passed back yet");
		}
		
		//add markers			
	    map.setOnMapClickListener(new OnMapClickListener() {	    	    
	            @Override
	            public void onMapClick(LatLng latlng) { 	            	
	            	            	
	            	String coordinates = String.valueOf(latlng.latitude)+" "+String.valueOf(latlng.longitude);					
					Intent newLocation = new Intent(getBaseContext(), NewLocationActivity.class)
	            	                               .putExtra(Intent.EXTRA_TEXT, coordinates);
                    startActivityForResult(newLocation, 91);                    
	            }
	    });	 
	    
	    //delete markers
	    map.setOnInfoWindowClickListener(new OnInfoWindowClickListener(){
			@Override
			public void onInfoWindowClick(Marker marker) {	
				//save coordinates of location marker to be deleted
				//TODO find a better way to pass the coordinates, instead of setLoc
				setLoc(new LatLng(marker.getPosition().latitude,marker.getPosition().longitude));	
				//TODO uncomment after fixing
				//showNoticeDialog();
			}
	    }); 
	 		    
	}
	//end onCreate method
		
	private String geoCode(String city) throws IOException{
		
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		List<Address> addresses = geocoder.getFromLocationName(city, 1);
		Address address = addresses.get(0);
		String coordinates = String.valueOf(address.getLatitude())+
				             " "+
		                     String.valueOf(address.getLongitude());
		return coordinates;
		
	}
	
	/** Swaps fragments in the map view */
	private void selectItem(String item, int position) {
		DrawerListEnum enumval = DrawerListEnum.valueOf(item.toUpperCase().replace(" ", "_"));        
        switch (enumval) {
        case SEARCH: //search
        	startActivityForResult(new Intent(context, SearchActivity.class), 90);
        	//startActivity(new Intent(context, SearchActivity.class));
            break;
        case SEARCH_GROUPS: //create a group        
        	startActivity(new Intent(context, SearchGroupsActivity.class));        	
            break;
        case CREATE_GROUP: //create a group
        	startActivity(new Intent(context, CreateGroupActivity.class));        	
            break;
        case PROFILE:  //profile        	
        	startActivity(new Intent(context, ProfileActivity.class));
            break; 
        case SETTINGS://settings activity
        	startActivity(new Intent(context, SettingsActivity.class));
            break;
        case LOGOUT: //Log out        
        	session.logoutSession();
        	//to reload activity
        	reloadActivity();
            break; 
        case CONTACT: //send feedback
        	sendEmail();
            break; 
        case LOGIN: //login
        	map.clear();
        	startActivity(new Intent(context, AuthenticateActivity.class));
        	finish();
            break; 
        case REGISTER: //register 
        	//TODO fix code
        	startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        	finish();
            break;
        default:
            break;
        }        
	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    setTitle(mListTitles[position]);
	    mDrawerLayout.closeDrawer(mDrawerList);	    
	}
	
	@Override
	public void setTitle(CharSequence title) {
	    mTitle = title;
	    //TODO fix
	    //getActionBar().setTitle(mTitle);
	}
	
	//TODO review
	protected void sendEmail() {
	      Log.i("Send email", "");

	      String[] TO = {"alwesam@gmail.com"};
	      Intent emailIntent = new Intent(Intent.ACTION_SEND);
	      emailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	      emailIntent.setData(Uri.parse("mailto:"));
	      emailIntent.setType("text/plain");
	      emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);	      
	      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
	      emailIntent.putExtra(Intent.EXTRA_TEXT, "Feedback");
	      try {
	         startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	         finish();
	         Log.i("Finished sending email...", "");
	      } catch (android.content.ActivityNotFoundException ex) {
	         Toast.makeText(MainActivity.this, 
	         "There is no messaging client installed.", Toast.LENGTH_SHORT).show();
	      }
	   }
	
	//dialog methods
	public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new NoticeDialogFragment(context);
        //TODO fix
        FragmentManager fm =  getSupportFragmentManager();
        //dialog.show(fm, "NoticeDialogFragment");
    }
    
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
    	//retreive location marker coordinates
    	String slatlng = String.valueOf(getLoc().latitude)+" "+String.valueOf(getLoc().longitude);    	
    	//delete record
    	data.deleteMarker( new MyMarkerObj(slatlng));
    	dialog.dismiss();  
    	Toast.makeText(getApplicationContext(), "Marker deleted", Toast.LENGTH_LONG).show();
    	listMarker();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) { 
    	dialog.dismiss();    	
    }
    
    /**
    @Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub		
	}	**/
	
	//list markers on the map
	private void listMarker(){		
		float mapIcon = BitmapDescriptorFactory.HUE_ROSE;
		map.clear();
		List <MyMarkerObj> n = data.getAllMarkers();			
		for (int i=0; i < n.size(); i++){
			String[] slatlng = n.get(i).getPosition().split(" ");
			LatLng latlng = new LatLng (Double.valueOf(slatlng[0]), Double.valueOf(slatlng[1]));			
			if(n.get(i).getStatus().equalsIgnoreCase("no"))
				mapIcon = BitmapDescriptorFactory.HUE_ROSE;
			else
				mapIcon = BitmapDescriptorFactory.HUE_GREEN;
			map.addMarker(new MarkerOptions()			   
			   .title(n.get(i).getTitle())
			   .snippet(n.get(i).getSnippet())
			   .position(latlng)
			   .icon(BitmapDescriptorFactory.defaultMarker(mapIcon))
			   .draggable(true));			
		}	
	}	
	
	private Marker listAMarker(String pos){
		map.clear();
		float mapIcon = BitmapDescriptorFactory.HUE_ROSE;
		MyMarkerObj n = data.getSelectMarker(pos);		
	    String[] slatlng = n.getPosition().split(" ");
		LatLng latlng = new LatLng (Double.valueOf(slatlng[0]), Double.valueOf(slatlng[1]));
		if((n.getStatus()).equalsIgnoreCase("no")) //not synced, goes rose
			mapIcon = BitmapDescriptorFactory.HUE_ROSE; //not synced
		else
			mapIcon = BitmapDescriptorFactory.HUE_GREEN; //synced
		Marker marker = map.addMarker(new MarkerOptions()			   
		               .title(n.getTitle())
		        	   .snippet(n.getSnippet())
			           .position(latlng)
			           .icon(BitmapDescriptorFactory.defaultMarker(mapIcon))
			           .draggable(true));		
		return marker;
	}	
	
	private void findMarker(String slatlng){
		
		Marker marker = listAMarker(slatlng);
		LatLng latLng = marker.getPosition();	 
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(zoom+2));        
		marker.showInfoWindow();		
	}
	
	
	/**private void checkPref(){		
        SharedPreferences myPref  
        = PreferenceManager.getDefaultSharedPreferences(this); 
        String _pref =  
                "Option 1: " +  myPref.getBoolean("pref_opt1", true) + "\n"
                + "Option 4: " +  myPref.getBoolean("pref_opt4", false); 
        Toast.makeText(getApplicationContext(), 
  			 _pref, 
  			  Toast.LENGTH_LONG).show();
    }**/
	
		
	private void checkPref(){
	
		SaveGroupPreference pref = new SaveGroupPreference(this); 
		List<String> groups = pref.getPrefGroup();
		List<Boolean> groups_check = pref.getPrefCheck();
		
		for (int i=0; i<groups.size(); i++) {
			Toast.makeText(getApplicationContext(), 
		  			 groups.get(i), Toast.LENGTH_LONG).show();
		
			String a;
			if(groups_check.get(i))
				a = "True";
			else 
				a = "False";
			
			
			Toast.makeText(getApplicationContext(), 
					a, Toast.LENGTH_LONG).show();
			
		}
		        
	}
	
	//call back from SearchActivity	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent response) {
	  //super.onActivityResult(requestCode, resultCode, response);
     if (response != null){
	    switch(requestCode) {
	      case 0 : {	    	
	        if (resultCode == RESULT_OK) {
	    	  checkPref();
	        }
	       break;
	      } 
	      case 90 : {	    	
	        if (resultCode == RESULT_OK) {
	    	  String coordinates = response.getStringExtra("note");
	    	  findMarker(coordinates);
	        }
	       break;
	      } 
	      case 91 : {	    	
		        if (resultCode == RESULT_OK) {
		    	  String result = response.getStringExtra("coord");
		    	  boolean added = response.getExtras().getBoolean("added");		    	  
		    	  if (added)
		    	      findMarker(result);
		    	  else
		    		  zoomToLocation(result);
		        }
		       break;
		      } 
	    }
      }  else {
    	  Toast.makeText(getApplicationContext(), 
    			  "Data NULL", 
    			  Toast.LENGTH_LONG).show();
      }    	       
	}		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //When Sync action button is clicked
        if (id == R.id.action_search){
            startActivityForResult(new Intent(this, SearchActivity.class), 90);        	
           return true;
        }       
        if (id == R.id.refresh){
        	//reloadActivity();
        	listMarker();
        	return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.sync_to_DB) {
            //Sync SQLite DB markers to remote MySQL DB
        	if (session.checkLogin()){
               ServerUtilFunctions up = new ServerUtilFunctions(this, "Uploading Markers...");
               up.syncSQLiteMySQLDB();
        	}
        	else
        		Toast.makeText(getApplicationContext(), 
        				"You have to logged in to upload markers to server", 
        				Toast.LENGTH_LONG).show();
            return true;
        } 
        if (id == R.id.sync_from_DB) {
            ServerUtilFunctions down = new ServerUtilFunctions(this, "Downloading Markers...");
            down.syncMySQLDBSQLite();
            return true;
        }  
        if (id == R.id.group_pref){
           startActivityForResult(new Intent(this, SetGroupPreference.class), 0);        	
           return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
	
   @Override
   public void onLocationChanged(Location location) {
        double mLatitude = location.getLatitude();
        double mLongitude = location.getLongitude();
        LatLng latLng = new LatLng(mLatitude, mLongitude);	 
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }	
   
   private void zoomToLocation(String location) {		 
	    String[] coordinates = location.split("\\s+");   	  
        LatLng latLng = new LatLng(Double.parseDouble(coordinates[0]),
       		       Double.parseDouble(coordinates[1]));	    	 
	    map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
	    map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
	}	
  
   // Reload MainActivity
   public void reloadActivity() {
       Intent objIntent = new Intent(getApplicationContext(), MainActivity.class);
       startActivity(objIntent);
   }     
	
	@Override
	public void onProviderDisabled(String arg0) {
			
	}

	@Override
	public void onProviderEnabled(String arg0) {
				
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				
	}	
	
}
