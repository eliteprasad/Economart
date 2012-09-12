package org.embedded.economart;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;


public class LocationList extends AsyncTask<String, Long,String> {
	
	private ProgressDialog dialog;
	private MyInterface ctx;
	public Double lat = 0.0;
	public Double lon = 0.0;
	public String LocationListAddr = null;

	public LocationList(MyInterface ctx) {
		this.ctx = ctx;
	}

	 
	
	@Override
	
	protected void onPreExecute() 
	{
		super.onPreExecute();

		dialog = new ProgressDialog((Context)ctx);
		dialog.setMessage("Loading ....");

		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.show();
	}
	
	protected String doInBackground(String... params) 
	{
		
	Context context = (Context)ctx;
	LocationManager locationManager = null;
	LocListener lt = new LocListener();
	locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);		
	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, lt);
		
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Addresssssss: " + lt.locName);
//		return lt.locName;
		
		//LocationListAddr = lt.locName
		//return addget.myFunc(ctx, lat, lon);*/
/*	   try {
		Thread.sleep(5000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	   return "hei";
		
	}

	@Override
	protected void onPostExecute(String e){
		dialog.dismiss();
		ctx.onData(e);
	}



	
}