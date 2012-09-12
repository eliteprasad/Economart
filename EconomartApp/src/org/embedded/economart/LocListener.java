package org.embedded.economart;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;

import org.embedded.economart.PostQuery.MyOnItemSelectedListener;

//import com.amazon.aws.demo.AWSDemo;
//import com.amazon.aws.demo.sns.SnsMenu;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import java.util.UUID;


public class LocListener implements LocationListener
{
	public int ctr = 0;
	public Double locListenerlat = 0.0;
	public Double locListenerlon = 0.0;
	public String locName = null;
	public int timeOut = 0;
	private MyInterface ctx;
	
	public LocListener() {
		this.ctx = ctx;
	}
	
	public String myFunc(MyInterface c, Double lat, Double lon)
    {
    	System.out.println("Entering myFunc ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		List<Address> address = null;
	       
	       try
	       {
         	   		
	    	   		address = new Geocoder((Context) c, Locale.getDefault()).getFromLocation(lat, lon, 1);
	                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Address is :");
	       }
	      
	       catch (IOException e)
	       {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	       }
	       
	       
	        
	        locName = address.get(0).getAddressLine(0)+ ", " +address.get(0).getAddressLine(1) + ", " + address.get(0).getAddressLine(2);
	        return locName;
    }
	
	
	
	
	
	public void onLocationChanged(Location location) 
    {
          // Called when a new location is found by the network location provider.
         
		// makeUseOfNewLocation(location);
    	Context context = (Context) ctx;
		if(ctr == 0 && timeOut <= 1000)
		{
			    Location loc = location;			
        	   	locListenerlat = location.getLatitude();
            	locListenerlon = location.getLongitude();
            	System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!Coordinates are :" + locListenerlat +" "+ locListenerlon);
            	timeOut++;
                String locNameReturn = myFunc(ctx, locListenerlat, locListenerlon);
                
                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ " + locListenerlat + " " + locListenerlon + " " + locName);
                
            	            	
              //	locationUpdated = true;
		}      	
        	
        	if(locListenerlat != 0.0 && locListenerlon != 0.0)
        		ctr = 1;        	
        		
    }

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}




