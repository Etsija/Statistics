package com.github.etsija.statistics;

public class PlayerData {
	private String _name;
	private int _totalLogins;
	private int _totalPlaytime;
	private int _avgPlaytime;
	private int _totalBlocksPlaced;
	private int _avgBlocksPlaced;
	private int _totalBlocksBroken;
	private int _avgBlocksBroken;
	
	PlayerData(String name) {
		this._name = name;
		this._totalLogins = 0;
		this._totalPlaytime = 0;
		this._avgPlaytime = 0;
		this._totalBlocksPlaced = 0;
		this._avgBlocksPlaced = 0;
		this._totalBlocksBroken = 0;
		this._avgBlocksBroken = 0;
	}
	
	PlayerData(String name, int totalLogins, int totalPlaytime, int avgPlaytime) {
		this._name = name;
		this._totalLogins = totalLogins;
		this._totalPlaytime = totalPlaytime;
		this._avgPlaytime = avgPlaytime;
		this._totalBlocksPlaced = 0;
		this._avgBlocksPlaced = 0;
		this._totalBlocksBroken = 0;
		this._avgBlocksBroken = 0;
	}
	
	PlayerData(String name, int totalLogins, int totalPlaytime, int avgPlaytime, int totalBlocksPlaced, int totalBlocksBroken) {
		this._name = name;
		this._totalLogins = totalLogins;
		this._totalPlaytime = totalPlaytime;
		this._avgPlaytime = avgPlaytime;
		this._totalBlocksPlaced = totalBlocksPlaced;
		this._totalBlocksBroken = totalBlocksBroken;
	}
	
	PlayerData(String name, int totalLogins, int totalPlaytime, int avgPlaytime, 
			   int totalBlocksPlaced, int avgBlocksPlaced, int totalBlocksBroken, int avgBlocksBroken) {
		this._name = name;
		this._totalLogins = totalLogins;
		this._totalPlaytime = totalPlaytime;
		this._avgPlaytime = avgPlaytime;
		this._totalBlocksPlaced = totalBlocksPlaced;
		this._avgBlocksPlaced = avgBlocksPlaced;
		this._totalBlocksBroken = totalBlocksBroken;
		this._avgBlocksBroken = avgBlocksBroken;
	}
	
	public String getPlayerName() {
		return this._name;
	}
	
	public void setPlayerName(String name) {
		this._name = name;
	}
	
	public int getTotalLogins() {
		return this._totalLogins;
	}
	
	public void setTotalLogins(int totalLogins) {
		this._totalLogins = totalLogins;
	}
	
	public int getTotalPlaytime() {
		return this._totalPlaytime;
	}
	
	public void setTotalPlaytime(int totalPlaytime) {
		this._totalPlaytime = totalPlaytime;
	}
	
	public int getAvgPlaytime() {
		return this._avgPlaytime;
	}
	
	public void setAvgPlaytime(int avgPlaytime) {
		this._avgPlaytime = avgPlaytime;
	}
	
	public int getTotalBlocksPlaced() {
		return this._totalBlocksPlaced;
	}
	
	public void setTotalBlocksPlaced(int totalBlocksPlaced) {
		this._totalBlocksPlaced = totalBlocksPlaced;
	}
	
	public int getAvgBlocksPlaced() {
		return this._avgBlocksPlaced;
	}
	
	public void setAvgBlocksPlaced(int avgBlocksPlaced) {
		this._avgBlocksPlaced = avgBlocksPlaced;
	}

	public int getTotalBlocksBroken() {
		return this._totalBlocksBroken;
	}
	
	public void setTotalBlocksBroken(int totalBlocksBroken) {
		this._totalBlocksBroken = totalBlocksBroken;
	}

	public int getAvgBlocksBroken() {
		return this._avgBlocksBroken;
	}
	
	public void setAvgBlocksBroken(int avgBlocksBroken) {
		this._avgBlocksBroken = avgBlocksBroken;
	}

}
