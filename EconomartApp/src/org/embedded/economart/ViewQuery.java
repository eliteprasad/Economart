package org.embedded.economart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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

public class ViewQuery extends ListActivity
{
	
	// Global Variables
	
	private Handler mHandler;
	private boolean credentials_found;
	private static AmazonSimpleDB sdb = null;
	private Button postButton;
	private Button backButton;
	private String[] itemNames;
	private String newItem = "newItem";
	private HashMap<String,String> attributes;
	private ProgressDialog pd;
		
	public String dateFromDb = null;
	public int stalenessParam = 20;
	public static int count;
	public static BasicAWSCredentials credentials = null;
	public Location loc;
	public double lat;
	public double lon;	
	public final int geonetThresh = 11000; //default threshold set to 4000 meters 
	public int ctr = 0;
	public String[] queryList;	
	public String[] queryID;
	public String[] prod;	
	public List<String> qList = new ArrayList<String>();	
	public List<String> freshqList = new ArrayList<String>();
	public List<String> freshProdList = new ArrayList<String>();
    public List<String> freshQidList = new ArrayList<String>();

	
	Spinner spinner;
    // Functions
    
	 @Override
	    public void onCreate(Bundle savedInstanceState) 
	 	{
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.viewquery);
	        mHandler = new Handler();
	        
	        startGetCredentials();
	        
	        Bundle b = getIntent().getExtras();
	        lat = b.getDouble("latitude");
	        lon = b.getDouble("longitude");	           
	               
	        ImageView image = (ImageView) findViewById(R.id.test_image);
	        TextView text = (TextView) findViewById(R.id.defaulttext);   
	        pd = ProgressDialog.show(this, "", "Looking for what people around you want..", true, false);
	        callMyFuncs();    
	  	       
	        // Register the onClick listener with the implementation above
	       	      
	        ListView lv = getListView();
	        lv.setTextFilterEnabled(true);

	        lv.setOnItemClickListener(new OnItemClickListener() 
	        {
	          public void onItemClick(AdapterView<?> parent, View view,
	              int position, long id) {
	        	  
	        	  Intent intent = new Intent(ViewQuery.this, ViewResponse.class);
	        	  Bundle b = new Bundle();
	        	        	  
	        	  b.putString("queryid", freshQidList.get(position));
	        	  b.putString("queryposted", freshProdList.get(position));
	        	  b.putDouble("latitude", lat);
	        	  b.putDouble("longitude", lon);        	  
	        	  
	        	  System.out.println("Vals being passed from viewqueries to viewresponses = " + lat + lon);
	        	  
	        	  intent.putExtras(b);
	        	  startActivity(intent);	        	  
	           
	          }
	        });
	        
	        
	    } //end of onCreate()
	 
	 	    
	    private void callMyFuncs() 
	    {    	
	    	Thread t = new Thread() 
	    	{
	    		@Override
	    		public void run()
	    		{
	    	        try 
	    	        {            
	    	        	calculateGeoNet();  	        	
	    	        	handler.sendEmptyMessage(0);
	    	        }
	    	        
	    	        catch ( Exception exception ) 
	    	        {
	    	        	System.out.println("Error in calling the display function!!!!");
	    	        }
	    			
	    		}    		
	    		    		
	    		
	    	};
	    	t.start();	    	
	    	
	    }
	    
	    private Handler handler = new Handler() 
	    {
            @Override
            public void handleMessage(Message msg) 
            {
                   pd.dismiss();
                   Collections.reverse(freshqList);
                   Collections.reverse(freshQidList);
                   Collections.reverse(freshProdList);
                   
                   ArrayList<String> EmptyList = new ArrayList<String>();
       	           EmptyList.add("No one has asked anything yet! Check back later! :)");
       	           
                   if(freshqList.isEmpty())
           	        	setListAdapter(new ArrayAdapter(ViewQuery.this, android.R.layout.simple_list_item_1, EmptyList));
           	        else	        
           	        	setListAdapter(new ArrayAdapter(ViewQuery.this, android.R.layout.simple_list_item_1,freshqList));
                   System.out.println("Reached here..");

            }
    };
   
	    public void calculateGeoNet()
	    {

	        String product = null;
	        String id = null;
	    	
	    	// Database Query
	    	
	    	SelectRequest selectRequest = new SelectRequest( "select itemName() from `Queries`" ).withConsistentRead( true );
			List items = getInstance().select(selectRequest).getItems();	

	        
	        String[] itemNames = new String[items.size()];
			
			System.out.println("SIZE ---------------------" + items.size());
			
				for(int i = 0; i < items.size(); i++)
				{
					itemNames[i] = ((Item)items.get(i)).getName();
					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Item NAME IS :"+itemNames[i]);
				}         
	        
	        queryList = new String[itemNames.length];
	        queryID = new String[itemNames.length];
	        prod = new String[itemNames.length];
	        	        
	        
	        int i;	
	        
	        Double lati = 0.0;
	        Double longi = 0.0;
	        
	        
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
	                                               
	                        product = attributes.get("queryposted");
	                        id = attributes.get("queryid");
	                        
	                        System.out.println("QueryID "+id+ "is:  " + attributes.get("queryid"));
	                        System.out.println("What is the price of " + product + "?");
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
	            		
	            		prod[i] = product;
	            		queryList[i] = "What is the price of " + product + "?";
	            		
	            		dateFromDb = attributes.get("Date");
	            		
	            		String[] da = dateFromDb.split("/");
	            		
	            		// ================================================== Date Calculations ==============================================		
	    		        Calendar calendar1 = Calendar.getInstance();
	    		        Calendar calendar2 = Calendar.getInstance();
	    		        
	    		        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	    		 	    //get current date time with Date()
	    		 	    Date date = new Date();
	    		 	   
	    		 	    String[] d = dateFormat.format(date).split("/");
	    		 	   
	    		 	    System.out.println(dateFormat.format(date));
	    		 	   
	    		 	    System.out.println(Integer.parseInt(d[0]));
	    		 	    System.out.println(Integer.parseInt(d[1]));
	    		 	    System.out.println(Integer.parseInt(d[2]));
	    		        
	    		        calendar1.set(Integer.parseInt(da[0]),Integer.parseInt(da[1]),Integer.parseInt(da[2]));
	    		        calendar2.set(Integer.parseInt(d[0]),Integer.parseInt(d[1]), Integer.parseInt(d[2]));
	    		        
	    		        long milliseconds1 = calendar1.getTimeInMillis();
	    		        long milliseconds2 = calendar2.getTimeInMillis();
	    		        long diff = milliseconds2 - milliseconds1;
	    		        
	    				long diffDays = diff / (24 * 60 * 60 * 1000);
	    				
	    				System.out.println("The difference between the two days is: " + diffDays);
	    				
	    		// ============================================= Check if the queries are stale =====================================================================		
	    		   
	    				qList.add("What is the price of " + product);
	    				
	            		if(diffDays < stalenessParam)
	            		{
	            			freshqList.add("What is the price of " + product);
	            			freshQidList.add(attributes.get("queryid"));
	            			freshProdList.add(product);
	            		} 
	            		
	            		System.out.println("QList " + queryList[i]);
	            		
	            	
	            }
	    	}
	        
	          
	        
	        System.out.println("^&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&^^^^^^^^^^^^^^^^^^^^^^^^^&&&&&&&&&&&&&&&&&&&&&&&&&& REACHED");
	        
	 
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
	    			//economart.this.mHandler.post(postResults);
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
				//System.out.println("Name = "+name);
				//System.out.println("Value = "+value);
				attributes.put( name, value );
			}

			return attributes;
		}
		
}
