package com.tt.mongoexplorer.callback;

import com.tt.mongoexplorer.domain.Collection;
import com.tt.mongoexplorer.domain.Host;

public interface NavigationCallback {

	public void onOpenQueryWindowRequested(Collection collection);
	
	public void onFindAllDocumentsRequested(Collection collection);

    public void onDisconnectFromHostRequested(Host host);

}
