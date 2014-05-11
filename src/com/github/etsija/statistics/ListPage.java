package com.github.etsija.statistics;

import java.util.ArrayList;
import java.util.List;

// A generic class for a list of Objects, along with the page
public class ListPage <T> {
	private List<T> list;
	private int page;
	
	ListPage() {
		
	}
	
	public ListPage(List<T> list, int page) {
		this.list = new ArrayList<T>(list);
		this.page = page;
	}
	
	public void setList(List<T> list) {
		this.list = list;
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	
	public List<T> getList() {
		return this.list;
	}
	
	public int getPage() {
		return this.page;
	}
}
