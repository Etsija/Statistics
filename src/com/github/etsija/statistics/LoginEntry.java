package com.github.etsija.statistics;

// A class which represents one login entry from "login" table
public class LoginEntry {
	String _playerName;
	String _timeLogin;
	int    _timeOnline;
	HelperMethods helper = new HelperMethods();
	
	LoginEntry(String name, String login, int online) {
		this._playerName = name;
		this._timeLogin = login;
		this._timeOnline = online;
	}
	
	public String getPlayerName() {
		return this._playerName;
	}
	
	public void setPlayerName(String name) {
		this._playerName = name;
	}
	
	public String getTimeLogin() {
		return this._timeLogin;
	}
	
	public void setTimeLogin(String time) {
		this._timeLogin = time;
	}
	
	public int getTimeOnline() {
		return this._timeOnline;
	}
	
	public String getTimeOnlineAsString() {
		String retString;
		if (getTimeOnline() > 0) {
			retString = String.format("[%s]", helper.timeFormatted(getTimeOnline()));
		} else {
			retString = "[ ONLINE ]";
		}
		return retString;
	}
}
