package com.tt.mongoexplorer.domain;

import java.util.ArrayList;
import java.util.List;

public class Database {

	private String name;

	private Host host;
	
	private List<Collection> collections = new ArrayList<>();
	
	public Database(String name, Host host) {
		this.name = name;
		this.host = host;
		this.host.getDatabases().add(this);
	}

	@Override
	public String toString() {
		return (name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public List<Collection> getCollections() {
		return collections;
	}

	public void setCollections(List<Collection> collections) {
		this.collections = collections;
	}
	
}
