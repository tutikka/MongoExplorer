package com.tt.mongoexplorer.callback;

import com.tt.mongoexplorer.domain.Collection;
import com.tt.mongoexplorer.domain.Database;
import com.tt.mongoexplorer.domain.Host;

public interface NavigationCallback {

	public void onHostSelected(Host host);
	
	public void onDatabaseSelected(Database database);
	
	public void onCollectionSelected(Collection collection);
	
}
