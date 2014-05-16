package com.github.etsija.statistics;

// A class which represents one login entry from "login" table
public class LoginEntry {
	private String _playerName;
	private String _timeLogin;
	private int _timeOnline;
	private String _world;
	private int _x;
	private int _y;
	private int _z;
	private int _blocksPlaced;
	private int _blocksBroken;
	
	HelperMethods helper = new HelperMethods();
	
	LoginEntry(String name, String login, int online) {
		this._playerName = name;
		this._timeLogin = login;
		this._timeOnline = online;
	}
	
	LoginEntry(String name, String login, int online, int blocksPlaced, int blocksBroken) {
		this._playerName = name;
		this._timeLogin = login;
		this._timeOnline = online;
		this._blocksPlaced = blocksPlaced;
		this._blocksBroken = blocksBroken;
	}
	
	LoginEntry(String name, String login, int online, String world, int x, int y, int z, int blocksPlaced, int blocksBroken) {
		this._playerName = name;
		this._timeLogin = login;
		this._timeOnline = online;
		this._world = world;
		this._x = x;
		this._y = y;
		this._z = z;
		this._blocksPlaced = blocksPlaced;
		this._blocksBroken = blocksBroken;
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
	
	public void setWorld(String world) {
		this._world = world;
	}
	
	public String getWorld() {
		return this._world;
	}
	
	public void setX(int x) {
		this._x = x;
	}
	
	public int getX() {
		return this._x;
	}
	
	public void setY(int y) {
		this._y = y;
	}
	
	public int getY() {
		return this._y;
	}
	
	public void setZ(int z) {
		this._z = z;
	}
	
	public int getZ() {
		return this._z;
	}
	
	public void setBlocksPlaced(int blocksPlaced) {
		this._blocksPlaced = blocksPlaced;
	}
	
	public int getBlocksPlaced() {
		return this._blocksPlaced;
	}
	
	public void setBlocksBroken(int blocksBroken) {
		this._blocksBroken = blocksBroken;
	}
	
	public int getBlocksBroken() {
		return this._blocksBroken;
	}
}
