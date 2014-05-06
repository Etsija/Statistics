package com.github.etsija.statistics;

import java.util.ArrayList;
import java.util.List;

public class ListPage {
	private List<String> list;
	private int page;
	
	public ListPage() {
		
	}
	
	public ListPage(List<String> list, int page) {
		this.list = new ArrayList<String>(list);
		this.page = page;
	}
	
	public void setList(List<String> list) {
		this.list = list;
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	
	public List<String> getList() {
		return this.list;
	}
	
	public int getPage() {
		return this.page;
	}
}
