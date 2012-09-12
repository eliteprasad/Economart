package org.embedded.economart;


//import org.example.hello.Hello.MyOnItemSelectedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

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
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class PostQuery extends Activity 
{
	private Handler mHandler;
	private boolean credentials_found;
	private String newItem = "newItem";
	private String newItemMQ = null;
	private int countQ;
	private int countMQ;
	private static AmazonSimpleDB sdb = null;
	private Button backButton;
	
	public static BasicAWSCredentials credentials = null;
	public double lat = 0.0;
	public double lon = 0.0;
	public Location loc;
	public final int geonetThresh = 11000; 
	public String dateFromDb = null;
	public int stalenessParam = 20;
	
	Spinner spinner;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.postquery);
			mHandler = new Handler();
			startGetCredentials();
			
			Bundle b = getIntent().getExtras();
			lat = b.getDouble("latitude");
			lon = b.getDouble("longitude");
			System.out.println("###############################################3Coordinates are :"+lat+" "+lon);
			
			//Display spinner
			ImageView image = (ImageView) findViewById(R.id.test_image);
			this.spinner = (Spinner) findViewById(R.id.spinner);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.products_array, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this.getApplicationContext(), "An unexpected error has occured. Please re-start your application.", Toast.LENGTH_LONG);
			
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
    public class MyOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	        if(!(parent.getItemAtPosition(pos).toString().equalsIgnoreCase("Choose a product"))) {
		         countQ = getItemNamesCountForDomain("Queries");
		         countMQ = getItemNamesCountForDomain("MyQueries");
		         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!count is "+countQ);
		         newItem = "item" + ++countQ;
		         newItemMQ = String.valueOf(++countMQ);
		         String qid = String.valueOf(countQ);
		         
		         
		         Toast toast = Toast.makeText(parent.getContext(), "Your query has been posted!", Toast.LENGTH_LONG);
		         toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		         toast.show();
		         
		         createEntry(newItem, newItemMQ, qid, parent.getItemAtPosition(pos).toString(), lat, lon );        
	        }
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    
    public static AmazonSimpleDB getInstance() {
        if ( sdb == null ) {
		    sdb = new AmazonSimpleDBClient( economart.credentials );
            sdb.setEndpoint( "https://sdb.amazonaws.com:443" );  		
        }

        return sdb;
	}
    
    public static int getItemNamesCountForDomain( String domainName ) {
		SelectRequest selectRequest = new SelectRequest( "select itemName() from `" + domainName + "`" ).withConsistentRead( true );
		List items = getInstance().select( selectRequest ).getItems();	
		
	
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ ITEM SIZE " + items.size());
		return items.size();
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
	
    
    
    public void createEntry(String itemName, String itemNameMQ, String qid, String prod , Double lat, Double lon) 
    {
   	 AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
                economart.class.getResourceAsStream("AwsCredentials.properties")));

        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SimpleDB");
        System.out.println("===========================================\n");
        
        // ====================================== Obtaining Android ID ==============================================
        
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, tmPhone, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
               
        
        System.out.println("THE ANDROID ID IS &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& " + deviceId);
        
        // ==========================================================================================================
        
        try 
        {
        	
			// ================================================== Calculate current date ==============================================		
	       
	        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	 	    //get current date time with Date()
	 	    Date date = new Date();
	 	   
	 	    String postDate = dateFormat.format(date);
	 	    
	 	    String[] d = dateFormat.format(date).split("/");
	 	   
	 	    System.out.println(postDate);
	 	   
			
			// ==================================================================================================================		
	   
			
			
			int i, duplicate = 0;
        	float distCheck[] = new float[2];
        	Double lati = 0.0;
	        Double longi = 0.0;
	        String product = null;
	        String id = null, dupID = null;
        	// Checking for duplicates
        	SelectRequest selectRequest = new SelectRequest( "select itemName() from `Queries`" ).withConsistentRead( true );
			List items = getInstance().select(selectRequest).getItems();
			String[] itemNames = new String[items.size()];
			
			System.out.println("SIZE ---------------------" + items.size());
			
			for(i = 0; i < items.size(); i++)
			{
				itemNames[i] = ((Item)items.get(i)).getName();
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Item NAME IS :"+itemNames[i]);
			}      
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
	                        //System.out.println("Name = "+name);
	                        //System.out.println("Value = "+value);
	                        attributes.put(name, value);
	                        	                        
	                        lati = Double.valueOf(attributes.get("Latitude"));
	                        longi = Double.valueOf(attributes.get("Longitude"));
	                        product = attributes.get("queryposted");
	                        id = attributes.get("queryid");
	                        dateFromDb = attributes.get("Date");
		            		
		            		String[] da = dateFromDb.split("/");
	                        
	                        // ============================== Checking for staleness of entries ====================================
	                        
	                        Calendar calendar1 = Calendar.getInstance();
		    		        Calendar calendar2 = Calendar.getInstance();
		    		        
		    		        
		    		 	   
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
		    				
	                        
	                        // ======================================================================================
	                        
	                        
	                        System.out.println("QueryID "+id+ "is:  " + attributes.get("queryid"));
	                        System.out.println("What is the price of " + product);
	                        System.out.print("near " + lati);
	                        System.out.print("and " + longi);
	                        
	                        loc.distanceBetween(lat, lon, lati, longi, distCheck);      	            	
	                    	System.out.println("The Distance Between the co-ordinates is: " + distCheck[0] + " meters");
	                        if(distCheck[0] <= geonetThresh && product.equals(prod)&& diffDays < stalenessParam) {
	                        	duplicate = 1;
	                        	dupID = id;
	                        	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Duplicate found!!");
	                        	break;
	                        }
	                        
	                    }
	                    
	                    catch(Exception e)
	                    {
	                    	
	                    }
	            }
	            if(duplicate == 1) {
	            	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Duplicate == 1");
	            	break;
	            }

	        }
	           
        	
	          //Insert into Queries table if it is not a duplicate query
        	
        	if(duplicate == 0) {
        	
		        	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@NOT A DUPLICATE QUERY");
		            // Create a domain
		            String myDomain = "Queries";
		            
		            List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();            
		        	
		            data.add(new ReplaceableItem().withName(itemName).withAttributes(
		            new ReplaceableAttribute().withName("queryid").withValue(qid),
		            new ReplaceableAttribute().withName("queryposted").withValue(prod),
		            new ReplaceableAttribute().withName("userdeviceid").withValue(deviceId),
		            new ReplaceableAttribute().withName("Latitude").withValue(lat.toString()),
					new ReplaceableAttribute().withName("Date").withValue(postDate),
		            new ReplaceableAttribute().withName("Longitude").withValue(lon.toString())));
		            
		            System.out.println("After");
		            
		            System.out.println("Data " + data);
		            
		            
		            System.out.println("Putting data into " + myDomain + " domain.\n");
		            sdb.batchPutAttributes(new BatchPutAttributesRequest(myDomain, data));
        	}
        	
        	
        	
        	/***************************** Insert into MyQueries table ****************************/
        	String myDomain = "MyQueries";
            
                       
            if(duplicate == 1) 
            {
            	qid = dupID;
            	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Duplpicate Query ID " + id);
            }
            List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();            
        	
            data.add(new ReplaceableItem().withName(itemNameMQ).withAttributes(
            new ReplaceableAttribute().withName("queryid").withValue(qid),
            new ReplaceableAttribute().withName("userdeviceid").withValue(deviceId)));
         
            
            System.out.println("After");
            
            System.out.println("Data " + data);
            
            
            System.out.println("Putting data into " + myDomain + " domain.\n");
            sdb.batchPutAttributes(new BatchPutAttributesRequest(myDomain, data));
         
        }
        catch (AmazonServiceException ase) 
        {
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
}