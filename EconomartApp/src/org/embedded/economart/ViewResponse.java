package org.embedded.economart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import android.widget.Toast;
import android.location.Geocoder;
import java.util.UUID;

public class ViewResponse extends ListActivity {
	private Handler mHandler;
	private boolean credentials_found;
	private String newItem = "newItem";
	private static AmazonSimpleDB sdb = null;
	private Button responseButton;
	private Button backButton;
	private String[] itemNames;
	private HashMap<String,String> attributes;
	
	public static BasicAWSCredentials credentials = null;
	public static int count;
	public String[] ResponseList = null;
	public List<String> ViewList = null;
	public Location loc;
	public double lat;
	public double lon;	
	public int respcount = 0;
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

	 public void onCreate(Bundle savedInstanceState) 
	 {
	       super.onCreate(savedInstanceState);
	       setContentView(R.layout.response);
	       	       
	       Bundle b = getIntent().getExtras();
	       
	       lat = b.getDouble("latitude");
	       lon = b.getDouble("longitude");
	       
	       System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!Coordinates are :" + lat +" "+ lon);
	       
	       ImageView image = (ImageView) findViewById(R.id.test_image);
	       
	       final EditText edittext = (EditText) findViewById(R.id.widget27);
	       final EditText edittext2 = (EditText) findViewById(R.id.widget29);
	       final EditText edittext3 = (EditText) findViewById(R.id.widget30);
	       	       
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
	        
	        TextView prodName = (TextView) findViewById(R.id.prodname);
	        prodName.setText(product+"\n");	
	        prodName.setTextSize(24);
	        prodName.setTextColor(Color.DKGRAY);
	        
	        //Toast.makeText(this.getBaseContext(),"QueryID is " + id + " & Product is " + product + " & Location is " + locName, Toast.LENGTH_LONG).show();
	        
	        TextView prodText = (TextView) findViewById(R.id.textView1);
	        prodText.setText("Price of " + product+ ":");
	        
	        mHandler = new Handler();
	        startGetCredentials();
	        
	        ArrayList<String> EmptyList = new ArrayList<String>();
	        EmptyList.add("Sorry. No responses have been posted to the query just yet. We request you to be patient! :)");
	        
	        displayEntries();
	        ViewList = new ArrayList<String>();
	        for(int v = 0; v < ResponseList.length; v++) 
	        {
	        	if(!(ResponseList[v] == null)) 
	        	{
	        		respcount++;
	        		ViewList.add(ResponseList[v]);
	        	}
	        }
	        
	        if(respcount == 0)
	        	setListAdapter(new ArrayAdapter(ViewResponse.this, android.R.layout.simple_list_item_1, EmptyList));
	        else	        
	        	setListAdapter(new ArrayAdapter(ViewResponse.this, android.R.layout.simple_list_item_1,ViewList));
	       
	        System.out.println("######################################################################");
	        responseButton.cancelLongPress();
	        responseButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) 
				{
	
					price = edittext.getText().toString();
					amt = edittext2.getText().toString();
					store = edittext3.getText().toString();
					
					
					if(price.equals("") || amt.equals("") || store.equals(""))
					{
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(responseButton.getWindowToken(), 0);
						Toast.makeText(getBaseContext(),"Some field(s) have been left empty. Please fill them and hit Respond!", Toast.LENGTH_LONG).show();
						
					}
					
					
					else
					{	
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(responseButton.getWindowToken(), 0);
						edittext.setText("");
						edittext2.setText("");
						edittext3.setText("");
						
						
						// ========================================== Responses will be posted here ====================================
					
				        AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				        economart.class.getResourceAsStream("AwsCredentials.properties")));

				        System.out.println("===========================================");
				        System.out.println("Getting Started with Amazon SimpleDB");
				        System.out.println("===========================================\n");

				        try {
				            // Create a domain
				            String myDomain = "Response";
				            	            
				            				            
				            List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
				            
				        	String lastqid = Integer.toString(lastitemName+1);
				        	
				        	String lastrid = Integer.toString(rid+1);
				        	
				            data.add(new ReplaceableItem().withName(lastqid).withAttributes(
				            new ReplaceableAttribute().withName("QueryID").withValue(id),
				            new ReplaceableAttribute().withName("ResponseID").withValue(lastrid),
				            new ReplaceableAttribute().withName("Location").withValue(store),
				            new ReplaceableAttribute().withName("Address").withValue(locName),
				            new ReplaceableAttribute().withName("Qty").withValue(amt),
				            new ReplaceableAttribute().withName("Price").withValue(price)));

				            System.out.println("After");
				            
				            System.out.println("Data " + data);
				            
				            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  " + data);
				            
				            
				            Toast.makeText(getBaseContext(),"Your response has been posted! Thanks! :)", Toast.LENGTH_LONG).show();
				            
				            
				            System.out.println("Putting data into " + myDomain + " domain.\n");
				            sdb.batchPutAttributes(new BatchPutAttributesRequest(myDomain, data));
			            
				            displayEntries();
					        ViewList = new ArrayList<String>();
					        for(int v1 = 0; v1 < ResponseList.length; v1++) 
					        {
					        	if(!(ResponseList[v1] == null)) 
					        	{
					        		ViewList.add(ResponseList[v1]);
					        	}
					        }
					        
					        Collections.reverse(ViewList);
			                				        
					        setListAdapter(new ArrayAdapter(ViewResponse.this, android.R.layout.simple_list_item_1,ViewList));
				            
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
	 
	 
	//=================================================Display Entries=================================================
	 public void displayEntries(){
		 
		 	SelectRequest selectRequest = new SelectRequest( "select itemName() from `Response` where `QueryID` = '"+ id +"'" ).withConsistentRead(true);
			List items = getInstance().select(selectRequest).getItems();	
			List temp;


			SelectRequest selectmax = new SelectRequest( "select itemName() from `Response`" ).withConsistentRead(true);
			temp = getInstance().select(selectmax).getItems();
			
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  " + temp.size());
			
			lastitemName = temp.size();
			
			System.out.println("*********************************************************  " + lastitemName);
			
			String[] itemNames = new String[items.size()];
			
			sz = items.size();	        
			
			System.out.println("SIZE ---------------------" + items.size());
			
				for(int i = 0; i < items.size(); i++)
				{
					itemNames[i] = ((Item)items.get(i)).getName();
				}         
	        
	        
	        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ item names length " + items.size());
	        
	        int i;
	        ResponseList = new String[items.size()];	          
	        	
	        
	        for(i = 0; i < items.size(); i++){
	        
	        	
	        	GetAttributesRequest getRequest = new GetAttributesRequest( "Response", itemNames[i] ).withConsistentRead( true );
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
		         
		         rid = Integer.parseInt(attributes.get("ResponseID"));
		         
		         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   " + rid);
		         
		         System.out.println("ResponseID is:  " + attributes.get("ResponseID"));
                 System.out.println("QueryID is:  " + attributes.get("QueryID"));
                 System.out.println("The price is "+ attributes.get("Price"));
                 ResponseList[i] = "\n$" + attributes.get("Price") + " for " + attributes.get("Qty") + " lbs " + "at " + attributes.get("Location") + " (" + attributes.get("Address") + " )\n" ;
                	 System.out.println("RList " + ResponseList[i]);
                 
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