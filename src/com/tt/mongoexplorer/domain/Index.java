package com.tt.mongoexplorer.domain;

import com.mongodb.DBObject;

public class Index {

	private DBObject object;
	
	private Collection collection;
	
	public Index(DBObject object, Collection collection) {
		this.object = object;
		this.collection = collection;
		this.collection.getIndexes().add(this);
	}

	public DBObject getObject() {
		return object;
	}

	public void setObject(DBObject object) {
		this.object = object;
	}

	public Collection getCollection() {
		return collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}
	
}
