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

package com.se.chalmers.roa;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.se.chalmers.roa.R;

/**
 * This is the main Activity that displays the current chat session.
 */
public class Interfacet extends Activity {
	ImageView circleView;
	
    /* Message types sent from the TheBluetoothConnection Handler */
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    /* Key names received from the TheBluetoothConnection Handler */
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    /* Intent request codes */
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    /* Layout Views */
    private TextView mTheConnStatus;

    /* Name of the connected device */
    private String mConnectedDeviceName = null;
     
    /* String buffer for outgoing messages */
    private StringBuffer mOutStringBuffer;
    /* Local Bluetooth adapter */
    private BluetoothAdapter mBluetoothAdapter = null;
    /* Member object for the chat services */
    private TheBluetoothConnection mBluetoothConnection = null;
    
    private Pattern extractionPattern = Pattern.compile("angle\\{(.*?)\\};radius\\{(.*?)\\};");
    
    public Sendstring uartInterface;
	public MotorControl control;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        /* Set up the window layout */
        setContentView(R.layout.main);

        /* Set up the text view containing bluetooth connection information. */
        mTheConnStatus = (TextView) findViewById(R.id.tvTheConnStatus);
        
        /* Get local Bluetooth adapter */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /* If the adapter is null, then Bluetooth is not supported */
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        this.uartInterface = new Sendstring(this, null);
        this.uartInterface.ResumeAccessory();
        this.uartInterface.SetConfig(57600, (byte)8, (byte)1, (byte)0, (byte)0);
        this.control = new MotorControl(uartInterface);
        
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
        			x = (touchX - (imageWidth/2))/(imageWidth/2);
        			y = -((touchY - (imageHeight/2))/(imageHeight/2));
        			
        			/* Calculate vector length, max is 1. */
        			double vectorLength = Math.sqrt(x*x + y*y);
        			radius = Math.min(vectorLength, 1);
        			
        			/* Calculate vector angle */
        			angle = Math.atan2(y, x);
    			}
    			
    			/* format the doubles */
    			String strRadius =  doubleFormatter(radius);
    			String strAngle = doubleFormatter(angle);
    			
    	    	control.setVelocity(radius, angle);
    	    	
    	    	/* present it as output */
    			TextView theRView = (TextView) findViewById(R.id.tvTheLength);
    	    	TextView theAngView = (TextView) findViewById(R.id.tvTheAngle);
    			theRView.setText(strRadius);
    	    	theAngView.setText(strAngle);
    			
    	    	/* send it to the receiving device
    	    	 * use this format so that the setOutput method can translate it to separate values,
    	    	 * one for angle and  one for radius */
    	    	sendMessage("angle{"+ strAngle + "};radius{" + strRadius + "};");
  
    			return true;  			
        	}
		});
    }
    
    /**
     * removes extra decimals. Only two are needed. 
	 * Also, use dots as decimal separator instead of commas. 
	 * Finally, use - as minus sign instead of the wider type .
     * @param d  A double to format.
     * @return a formatted string.
     */
    private String doubleFormatter (Double d){
		DecimalFormatSymbols df = new DecimalFormatSymbols();
		df.setDecimalSeparator('.');
		df.setMinusSign('-');
    	DecimalFormat style = new DecimalFormat("#.##",df);
    	String output = style.format(d);
    	return output;
    }

    @Override
    public void onStart() {
        super.onStart();

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

        /* Initialize the BluetoothChatService to perform bluetooth connections */
        mBluetoothConnection = new TheBluetoothConnection(this, mHandler);

        /* Initialize the buffer for outgoing messages */
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* Stop the Bluetooth chat services */
        if (mBluetoothConnection != null) mBluetoothConnection.stop();
    }

    private void ensureDiscoverable() {
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
            /* Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();*/
            return;
        }

        /* Check that there's actually something to send */
        if (message.length() > 0) {
            /* Get the message bytes and tell the BluetoothChatService to write */
            byte[] send = message.getBytes();
            mBluetoothConnection.write(send);
            
            /* Reset out string buffer to zero  */
            mOutStringBuffer.setLength(0);
            
        }
    }

    /* The Handler that gets information back from the BluetoothChatService */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case TheBluetoothConnection.STATE_CONNECTED:
                    mTheConnStatus.setText(R.string.title_connected_to);
                    mTheConnStatus.append(mConnectedDeviceName);
                    break;
                case TheBluetoothConnection.STATE_CONNECTING:
                	mTheConnStatus.setText(R.string.title_connecting);
                    break;
                case TheBluetoothConnection.STATE_LISTEN:
                case TheBluetoothConnection.STATE_NONE:
                	mTheConnStatus.setText(R.string.title_not_connected);
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
    
    public static class Vector {
    	public double angle;
    	public double radius;
    }
    
    public Vector parseString(String msg) {
    	/* extractionPattern = Pattern.compile("angle\\{(.*?)\\};radius\\{(.*?)\\}"); */
    	/* Run a regex search on the received string. */
    	Matcher m = extractionPattern.matcher(msg);
    	
    	if(m.find()) {
    		/* If the string is valid and there is a match, 
    		 * parse the matches into doubles. */
    		Vector result = new Vector();
    		
    		try{result.angle = Double.parseDouble(m.group(1));}catch(NumberFormatException e){};
    		try{result.radius = Double.parseDouble(m.group(2));}catch(NumberFormatException e){};
    		
    		return result;
    	} else {
    		return null;
    	}
    	
    }
    
    /*Translates the output from a string to a radius and an angle */
    public void setOutput(String msg){ 
    	
    	Vector motorData = parseString(msg);
    	if(motorData != null) {
	    	/* Set the motor speeds accordingly. */
	    	control.setVelocity(motorData.radius, motorData.angle);
	    	
	    	/* Present the data to the UI. */
	    	String strRadius = doubleFormatter(motorData.radius);
	    	String strAngle =  doubleFormatter(motorData.angle);
	    	
			TextView theRView = (TextView) findViewById(R.id.tvTheLength);
	    	TextView theAngView = (TextView) findViewById(R.id.tvTheAngle);
			theRView.setText(strRadius);
	    	theAngView.setText(strAngle);
    	}
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
