package com.tt.mongoexplorer.callback;

import com.tt.mongoexplorer.domain.Host;

public interface ConnectCallback {

	public void onRequestConnect(Host host);
	
}
