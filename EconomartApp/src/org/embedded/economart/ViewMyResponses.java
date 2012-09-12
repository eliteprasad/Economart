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

public class ViewMyResponses extends ListActivity 
{
	private Handler mHandler;
	private boolean credentials_found;
	public static BasicAWSCredentials credentials = null;
	Spinner spinner;
	private String newItem = "newItem";
	public static int count;
	private static AmazonSimpleDB sdb = null;
	private String[] itemNames;
	public String[] ResponseList = null;
	public List<String> ViewList = null;
	public Location loc;
	public double lat;
	public double lon;	
	private HashMap<String,String> attributes;
	
	public final int geonetThresh = 11000; //default threshold set to 4000 meters 
	public int ctr = 0;
	int sz;
	String id = null;
	String product = null;
	int lastitemName;
	int rid;
	String locName = null;
	String price = null;
	String amt = null;
	String store = null;
	
	public int respcount = 0;
	
	//===============================================================================
	 public void onCreate(Bundle savedInstanceState) 
	 {
	       super.onCreate(savedInstanceState);
	       setContentView(R.layout.viewmyresponses);	       
	       
	       Bundle b = getIntent().getExtras();
	       
	       lat = b.getDouble("latitude");
	       lon = b.getDouble("longitude");
	       ImageView image = (ImageView) findViewById(R.id.test_image);
	        
	          
	        id = b.getString("queryid");
	        product = b.getString("queryposted");
	        
	        TextView prodName = (TextView) findViewById(R.id.prodname);
	        prodName.setText(product+"\n");
	        prodName.setTextSize(24);
	        prodName.setTextColor(Color.DKGRAY);
	        
	        mHandler = new Handler();
	        startGetCredentials();
	        
	        ArrayList<String> EmptyList = new ArrayList<String>();
	        EmptyList.add("Sorry. No responses have been posted to your query just yet. We request you to be patient! :)");
	        
	        displayEntries();
	        ViewList = new ArrayList<String>();
	        
	        for(int v = 0; v < ResponseList.length; v++) 
	        {
	        	respcount++;
	        	if(!(ResponseList[v] == null)) 
	        	{
	        		ViewList.add(ResponseList[v]);
	        	}
	        	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~(" + ResponseList[v] + ")");
	        }
	        
	        Collections.reverse(ViewList);
	        
	        if(respcount == 0)
	        	setListAdapter(new ArrayAdapter(ViewMyResponses.this, android.R.layout.simple_list_item_1, EmptyList));
	        else	        
	        	setListAdapter(new ArrayAdapter(ViewMyResponses.this, android.R.layout.simple_list_item_1,ViewList));
	        
	      	 	   	        	       
	  } //onCreate ends here
	 
	 
	//=================================================Display Entries=================================================
	 public void displayEntries()
	 {
		 	 
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