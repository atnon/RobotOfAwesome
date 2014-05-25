/**
 * MotorControl is the glue that binds the output from the UI
 * to the actual commands that the harware can act upon.
 * 
 * @author Anton Landberg (git@atnon.se)
 */
package com.example.theinterface;

import android.util.Log;

public class MotorControl {
	
	private Sendstring uartCom;
	private static final String TAG = "MotorControl";
	
	public MotorControl(Sendstring uartCom) {
		this.uartCom = uartCom;
	}

	/**
	 * Translates the given parameters into the speed of the two motors 
	 * of the robot. Turning the robot is accomplished through applying 
	 * different speeds to the motors.
	 * 
	 * @param speed	Speed of the robot, normalized to the range [0,1].
	 * @param angle Angle determining the speed difference and direction.
	 */
	public void setVelocity(double speed, double angle) {
		if((speed > 1) || (speed < 0)) {
			/* If the speed setting is not in the range [0,1],
			 * we should throw an exception. */
			throw new IllegalArgumentException("Speeds larger than 1.0 are not allowed.");
		}
		
		/* Calculate the velocity and difference in speed between motors. 
		 * Velocity may be negative. */
		double velocity = Math.sin(angle)*speed;
		double speedDifference = speed*Math.cos(angle);
		
		/* Calculate the final speeds, split the speed difference between the motors. */
		double leftSpeedRaw = velocity + speedDifference/2;
		double rightSpeedRaw = velocity - speedDifference/2;
		
		/* Clip signals to range [-1, 1] */
		double leftSpeed = this.clipRange(leftSpeedRaw, -1, 1);
		double rightSpeed = this.clipRange(rightSpeedRaw, -1, 1);
		
		/* Generate control strings to send to the motor controller. */
		byte[] leftSpeedString = generateControlString(2, leftSpeed);
		byte[] rightSpeedString = generateControlString(1, rightSpeed);
		
		/* If the controller is connected, send the control strings.*/
		if(this.uartCom.accessory_attached) {
			this.uartCom.SendData(leftSpeedString.length, leftSpeedString);
			this.uartCom.SendData(rightSpeedString.length, rightSpeedString);
		}
	}
	
	/**
	 * Takes the motor number and the speed to set, maps the speed [-1, 1] to [-127, 127]
	 * and generates the control string neccessary for communication
	 * with the motor controller.
	 * 
	 * @param motorNum	Number of the motor on the robot, 1 or 2.
	 * @param speed		Speed to set to the assigned motor.
	 * @return			Byte array containing the control string.
	 */
	public byte[] generateControlString(int motorNum, double speed) {
		int motorSpeed = (int) Math.floor(127*speed + 0.5); /* Round to closest int. */
		String preparedString = String.format("set m%dspeed %d\n", motorNum, motorSpeed);
		byte[] byteString = preparedString.getBytes();
		return byteString;
	}
	
	/**
	 * Clips the given value to the range[min, max].
	 * 
	 * @param value	Input value
	 * @param min	Minimum value
	 * @param max	Maximum value
	 * @return		Clipped value
	 */
	public double clipRange(double value, double min, double max) {
		if(value > max) {
			return Math.min(value, max);
		} else {
			return Math.max(value, min);
		}
	}
	
}
