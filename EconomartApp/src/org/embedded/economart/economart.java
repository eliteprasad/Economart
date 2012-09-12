package org.embedded.economart;


// Imports

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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;


// Classes

public class economart extends Activity implements LocationListener
{
	private boolean credentials_found;
	private static AmazonSimpleDB sdb = null;
	private Button postButton;
	private Button workButton;
	private Button dealsButton;
	private String newItem = "newItem";
	private Button myqueriesButton;
	private String[] itemNames;
	private HashMap<String,String> attributes;
	private ProgressDialog myProgressDialog =null;
	private LocationManager locationManager;
	private boolean locationUpdated = false;
	
	public static int count;
	public static BasicAWSCredentials credentials = null;
	public Location loc;
	public double lat;
	public double lon;	
	public final int geonetThresh = 11000; //default threshold set to 4000 meters 
	public int ctr = 0;
	public String[] queryList;	
	public String locName = null;
	
	Spinner spinner;
	
	public void onLocationChanged(Location location) 
	{
		System.out.println("Entered......................................................");
		if(location!=null)
		{
		   if(location.getLatitude()!=0.0 && location.getLongitude()!=0.0 )
		   {
			   
			   System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!In progressDialod dismiss");
			   System.out.println("++++++++++++++++++++++++++++++Progress Dialog = "+myProgressDialog.toString());
			   myProgressDialog.dismiss();
			   
			   locationUpdated=true;
			   lat=location.getLatitude();
			   lon=location.getLongitude();
			   System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Coordinates are "+lat+ " " + lon);
			   locationManager.removeUpdates(this);
		   }
		}
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

	
	
	protected void onStart() 
	{
		super.onStart();
		//calculateGeoNet();	
	};
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main);       
			myProgressDialog = ProgressDialog.show(economart.this,"","Computing Your Current Location.." );
			
			Context context = getApplicationContext();        
			        
