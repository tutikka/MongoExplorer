package com.tt.mongoexplorer.domain;

public class Host {

	private String address;
	
	private int port = -1;
	
	private String username;
	
	private String password;
	
	private String authenticationDatabase;
	
	public Host(String address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(address);
		sb.append(":");
		sb.append(port);
		return (sb.toString());
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthenticationDatabase() {
		return authenticationDatabase;
	}

	public void setAuthenticationDatabase(String authenticationDatabase) {
		this.authenticationDatabase = authenticationDatabase;
	}
	
}
