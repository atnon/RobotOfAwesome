/**
 * MotorControl is the glue that binds the output from the UI
 * to the actual commands that the harware can act upon.
 * 
 * @author Anton Landberg (git@atnon.se)
 */
package com.example.theinterface;

import android.util.Log;

public class MotorControl {
	
	private Communications uartCom;
	private static final String TAG = "MotorControl";
	
	public MotorControl(Communications uartCom) {
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
		double velocity = (Math.sin(angle) < 0) ? -speed : speed;
		double speedDifference = speed*Math.cos(angle);
		
		/* Calculate the final speeds, split the speed difference between the motors. */
		double leftSpeed = velocity + speedDifference/2;
		double rightSpeed = velocity - speedDifference/2;
		
		/* Set motor speeds .*/
		this.setSpeedLeft(leftSpeed);
		this.setSpeedRight(rightSpeed);
	}
	
	/**
	 * Sets the speed of the left (M2) motor. Maps the speed [-1, 1] to [-127, 127].
	 * @param speed Speed to set to the left motor, should be in the range [-1, 1].
	 */
	private void setSpeedLeft(double speed) {
		int motorSpeed = (int) Math.floor(127*speed + 0.5); /* Round to closest int. */
		String preparedString = String.format("set m2speed %i\n", motorSpeed);
		this.uartCom.sendString(preparedString);
		Log.i(TAG, "Speed Left Command: " + preparedString);
	}
	/**
	 * Sets the speed of the right (M1) motor. Maps the speed [-1, 1] to [-127, 127].
	 * @param speed Speed to set to the left motor, should be in the range [-1, 1].
	 */
	private void setSpeedRight(double speed) {
		int motorSpeed = (int) Math.floor(127*speed + 0.5); /* Round to closest int. */
		String preparedString = String.format("set m1speed %i\n", motorSpeed);
		this.uartCom.sendString(preparedString);
		Log.i(TAG, "Speed Right Command: " + preparedString);
	}
	
}
