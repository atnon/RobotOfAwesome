package com.example.theinterface;

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
    	
    	x = (x-(480/2));
    	y = (-y+(800/2));
    	double dx = 0.1*x;
    	double dy = 0.1*y;
    	
    	
    	if (dx>=10){
    		dx=10;
    	}
    	else if (dx<=-10){
    		dx = -10;
    	}
    	if (dy>=10){
    		dy=10;
    	}
    	else if (dy<=-10){
    		dy = -10;
    	}
    		
    	String xS = String.valueOf(dx);
    	String yS = String.valueOf(dy);
    	theXView.setText(xS);
    	theYView.setText(yS);
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
