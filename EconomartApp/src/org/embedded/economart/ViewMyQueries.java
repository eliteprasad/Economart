package org.embedded.economart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import java.util.UUID;

public class ViewMyQueries extends ListActivity{
	
	private Handler mHandler;
	private boolean credentials_found;
	public static BasicAWSCredentials credentials = null;
	Spinner spinner;
	private String newItem = "newItem";
	public static int count;
	private static AmazonSimpleDB sdb = null;
	private Button postButton;
	private Button backButton;
	private String[] itemNames;
	public Location loc;
	public double lat;
	public double lon;	
	private HashMap<String,String> attributes;
	public final int geonetThresh = 11000; //default threshold set to 4000 meters 
	public int ctr = 0;
	public String[] queryList;
	public ArrayList<String> newqueryList = new ArrayList<String>();
	public String[] queryID;
	public ArrayList<String> newqueryID = new ArrayList<String>();
	public String[] prod;
	public ArrayList<String> newproduct = new ArrayList<String>();
	String deviceId = null;
	public int sz;
	String[] ResponseList = null;
	String product = null;
	String id = null;
	public List<String> ViewList = null;
	private ProgressDialog pd;
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) 
	 	{
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.myqueries);
	        mHandler = new Handler();
	        	        
	        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
	        final String tmDevice, tmSerial, tmPhone, androidId;
	        tmDevice = "" + tm.getDeviceId();
	        tmSerial = "" + tm.getSimSerialNumber();
	        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

	        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	        deviceId = deviceUuid.toString();
	        
	        System.out.println("THE ANDROID ID IS &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& " + deviceId);
	        
	        startGetCredentials();
	        
	        Bundle b = getIntent().getExtras();
	        lat = b.getDouble("latitude");
	        lon = b.getDouble("longitude");	        
	 	   
	       	           
	        pd = ProgressDialog.show(this, "", "Please wait while we get the list of things you have asked for..", true, false);
	        
	        callMyFuncs();
	        
	        ImageView image = (ImageView) findViewById(R.id.test_image);
	        TextView text = (TextView) findViewById(R.id.defaulttext);
	        
	        // Register the onClick listener with the implementation above
	       	      
	        ListView lv = getListView();
	        lv.setTextFilterEnabled(true);

	        lv.setOnItemClickListener(new OnItemClickListener() 
	        {
	              public void onItemClick(AdapterView<?> parent, View view,
	              int position, long id) {
	        	  
	        	  Intent intent = new Intent(ViewMyQueries.this, ViewMyResponses.class);
	        	  Bundle b = new Bundle();
	        	        	  
	        	  b.putString("queryid", newqueryID.get(position));
	        	  b.putString("queryposted", newproduct.get(position));
	        	  b.putDouble("latitude", lat);
	        	  b.putDouble("longitude", lon);
	        	  
	        	  System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ being passed: " + lat + lon);
	        	  
	        	  
	        	  intent.putExtras(b);
	        	  startActivity(intent);
	        	          
	           
	          }
	        });

	  }
	 
	 
	 public void callMyFuncs() 
	    {    	
	    	Thread t1 = new Thread() 
	    	{
	    		@Override
	    		public void run()
	    		{
	    	        try 
	    	        {            
	    	        	displayEntries();    	    	        	
	    	        	handler.sendEmptyMessage(0);
	    	        }
	    	        
	    	        catch ( Exception exception ) 
	    	        {
	    	        	System.out.println("Error in calling the display function!!!!");
	    	        }
	    			
	    		}    		
	    		    		
	    		
	    	};
	    	t1.start();	    	
	    	
	    }
	    
	    private Handler handler = new Handler() 
	    {
         @Override
         public void handleMessage(Message msg) 
         {
                pd.dismiss();
                
                Collections.reverse(newqueryList);
                Collections.reverse(newqueryID);
                Collections.reverse(newproduct);                

                ArrayList<String> EmptyList = new ArrayList<String>();
    	           EmptyList.add("Sorry. No responses have been posted to the query just yet. We request you to be patient! :)");
    	           
                if(newqueryList.isEmpty())
        	        	setListAdapter(new ArrayAdapter(ViewMyQueries.this, android.R.layout.simple_list_item_1, EmptyList));
        	        else	        
        	        	setListAdapter(new ArrayAdapter(ViewMyQueries.this, android.R.layout.simple_list_item_1,newqueryList));
                System.out.println("Reached here..");

         }
 };

	 
	public void displayEntries()
	 {
			
			System.out.println("ENTERED FUNCTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			
			SelectRequest selectRequest = new SelectRequest( "select itemName() from `MyQueries` where `userdeviceid` = '"+ deviceId +"'" ).withConsistentRead(true);
			List items = getInstance().select(selectRequest).getItems();
			
								
			String[] itemNames = new String[items.size()];
			
			queryList = new String[items.size()];
			queryID = new String[itemNames.length];
			prod = new String[itemNames.length];
			
			System.out.println("SIZE ---------------------" + items.size());
			
			System.out.println("LEEENGGGTHHHHHHHHHHHHHHHHHHH 2 " + items.size());
			
				for(int j = 0; j < items.size(); j++)
				{
					itemNames[j] = ((Item)items.get(j)).getName();
				}         
	        	        
				System.out.println("LEEENGGGTHHHHHHHHHHHHHHHHHHH 3 " + items.size());
				
	        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ item names length " + items.size());
	        
	        int i;
	        	   	        	
	        for(i = 0; i < items.size(); i++)
	        {
	             GetAttributesRequest getRequest = new GetAttributesRequest( "MyQueries", itemNames[i] ).withConsistentRead( true );
		         GetAttributesResult getResult = getInstance().getAttributes( getRequest );    
		            
		         HashMap<String,String> attributes = new HashMap<String,String>(30);
		         
		         for(Object attribute : getResult.getAttributes()){
		        	 try
	                    {
	                    	String name = ((Attribute)attribute).getName();
	                    
	                        String value = ((Attribute)attribute).getValue();
	                        attributes.put(name, value);

	                    }
	                    
	                    catch(Exception e)
	                    {
	                    	e.printStackTrace();
	                    }
	                    	                    
	                    
		         }
		         
                 id = attributes.get("queryid");
                 queryID[i] = id;                 
                 newqueryID.add(id);
                 System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@QueryID for user is "+queryID[i]);
                 
	        }
			
			// Obtaining information from Queries table
	        
	        for(int j = 0; j < queryID.length; j++) 
			{
		        selectRequest = new SelectRequest( "select itemName() from `Queries` where `queryid` = '"+ queryID[j] +"'" ).withConsistentRead(true);
				items = getInstance().select(selectRequest).getItems();
				String itemNamesQ = ((Item)items.get(0)).getName();
				
				GetAttributesRequest getRequest = new GetAttributesRequest( "Queries", itemNamesQ ).withConsistentRead( true );
			    GetAttributesResult getResult = getInstance().getAttributes( getRequest );    
			            
			         HashMap<String,String> attributes = new HashMap<String,String>(30);
			         
			         for(Object attribute : getResult.getAttributes()){
			        	 try
		                    {
		                    	String name = ((Attribute)attribute).getName();
		                    
		                        String value = ((Attribute)attribute).getValue();
		                        attributes.put(name, value);
		                        
		                    }
		                    
		                    catch(Exception e)
		                    {
		                    	e.printStackTrace();
		                    }
		                    	                    
		                    
			         }
			         	         
			         product = attributes.get("queryposted");
			         newproduct.add(product);
			         prod[j] = product;              
	                 queryList[j] = "What is the price of " + product + "?";
	                 newqueryList.add("What is the price of " + product + "?");
	                 System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Query List "+queryList[j]);
	            
	        }
	        
	 } 
	 

	    private void startGetCredentials() 
	    {
	    	Thread t = new Thread() {
	    		@Override
	    		public void run(){
	    	        try {            
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
	    
		    
	    public static AmazonSimpleDB getInstance() {
	        if ( sdb == null ) {
			    sdb = new AmazonSimpleDBClient( economart.credentials );
	            sdb.setEndpoint( "https://sdb.amazonaws.com:443" );  		
	        }

	        return sdb;
		}
	    
	    public static void getItemNamesCountForDomain( final String domainName ) {
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

	   	    
	    
	    public static String[] getItemNamesForDomain( String domainName ) 
	    {
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
				//System.out.println("Name = "+name);
				//System.out.println("Value = "+value);
				attributes.put( name, value );
			}

			return attributes;
		}
		
}
