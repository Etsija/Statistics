package com.github.etsija.statistics;

public class PlayerData {
	String name;
	int totalLogins;
	int totalPlaytime;
	int avgPlaytime;
	
	PlayerData(String name) {
		this.name = name;
		this.totalLogins = 0;
		this.totalPlaytime = 0;
		this.avgPlaytime = 0;
	}
	
	PlayerData(String name, int totalLogins, int totalPlaytime, int avgPlaytime) {
		this.name = name;
		this.totalLogins = totalLogins;
		this.totalPlaytime = totalPlaytime;
		this.avgPlaytime = avgPlaytime;
	}
	
	public String getPlayerName() {
		return this.name;
	}
	
	public void setPlayerName(String name) {
		this.name = name;
	}
	
	public int getTotalLogins() {
		return this.totalLogins;
	}
	
	public void setTotalLogins(int totalLogins) {
		this.totalLogins = totalLogins;
	}
	
	public int getTotalPlaytime() {
		return this.totalPlaytime;
	}
	
	public void setTotalPlaytime(int totalPlaytime) {
		this.totalPlaytime = totalPlaytime;
	}
	
	public int getAvgPlaytime() {
		return this.avgPlaytime;
	}
	
	public void setAvgPlaytime(int avgPlaytime) {
		this.avgPlaytime = avgPlaytime;
	}
}
