package com.se.chalmers.roa;

/**
 * Provides the neccessary interfaces for communication classes.
 * 
 * @author Anton Landberg (git@atnon.se)
 */

public interface Communications {
	
	/**
	 * Provides means to send strings to the recipient device
	 * associated with the object.
	 * @param string String to send to the recipient.
	 */
	public void sendString(String string);
}
