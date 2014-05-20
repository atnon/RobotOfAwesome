/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.theinterface;

import java.text.DecimalFormat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class Interfacet extends Activity {
	ImageView circleView;
	
	
    // Debugging
    private static final String TAG = "BluetoothCon";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTitle;

    // Name of the connected device
    private String mConnectedDeviceName = null;
     
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private TheBluetoothConnection mBluetoothConnection = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
       
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
        /* Get local Bluetooth adapter */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /* If the adapter is null, then Bluetooth is not supported */
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        /* Send the angle and radius when touching the circle */
        circleView = (ImageView) findViewById(R.id.inputArea);
		circleView.setOnTouchListener(new OnTouchListener() {
        	@Override
    		public boolean onTouch(View v, MotionEvent event) {
        		/* set default values */ 
        		double x = 0; 
        		double y = 0; 
        		double radius = 0;
        		double angle = 0;
        		
    			if(event.getAction() == android.view.MotionEvent.ACTION_UP) {
    				/* The user is no longer touching the action area. 
    				 * Reset the values to 0. */
    				x = 0;
    				y = 0;
    				radius = 0;
    				angle = 0;
    				
    			} else {
    				
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
        			
        			/* Calculate vector angle */
        			angle = Math.atan2(y, x);
    			}
    			
    	    	DecimalFormat style = new DecimalFormat("#.##");
    	    	
    	    	/* present it as output */
    			TextView theRView = (TextView) findViewById(R.id.tLength);
    	    	TextView theAngView = (TextView) findViewById(R.id.tAngle);
    			theRView.setText(style.format(radius));
    	    	theAngView.setText(style.format(angle));
    			
    	    	/* send it to the receiving device
    	    	 * use this format so that the setOutput method can translate it to separate values,
    	    	 * one for angle and  one for radius */
    	    	sendMessage(style.format(radius) + "}" + style.format(angle) + "}");
  
    			return true;  			
        	}
		});

        
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        /* If BT is not on, request that it be enabled.
         * setupChat() will then be called during onActivityResult */
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        /* Otherwise, setup the chat session*/
        } else {
            if (mBluetoothConnection == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        /* Performing this check in onResume() covers the case in which BT was
         * not enabled during onStart(), so we were paused to enable it...
         * onResume() will be called when ACTION_REQUEST_ENABLE activity returns. */
        if (mBluetoothConnection != null) {
            /* Only if the state is STATE_NONE, do we know that we haven't started already */
            if (mBluetoothConnection.getState() == TheBluetoothConnection.STATE_NONE) {
              /* Start the Bluetooth chat services*/
            	mBluetoothConnection.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        /* Initialize the BluetoothChatService to perform bluetooth connections */
        mBluetoothConnection = new TheBluetoothConnection(this, mHandler);

        /* Initialize the buffer for outgoing messages */
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* Stop the Bluetooth chat services */
        if (mBluetoothConnection != null) mBluetoothConnection.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        /* Check that we're actually connected before trying anything */
        if (mBluetoothConnection.getState() != TheBluetoothConnection.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        /* Check that there's actually something to send */
        if (message.length() > 0) {
            /* Get the message bytes and tell the BluetoothChatService to write */
            byte[] send = message.getBytes();
            mBluetoothConnection.write(send);
            
            /* Reset out string buffer to zero and clear the edit text field */
            mOutStringBuffer.setLength(0);
            
        }
    }

    /* The Handler that gets information back from the BluetoothChatService */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case TheBluetoothConnection.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    break;
                case TheBluetoothConnection.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case TheBluetoothConnection.STATE_LISTEN:
                case TheBluetoothConnection.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                /* construct a string from the valid bytes in the buffer */
                String readMessage = new String(readBuf, 0, msg.arg1);
                setOutput(readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                /* save the connected device's name */
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    
    /*Translates the output from a string to a radius and an angle */
    public void setOutput(String msg){ 
    	/* set default values */
    	String rad = "";
    	String ang = "";
    	boolean addToRad = true;
    	char[] c = msg.toCharArray();
    	
    	/*Go through every char and add the integers for the angle to the angle
    	 * and the same for the radius */
    	for (int i = 0;i<c.length;i++){
    		if((c[i] != '}')&&(addToRad)){
    			rad += c[i];
    		}else if(c[i] == '}'){
    			if (!addToRad){
    				break;
    			}
    			addToRad = false;
    		}
    		else {
    			ang+=c[i];
    		}
    	}
    	
    	/*Present it */ 
    	TextView tv = (TextView) findViewById(R.id.vRadius);
        tv.setText(rad);
        TextView tv2 = (TextView) findViewById(R.id.vAngle);
        tv2.setText(ang);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            /* When DeviceListActivity returns with a device to connect */
            if (resultCode == Activity.RESULT_OK) {
                /* Get the device MAC address */
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                /* Get the BLuetoothDevice object */
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                /* Attempt to connect to the device */
                mBluetoothConnection.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            /* When the request to enable Bluetooth returns */
            if (resultCode == Activity.RESULT_OK) {
                /* Bluetooth is now enabled, so set up a chat session */
                setupChat();
            } else {
                /* User did not enable Bluetooth or an error occured */
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            /* Launch the DeviceListActivity to see devices and do scan */
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            /* Ensure this device is discoverable by others */
            ensureDiscoverable();
            return true;
        }
        return false;
    }

}