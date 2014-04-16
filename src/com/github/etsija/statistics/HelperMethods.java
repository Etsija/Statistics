package com.github.etsija.statistics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class HelperMethods {

	//private Logger _log = Logger.getLogger("Minecraft"); // Write debug info to console
	
	// Format seconds into a "00:00:00" string
	// If hours exceeds 24, the format will be "5d 00:00:00" 
	public String timeFormatted(int seconds) {
	    int hours = seconds / 3600;
	    int minutes = (seconds % 3600) / 60;
	    seconds = seconds % 60;
	    // Handle hrs > 24 -> days conversion
	    int days = hours / 24;
	    hours = hours % 24;
	    if (days == 0) {
	    	return twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds);
	    } else {
	    	return days + "d " + twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds);
	    }
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
	
	// Format a Unix time (in milliseconds) to a 00-00-0000 00:00:00 format
	public String unixTimeToString(long unixTime) {
		Date date = new Date(unixTime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
	
		return sdf.format(date);
	}
}