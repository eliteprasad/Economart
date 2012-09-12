package org.embedded.economart;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.location.Geocoder;
import java.util.UUID;

public class Deals extends ListActivity 
{
	private Handler mHandler;
	private boolean credentials_found;	
	private String newItem = "newItem";	
	private static AmazonSimpleDB sdb = null;
	private Button responseButton;
	private Button backButton;
	private String[] itemNames;
	private HashMap<String,String> attributes;
	private ProgressDialog pd;
	
	public String[] DealsList = null;
	public List<String> ViewList = null;
	public Location loc;
	public double lat;
	public double lon;		
	public int dealcount = 0;	
	public int stalenessParam = 20;
	public Double lati = 0.0;
	public Double longi = 0.0;
	public int dealdisp = 0;	
	public static BasicAWSCredentials credentials = null;
	public static int count;	
	public final int geonetThresh = 11000; //default threshold set to 4000 meters 
	public int ctr = 0;
	
	Spinner spinner;
	int sz;
	String id = null;
	String product = null;
	int lastitemName;
	int rid;
	String locName = null;
	String price = null;
	String amt = null;
	String store = null;
	String dealprod = null;
	String qty = null;
	
	//===============================================================================
	 public void onCreate(Bundle savedInstanceState) 
	 {
	       super.onCreate(savedInstanceState);
	       setContentView(R.layout.deals);
	       	       
	       Bundle b = getIntent().getExtras();
	       
	       lat = b.getDouble("latitude");
	       lon = b.getDouble("longitude");
	       
	       System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!Coordinates are :" + lat +" "+ lon);
	       
	       ImageView image = (ImageView) findViewById(R.id.test_image);
	       
	       pd = ProgressDialog.show(this, "", "The hottest deals: on your screen in a moment! ;)", true, false);
	       
	       final EditText edittext1 = (EditText) findViewById(R.id.edit1);
	       final EditText edittext2 = (EditText) findViewById(R.id.edit2);
	       final EditText edittext3 = (EditText) findViewById(R.id.edit3);
	       final EditText edittext4 = (EditText) findViewById(R.id.edit4);
	       	       
   	       this.responseButton = (Button)this.findViewById(R.id.response);
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
	        locName = address.get(0).getAddressLine(0)+ "," +address.get(0).getAddressLine(1) + "," + address.get(0).getAddressLine(2);

	        id = b.getString("queryid");
	        product = b.getString("queryposted");
	        
	        mHandler = new Handler();
	        startGetCredentials();
	        
	        // ========================================= Display function being called here ===============================================
	        callMyFuncs();
	        
	        // ==========================================================================================================================
	        
	        System.out.println("######################################################################");
	        responseButton.setOnClickListener(new View.OnClickListener() 
	        {
				
				public void onClick(View v) 
				{
	
					dealprod = edittext1.getText().toString();
					price = edittext2.getText().toString();
					qty = edittext3.getText().toString();
					store = edittext4.getText().toString();
					
					
					if(dealprod.equals("") || price.equals("") || qty.equals("") || store.equals(""))
					{
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(responseButton.getWindowToken(), 0);
						Toast.makeText(getBaseContext(),"Some field(s) have been left empty. Please fill them and hit Respond!", Toast.LENGTH_LONG).show();
					}
					
					
					else
					{	
						
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(responseButton.getWindowToken(), 0);
						edittext1.setText("");
						edittext2.setText("");
						edittext3.setText("");
						edittext4.setText("");
						// ========================================== Responses will be posted here ====================================
					
				        AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				        economart.class.getResourceAsStream("AwsCredentials.properties")));

				        System.out.println("===========================================");
				        System.out.println("Getting Started with Amazon SimpleDB");
				        System.out.println("===========================================\n");

				        try {
				            // Create a domain
				            String myDomain = "Deals";
				            				            
				            List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
				            
				         // ================================================== Date Calculations ==============================================		
				 	       
					        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
					 	    //get current date time with Date()
					 	    Date date = new Date();
					 	   
					 	    String postDate = dateFormat.format(date);
					 	    
					 	    String[] d = dateFormat.format(date).split("/");
					 	   
					 	    System.out.println(postDate);				            
				        				        	
				            data.add(new ReplaceableItem().withName("deal" + Integer.toString(dealcount+1)).withAttributes(
				            new ReplaceableAttribute().withName("dealsid").withValue(Integer.toString(dealcount+1)),
				            new ReplaceableAttribute().withName("product").withValue(dealprod),
				            new ReplaceableAttribute().withName("price").withValue(price),
				            new ReplaceableAttribute().withName("qty").withValue(qty),
				            new ReplaceableAttribute().withName("store").withValue(store),
				            new ReplaceableAttribute().withName("Date").withValue(postDate),
							new ReplaceableAttribute().withName("location").withValue(locName),
				            new ReplaceableAttribute().withName("Latitude").withValue(String.valueOf(lat)),
				            new ReplaceableAttribute().withName("Longitude").withValue(String.valueOf(lon))));
				            System.out.println("After");
				            
				            System.out.println("Data " + data);
				            
				            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  " + data);
				            
				            
				            System.out.println("Putting data into " + myDomain + " domain.\n");
				            sdb.batchPutAttributes(new BatchPutAttributesRequest(myDomain, data));
				            
				            Toast.makeText(getBaseContext(),"Your deal has been posted! Thanks! :)", Toast.LENGTH_LONG).show();
				        
				           				            
				            
				            displayEntries();
				            
				            
					        ViewList = new ArrayList<String>();
					        for(int v1 = 0; v1 < DealsList.length; v1++) 
					        {
					        	if(!(DealsList[v1] == null)) 
					        	{
					        		//dealdisp++;
					        		ViewList.add(DealsList[v1]);
					        	}
					        }
					        Collections.reverse(ViewList);
					              
					        	setListAdapter(new ArrayAdapter(Deals.this, android.R.layout.simple_list_item_1,ViewList));
				            
				            
				            
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
								
					
				        
					} //end of else    
					
				} //end of onClick
			});
	        
	        	 	   	        	       
	  } //onCreate ends here
	 
	 
	 private void callMyFuncs() 
	    {    	
	    	Thread t = new Thread() 
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
	    	t.start();	    	
	    	
	    }
	    
	    private Handler handler = new Handler() 
	    {
         @Override
         public void handleMessage(Message msg) 
         {
                pd.dismiss();
                ArrayList<String> EmptyList = new ArrayList<String>();
			    EmptyList.add("We have no deals for you at the moment! Try again later! :)");
                ViewList = new ArrayList<String>();
    	        for(int v = 0; v < DealsList.length; v++) {
    	        	if(!(DealsList[v] == null)) {
    	        		dealdisp++;
    	        		ViewList.add(DealsList[v]);
    	        	}
    	        }
    	        
    	        Collections.reverse(ViewList);
			        if(dealdisp == 0)
			        	setListAdapter(new ArrayAdapter(Deals.this, android.R.layout.simple_list_item_1, EmptyList));
			        else	        
			        	setListAdapter(new ArrayAdapter(Deals.this, android.R.layout.simple_list_item_1,ViewList));
                System.out.println("Reached here..");

         }
 };
	    

	//=================================================Display Entries=================================================
	 public void displayEntries()
	 {
		 
		 	SelectRequest selectRequest = new SelectRequest( "select itemName() from `Deals`").withConsistentRead(true);
			List items = getInstance().select(selectRequest).getItems();	
			List temp;

			
			String[] itemNames = new String[items.size()];
			
			sz = items.size();	        
			
			System.out.println("SIZE ---------------------" + items.size());
			
				for(int i = 0; i < items.size(); i++)
				{
					itemNames[i] = ((Item)items.get(i)).getName();
					dealcount++;
				}         
	        
	        
	        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ item names length " + items.size());
	        
	        int i;
	        DealsList = new String[items.size()];	          
	        	
	        
	        for(i = 0; i < items.size(); i++)
	        {
	        	GetAttributesRequest getRequest = new GetAttributesRequest( "Deals", itemNames[i] ).withConsistentRead( true );
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
		         
		         //rid = Integer.parseInt(attributes.get("ResponseID"));
		         
		         //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   " + rid);
		         
		         System.out.println("DealsID is:  " + attributes.get("dealsid"));
                 System.out.println("Product is:  " + attributes.get("product"));
                 System.out.println("Price is "+ attributes.get("price"));
                 System.out.println("Quantity "+ attributes.get("qty"));
                 System.out.println("Store "+ attributes.get("store"));
                 System.out.println("Location "+ attributes.get("location"));
                 System.out.println("Date " + attributes.get("Date"));
                 System.out.println("Lat " + attributes.get("Latitude"));
                 System.out.println("Lon " + attributes.get("Longitude"));
                                  
                 String dateFromDb = attributes.get("Date");
                 lati = Double.valueOf(attributes.get("Latitude"));
                 longi = Double.valueOf(attributes.get("Longitude"));
         		
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
 				 
 				float[] results;
                results = new float[2];

                // =============================================== Geo-Net Computation ========================================================
                
            	loc.distanceBetween(lat, lon, lati, longi, results);      	   
            	
            	System.out.println("Lat from db " + attributes.get("Latitude"));
                System.out.println("Lon from db " + attributes.get("Longitude"));
                System.out.println("Current Lat " + lat);
                System.out.println("Current Lon " + lon);
            	System.out.println("The Distance Between the co-ordinates is: " + results[0] + " meters");
            	
            	
           		// ============================================= Check if the deals are stale and within the geo-net =====================================================================		
 		   
         		 if(diffDays < stalenessParam && results[0] <= geonetThresh)          
                	 DealsList[i] = "\n" + attributes.get("product") + ": $" + attributes.get("price") + " for " + attributes.get("qty") + " lbs " + "at " + attributes.get("store") + "\n(" + attributes.get("location") + " ) \n" ;
            	
	        }
	        
	 } 
	
	private void startGetCredentials() {
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
	//==============================================Credentials=====================================================
	
	 public static AmazonSimpleDB getInstance() {
	        if ( sdb == null ) {
			    sdb = new AmazonSimpleDBClient( economart.credentials );
	            sdb.setEndpoint( "https://sdb.amazonaws.com:443" );  		
	        }

	        return sdb;
		}
	
	 //==============================================Get Items for domain!================================================
	 
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
	    
 //================================================Get Attributes for each Item================================================================
	    
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
	 
	//==============================================================================================================
	
	
}