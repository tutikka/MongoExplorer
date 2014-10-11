package com.tt.mongoexplorer.utils;

import java.util.Arrays;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.tt.mongoexplorer.domain.Host;

public class MongoUtils {

	public static MongoClient getMongoClient(Host host) throws Exception {
		String address = host.getAddress();
		if (address == null || address.length() == 0) {
			address = "127.0.0.1";
		}
		int port = host.getPort();
		if (port == -1) {
			port = 27017;
		}
		MongoCredential credential = getCredential(host);
		if (credential == null) {
			return (new MongoClient(address, port));
		} else {
			return (new MongoClient(new ServerAddress(address, port), Arrays.asList(credential)));
		}
	}
	
	private static MongoCredential getCredential(Host host) {
		if (host.getUsername() == null || host.getUsername().length() == 0) {
			return (null);
		}
		if (host.getPassword() == null || host.getPassword().length() == 0) {
			return (null);
		}
		MongoCredential credential = MongoCredential.createMongoCRCredential(host.getUsername(), host.getAuthenticationDatabase(), host.getPassword().toCharArray());
		return (credential);
	}
	
}
