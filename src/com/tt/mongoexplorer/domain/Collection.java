package com.tt.mongoexplorer.domain;

public class Collection {

	private String name;
	
	private Database database;
	
	public Collection(String name, Database database) {
		this.name = name;
		this.database = database;
		this.database.getCollections().add(this);
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

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}
	
}
