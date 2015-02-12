package com.tt.mongoexplorer.domain;

import java.util.ArrayList;
import java.util.List;

public class Host {

	private String description;
	
	private String address;
	
	private int port = -1;
	
	private String username;
	
	private String password;
	
	private String authenticationDatabase;
	
	private List<Database> databases = new ArrayList<Database>();
	
	public Host(String description, String address, int port) {
		this.description = description;
		this.address = address;
		this.port = port;
	}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Host) {
            Host other = (Host) obj;
            return (address.equals(other.address) && port == other.port);
        } else {
            return (false);
        }
    }

    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(description);
		sb.append(" [ ");
		sb.append(address);
		sb.append(":");
		sb.append(port);
		sb.append(" ]");
		return (sb.toString());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public List<Database> getDatabases() {
		return databases;
	}

	public void setDatabases(List<Database> databases) {
		this.databases = databases;
	}
	
}
