package com.tt.mongoexplorer.domain;

public class Database {

	private String name;

	private Host host;
	
	public Database(String name, Host host) {
		this.name = name;
		this.host = host;
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
	
}
