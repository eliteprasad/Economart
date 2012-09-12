package org.embedded.economart;

//import org.example.hello.Hello.MyOnItemSelectedListener;

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


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

public class Splash extends Activity
{
	protected boolean _active = true;
	protected int _splashTime = 15000; // time to display the splash screen in ms
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.splash);
	 
	    // thread for displaying the SplashScreen
	    Thread splashTread = new Thread() {
	        @Override
	        public void run() {
	            try {
	                int waited = 0;
	                while(_active && (waited < _splashTime)) {
	                    sleep(100);
	                    if(_active) {
	                        waited += 100;
	                    }
	                }
	            } catch(InterruptedException e) {
	                // do nothing
	            } finally {
	                finish();
	                Intent intent = new Intent(Splash.this, economart.class);
	                startActivity(intent);
	                stop();
	            }
	        }
	    };
	    splashTread.start();
	}	

	public boolean onTouchEvent(MotionEvent event) 
	{
	    if (event.getAction() == MotionEvent.ACTION_DOWN) 
	    {
	        _active = false;
	    }
	    
	    return true;

	
	 }
}