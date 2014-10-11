package com.tt.mongoexplorer.domain;

import java.util.List;

public class Connections {

	private List<Host> hosts;
	
	public Connections() {
	}

	public List<Host> getHosts() {
		return hosts;
	}

	public void setHosts(List<Host> hosts) {
		this.hosts = hosts;
	}

}
