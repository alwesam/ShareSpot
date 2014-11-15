package com.vandevsam.shareloc.beta;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

public class SearchActivity extends Activity {
	
	private Context context = this;	
	private List<String> list;
	private ArrayAdapter<String> searchListAdapter;
	MarkerDataSource data;
	String coordinates;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		
		data = new MarkerDataSource(context);
        try {
			data.open();
		} catch (Exception e){
			Log.i("hello", "hello");
		} 	
        
        list = doMySearch("ALL");
        
	    searchIntent(getIntent());
	    
	    searchListAdapter = new ArrayAdapter<String>(
                //the current context (this fragement's parent activity)
                this,
                //ID of list item layout
                R.layout.list_search_item,
                //ID of textView to populate
                R.id.list_search_item_textview,
                //forecast data
                list);

         ListView listView = (ListView) findViewById(R.id.listview_search);
         listView.setAdapter(searchListAdapter); 
    
       //now when I click on a result!
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
	         coordinates = data.getPosition(searchListAdapter.getItem(position));
             returnResults(coordinates);	       
           }			
         });	
	}	
	
	private void returnResults (String slatlng){
		
		Intent resultIntent = new Intent();
		resultIntent.putExtra("note", slatlng);
		setResult(RESULT_OK, resultIntent);
		finish();		
	}
	
  /*	
	private void returnList (List<String> alist){
		
		Toast.makeText(getApplicationContext(), 
  			  "I'm running", 
  			  Toast.LENGTH_LONG).show();
		Intent resultIntent = new Intent();
		resultIntent.putStringArrayListExtra("note2", (ArrayList<String>) alist);
		setResult(RESULT_OK, resultIntent);
		finish();
	}
	
	/*@Override
	public void onBackPressed() {
	   //super.onBackPressed();
	   Toast.makeText(getApplicationContext(), 
 			  coordinates, 
 			  Toast.LENGTH_LONG).show();
	    Intent intent = new Intent();
	    intent.putExtra("note", coordinates);
	    setResult(RESULT_OK, intent);
	    finish();
	    returnResults(coordinates);
	} 
		
   private ArrayList<String> fakeList(){		
		ArrayList<String> searchList = new ArrayList<String>();		
		searchList.add("Nothing");			
		return searchList;		
	}	*/
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    searchIntent(intent);
	}
	
	private void searchIntent (Intent intent){
		//wait for user
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			
	         String query = intent.getStringExtra(SearchManager.QUERY);	         
	         list = doMySearch(query);
	    }
	   /* else {	         
	         
	    } */
	}
   
	private ArrayList<String> doMySearch (String search){	
		    //searchListAdapter.clear();
		    ArrayList<String> searchList = new ArrayList<String>();
	        searchList = data.getAddresses(search);
	        return searchList;		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the options menu from XML
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.search, menu);
	    // Get the SearchView and set the searchable configuration
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
	    // Assumes current activity is the searchable activity
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
	    return true;
	}
		
}