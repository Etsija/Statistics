package com.github.etsija.statistics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

public class HelperMethods {

	private Logger _log = Logger.getLogger("Minecraft"); // Write debug info to console
	
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
	
	// Format a Unix time (in milliseconds) to a 00-00-0000 format
	public String unixTimeToString(long unixTime) {
		Date date = new Date(unixTime);
		TimeZone tz = TimeZone.getDefault();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(tz);
	
		return sdf.format(date);
	}
	
	// Format a Unix time (in milliseconds) to a 00-00-0000 00:00:00 format
	public String unixTimeToStringAccurate(long unixTime) {
		Date date = new Date(unixTime);
		TimeZone tz = TimeZone.getDefault();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(tz);
		
		return sdf.format(date);
	}
	
	// Test whether a String is not empty or null
	public boolean isNotEmpty(String s) {
		return (s != null && !s.equals(""));
	}
	
	// Test whether a String is empty or null
	public boolean isEmpty(String s) {
		return (s == null || s.equals(""));
	}
	
	// Calculate # pages needed for the output
	public int nPages(int nTotal, int nPerPage) {
		return ((int)(nTotal / nPerPage) + 1);
	}

	// Parse a string format such as "12d5h3m" into a time in the past
	// To be used with the "/stats since" command
	public String parseSince(String str) {
		long seconds     = 0;
		String strRemainder = str;
		
		// Add days in seconds
		if (strRemainder.contains("d")) {
			String[] strSplit = str.split("d");
			seconds += Integer.parseInt(strSplit[0]) * 24 * 60 * 60;
			strRemainder = (strSplit.length == 2) ? strSplit[1] : "";
		}
		// Add hours in seconds
		if (strRemainder.contains("h")) {
			String[] strSplit = strRemainder.split("h");
			seconds += Integer.parseInt(strSplit[0]) * 60 * 60;
			strRemainder = (strSplit.length == 2) ? strSplit[1] : "";
		}
		// Add minutes in seconds
		if (strRemainder.contains("m")) {
			String[] strSplit = strRemainder.split("m");
			seconds += Integer.parseInt(strSplit[0]) * 60;
			strRemainder = (strSplit.length == 2) ? strSplit[1] : "";
		}
		// Add seconds
		if (strRemainder.contains("s")) {
			String[] strSplit = strRemainder.split("s");
			seconds += Integer.parseInt(strSplit[0]);
		}
		
		long now = System.currentTimeMillis();
		return unixTimeToStringAccurate(now - seconds * 1000);
	}
	
	// Return a sub-list (page "page") of a list of Objects
	public <T> ListPage<T> paginate(List<T> inputList, int page, int itemsPerPage) {
		List<T> paginatedList = new ArrayList<T>();
		int nItems = inputList.size();
		int nPages = nPages(nItems, itemsPerPage);
		
		if (page < 1) {
			page = 1;
		} else if (page > nPages) {
			page = nPages;
		}
		
		int start = (page - 1) * itemsPerPage;
		int end   =  page      * itemsPerPage;
		if (end > nItems) {
			end = nItems;
		}
		
		paginatedList = inputList.subList(start, end);
		ListPage<T> retList = new ListPage<T>(paginatedList, page);
		return retList;
	}
}