package com.vandevsam.shareloc.beta;


import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vandevsam.shareloc.beta.data.GroupDataManager;

public class MyGroupDetailActivity extends Activity {
	
	Context context = this;
	private String groupName;
	private TextView textName;	
	private TextView textDesc;
	private TextView textType;
	
	GroupDataManager group;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mygroupdetail);
        
        Intent intent = getIntent();   
		groupName = intent.getStringExtra("key");	
        
        group = new GroupDataManager(context);
        
        group.open();        
        //fetch details
        List<String> details = group.getDetails(groupName);             
        group.close();
        
        //extractions
        String name = details.get(0);
        String desc = details.get(1);
        String type = details.get(2);
        
      //get name and username
        textName = (TextView) findViewById(R.id.textName);
        String htmlName = "<h3>Group Name: "+name+"</h3>";
        textName.setText(Html.fromHtml(htmlName));
        
        textDesc = (TextView) findViewById(R.id.textDesc);
        String htmlDate = "<h3>Group Description: "+desc+"</h3>";
        textDesc.setText(Html.fromHtml(htmlDate));        
        
        //get groups signed in
        //special case TODO review later
        textType = (TextView) findViewById(R.id.textType);
        String htmlGroup = "<h3>Group Type: "+type+"</h3>";
        textType.setText(Html.fromHtml(htmlGroup));

	}
	
	public void deleteGroup(View view) {
		group.open();
		group.deleteGroup(groupName);
		group.close();
		Toast.makeText(getApplicationContext(), 
				 "group deleted", 
	              Toast.LENGTH_LONG).show();	
        this.callHomeActivity(view);
    }
	
	/**
     * Navigate to Home Screen 
     * @param view
     */
    public void callHomeActivity(View view) {        
		finish();
    }   
	

}

