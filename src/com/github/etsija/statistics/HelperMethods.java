package com.github.etsija.statistics;

import java.util.logging.Logger;

public class HelperMethods {

	private Logger _log = Logger.getLogger("Minecraft"); // Write debug info to console
	
	// Format seconds into 00:00:00 string
	public String timeFormatted(int seconds) {
	    int hours = seconds / 3600;
	    int minutes = (seconds % 3600) / 60;
	    seconds = seconds % 60;
	    return twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds);
	}
	
	// Format a number into two-digit string
	private String twoDigitString(int number) {
	    if (number == 0) {
	        return "00";
	    }
	    if (number / 10 == 0) {
	        return "0" + number;
	    }
	    return String.valueOf(number);
	}
	
	
	
}
