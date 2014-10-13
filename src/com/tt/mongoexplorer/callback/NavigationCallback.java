package com.tt.mongoexplorer.callback;

import com.tt.mongoexplorer.domain.Collection;

public interface NavigationCallback {

	public void onOpenQueryWindowRequested(Collection collection);
	
	public void onFindAllDocumentsRequested(Collection collection);
	
}
