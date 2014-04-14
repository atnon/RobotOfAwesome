package com.example.theinterface;

import java.text.DecimalFormat;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;
import android.provider.Contacts.Intents;

public class MainActivity extends ActionBarActivity {
	
	//bara för att testa
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
    

    public void changeIt (View view, float x, float y){
    	TextView theXView = (TextView)findViewById(R.id.xView);
    	TextView theYView = (TextView)findViewById(R.id.yView);
    	TextView theRView = (TextView)findViewById(R.id.rView);
    	TextView theAngView = (TextView)findViewById(R.id.angView);
    	
    	x = (x-(480/2));
    	y = (-y+(800/2)+55);
    	double dx = 0.1*x;
    	double dy = 0.1*y;
    	
    	double r = 0;
    	if (!(Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2))>10)){
    		r = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    	}else{
    		r= 10;
    	}
    	
    	double ang = 57.3*Math.atan2(dy,dx);
    	
    //Print out with just two decimals
    	//change to two decimals
    	DecimalFormat newFormat = new DecimalFormat("#.##");
    	
    	double rWTD =  Double.valueOf(newFormat.format(r));
    	double angWTD =  Double.valueOf(newFormat.format(ang));
    	double xWTD =  Double.valueOf(newFormat.format(dx));
    	double yWTD =  Double.valueOf(newFormat.format(dy));
    	
    	//Turn the doubles into strings
    	String xS = String.valueOf(xWTD);
    	String yS = String.valueOf(yWTD);
    	String rS = String.valueOf(rWTD);
    	String angS = String.valueOf(angWTD);
    	
    	//print out the strings into the corresponding views
    	theXView.setText(xS);
    	theYView.setText(yS);
    	theRView.setText(rS);
    	theAngView.setText(angS);
    }
    
    public boolean onTouchEvent(MotionEvent event){
    	
    	float x = event.getX();
    	float y = event.getY();
    	changeIt(getCurrentFocus(),x,y);
    	
    	
    	return true;
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
