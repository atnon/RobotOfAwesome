package com.example.theinterface;

import java.text.DecimalFormat;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            
            /* Find the ImageView containing the circle. */
        	ImageView circleView = (ImageView) rootView.findViewById(R.id.imageView1);

        	/* Create an OnToch listener with its associated onTouch method. */
        	circleView.setOnTouchListener(new OnTouchListener() {
        		
        		private MotorControl control = new MotorControl();
        		
        		@Override
        		public boolean onTouch(View v, MotionEvent event) {
        			
        			/* Set default values. */
        			double x = 0; 
        			double y = 0; 
        			double radius = 0;
        			double angle = 0;
        			
        			if(event.getAction() == android.view.MotionEvent.ACTION_UP) {
        				/* The user is no longer touching the action area. 
        				 * Reset the values to 0. */
        				
        			} else {
        				/* User is touching image. */
        				ImageView circleView = (ImageView) getView().findViewById(R.id.imageView1);
            			
            			/* Get touch coordinates. 
            			 * The coordinates are relative to the top left corner of the image. */
            			double touchX = event.getX();
            			double touchY = event.getY();
            			
            			/* Calculate image height and width. */
            			double imageWidth = circleView.getWidth();
            			double imageHeight = circleView.getHeight();
            			
            			/* Calculate the resulting vector and normalize it in order to compensate for 
            			 * different display sizes. */
            			x = (touchX - (imageWidth/2))/(circleView.getWidth()/2);
            			y = -((touchY - (imageHeight/2))/(imageHeight/2));
            			
            			/* Calculate vector length, max is 1. */
            			double vectorLength = Math.sqrt(x*x + y*y);
            			radius = Math.min(vectorLength, 1);
            			
            			/* Calculate vector angle in radians. */
            			angle = Math.atan2(y, x);
        			}
        			
        			control.setVelocity(radius, angle);
        			
        			/* Print the values to the screen. */
        			TextView theRView = (TextView) getView().findViewById(R.id.rView);
        	    	TextView theAngView = (TextView) getView().findViewById(R.id.angView);
        	    	TextView theXView = (TextView) getView().findViewById(R.id.xView);
        	    	TextView theYView = (TextView) getView().findViewById(R.id.yView);
        	    	
        	    	DecimalFormat style = new DecimalFormat("#.##");
        	    	
        			theRView.setText(style.format(radius));
        	    	theAngView.setText(style.format(angle));
        	    	theXView.setText(style.format(x));
        	    	theYView.setText(style.format(y));
        			
        			return true;
        		}
        	});
            
            return rootView;
        }
    }

}