			startGetCredentials();
			
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Before myProcess dialog");
      
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! After myProcess dialog");
			locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			
			// ****************** Location Listener ************************************       
			      
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);        
			        
			/* ************************************************************************************ */
			
			this.postButton = (Button)this.findViewById(R.id.postquery);
			postButton.cancelLongPress();
			postButton.setOnClickListener(new View.OnClickListener() 
			{
				
				public void onClick(View v) 
				{
					  Intent intent = new Intent(economart.this, PostQuery.class);
			    	  Bundle b = new Bundle();
			    	  b.putDouble("latitude", lat);
			    	  b.putDouble("longitude", lon);
			    	  System.out.println("Vals being passed from economart to postqueries = " + lat + lon);
			    	  intent.putExtras(b);
			    	  startActivity(intent);
				}
			});
			
			this.workButton = (Button)this.findViewById(R.id.button1);        
			workButton.cancelLongPress();
			workButton.setOnClickListener(new View.OnClickListener() 
			{
				
				public void onClick(View v) 
				{
					
					System.out.println("#######################In work button press");
					
					  Intent intent = new Intent(economart.this, ViewQuery.class);
			    	  Bundle b = new Bundle();
			    	  System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!Coordinates are :" + lat +" "+ lon);
			    	  b.putDouble("latitude", lat);
			    	  b.putDouble("longitude", lon);
			    	  System.out.println("Vals being passed from economart to view queries = " + lat + lon);
			    	  intent.putExtras(b);
			    	  startActivity(intent);
			        
				}
			});
      
			
			this.myqueriesButton = (Button)this.findViewById(R.id.myqueries);
			myqueriesButton.cancelLongPress();
			myqueriesButton.setOnClickListener(new View.OnClickListener() 
			{
				
				public void onClick(View v) 
				{
					  Intent intent = new Intent(economart.this, ViewMyQueries.class);
			    	  Bundle b = new Bundle();
			    	  b.putDouble("latitude", lat);
			    	  b.putDouble("longitude", lon);
			    	  System.out.println("Vals being passed from economart to myqueries = " + lat + lon);
			    	  intent.putExtras(b);
			    	  startActivity(intent);
				}
			});
			
			this.dealsButton = (Button)this.findViewById(R.id.dealsbutton);
			dealsButton.cancelLongPress();
			dealsButton.setOnClickListener(new View.OnClickListener() 
			{
				
				public void onClick(View v) 
				{
					  Intent intent = new Intent(economart.this, Deals.class);
			    	  Bundle b1 = new Bundle();
			    	  b1.putDouble("latitude", lat);
			    	  b1.putDouble("longitude", lon);
			    	  System.out.println("Vals being passed from economart to deals = " + lat + lon);
			    	  intent.putExtras(b1);
			    	  startActivity(intent);
				}
			});
		} 
        
        catch (Exception e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this.getApplicationContext(), "An unexpected error has occured. Please re-start your application.", Toast.LENGTH_LONG);
		}
              
    }

    public String myFunc(Double lat, Double lon)
    {
    	List<Address> address = null;
	       
	       try
	       {
	                address = new Geocoder(this,Locale.getDefault()).getFromLocation(lat, lon, 1);
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
    
    public void calculateGeoNet()
    {

        String product = null;
        int i;
        Double lati = 0.0;
        Double longi = 0.0;
         
    	// Database Query
    	       
        SelectRequest selectRequest = new SelectRequest( "select itemName() from `Queries`" ).withConsistentRead( true );
		List items = getInstance().select(selectRequest).getItems();	
        
        String[] itemNames = new String[100];
		
		System.out.println("SIZE ---------------------" + items.size());
		
			for(i = 0; i < items.size(); i++)
			{
				itemNames[i] = ((Item)items.get(i)).getName();
			}         
        
        	            
        itemNames = getItemNamesForDomain("Queries");
        queryList = new String[itemNames.length];
        
        for(i = 0; i < itemNames.length; i++) 
        {
            GetAttributesRequest getRequest = new GetAttributesRequest( "Queries", itemNames[i] ).withConsistentRead( true );
            GetAttributesResult getResult = getInstance().getAttributes( getRequest );    
            
            HashMap<String,String> attributes = new HashMap<String,String>(30);        
            for(Object attribute : getResult.getAttributes()) 
            {
                    try
                    {
                    	String name = ((Attribute)attribute).getName();
                        String value = ((Attribute)attribute).getValue();
                        attributes.put(name, value);

                        lati = Double.valueOf(attributes.get("Latitude"));
                        longi = Double.valueOf(attributes.get("Longitude"));
   
                        if(lati == null) lati = 0.0;
                        if(longi == null) longi = 0.0;
                        
                        product = attributes.get("queryposted");
                      
                        System.out.println("QueryID "+i+ "is:  " + attributes.get("queryid"));
                        System.out.println("What is the price of " + product);
                        System.out.print("near " + lati);
                        System.out.print("and " + longi);
                    }
                    
                    catch(Exception e)
                    {
                    	
                    }
            }

       	attributes.get ("column name");
   	    float[] results;
        results = new float[2];
               
        // =============================================== Geo-Net Computation ========================================================
                
       	loc.distanceBetween(lat, lon, lati, longi, results);      	            	
       	System.out.println("The Distance Between the co-ordinates is: " + results[0] + " meters");
       	
       	
       	if(results[0] <= geonetThresh)
       	{
       		System.out.println("******************************** The current location is within the Geo Net! ************************************");
       		// put your display code here
       		
       		queryList[i] = "What is the price of " + product;
       		System.out.println("QList " + queryList[i]);
       		
       	}
       	else
       	{
       		System.out.println("Not entering" + i);
       	}
       	
           	
                	
          	
     	} //end of for
       
        //locationManager.removeUpdates(locationListener);
                       
    }
 
    
    private void startGetCredentials() 
    {
    	
    	Thread t = new Thread() 
    	{
    		@Override
    		public void run()
    		{
    	        try 
    	        {            
    	            Properties properties = new Properties();
    	            properties.load( getClass().getResourceAsStream( "AwsCredentials.properties" ) );
    	            
    	            String accessKeyId = properties.getProperty( "accessKey" );
    	            String secretKey = properties.getProperty( "secretKey" );
    	            
    	            if ( ( accessKeyId == null ) || ( accessKeyId.equals( "" ) ) ||
    	            	 ( accessKeyId.equals( "CHANGEME" ) ) ||( secretKey == null )   || 
    	                 ( secretKey.equals( "" ) ) || ( secretKey.equals( "CHANGEME" ) ) ) {
    	                Log.e( "AWS", "Aws Credentials not configured correctly." );                                    
        	            credentials_found = false;
    	            } else {
    	            credentials = new BasicAWSCredentials( properties.getProperty( "accessKey" ), properties.getProperty( "secretKey" ) );
        	        credentials_found = true;
    	            }

    	        }
    	        catch ( Exception exception ) {
    	            Log.e( "Loading AWS Credentials", exception.getMessage() );
    	            credentials_found = false;
    	        }
  
    		}
    	};
    	t.start();
    	
    	
    }
    
  
    public static AmazonSimpleDB getInstance() 
    {
        if ( sdb == null ) {
		    sdb = new AmazonSimpleDBClient( economart.credentials );
            sdb.setEndpoint( "https://sdb.amazonaws.com:443" );  		
        }

        return sdb;
	}
    
    public static void getItemNamesCountForDomain( final String domainName ) 
    {
    	Thread t = new Thread() {
    		@Override
    		public void run(){
    			
    			try{
    		
				    	SelectRequest selectRequest = new SelectRequest( "select itemName() from `" + domainName + "`" ).withConsistentRead( true );
						List items = getInstance().select(selectRequest).getItems();	
						
						String[] itemNames = new String[ items.size() ];
						for ( int i = 0; i < items.size(); i++ ) {
							itemNames[ i ] = ((Item)items.get( i )).getName();
						}
						
						count = items.size();
						System.out.println("Count = "+count);
    			}
    			catch (AmazonServiceException ase) {
    	            System.out.println("Caught an AmazonServiceException, which means your request made it "
    	                    + "to Amazon SimpleDB, but was rejected with an error response for some reason.");
    	            System.out.println("Error Message:    " + ase.getMessage());
    	            System.out.println("HTTP Status Code: " + ase.getStatusCode());
    	            System.out.println("AWS Error Code:   " + ase.getErrorCode());
    	            System.out.println("Error Type:       " + ase.getErrorType());
    	            System.out.println("Request ID:       " + ase.getRequestId());
    	        } catch (AmazonClientException ace) {
    	            System.out.println("Caught an AmazonClientException, which means the client encountered "
    	                    + "a serious internal problem while trying to communicate with SimpleDB, "
    	                    + "such as not being able to access the network.");
    	            System.out.println("Error Message: " + ace.getMessage());
    	        }
    			
    			
    		}
	 };
	 t.start();
    }

    public static String[] getItemNamesForDomain( String domainName ) {
    	System.out.println("reached getItemNamesForDomain 1");
		SelectRequest selectRequest = new SelectRequest( "select itemName() from `" + domainName + "`" ).withConsistentRead( true );
		System.out.println("reached getItemNamesForDomain 2");
		List items = getInstance().select( selectRequest ).getItems();
		System.out.println("reached getItemNamesForDomain 3");
		
		String[] itemNames = new String[ items.size() ];
		for ( int i = 0; i < items.size(); i++ ) {
			itemNames[ i ] = ((Item)items.get( i )).getName();
		}
		
		return itemNames;
	}

	public static HashMap<String,String> getAttributesForItem( String domainName, String itemName ) {
		GetAttributesRequest getRequest = new GetAttributesRequest( domainName, itemName ).withConsistentRead( true );
		GetAttributesResult getResult = getInstance().getAttributes( getRequest );	
		
		HashMap<String,String> attributes = new HashMap<String,String>(30);
		for ( Object attribute : getResult.getAttributes() ) {
			String name = ((Attribute)attribute).getName();
			String value = ((Attribute)attribute).getValue();
			attributes.put( name, value );
		}

		return attributes;
	}

 }