package com.morlunk.mumbleclient.service;

import com.morlunk.mumbleclient.service.model.User;
import com.morlunk.mumbleclient.service.model.Message;
import com.morlunk.mumbleclient.service.model.Channel;

interface IServiceObserver {
	void onChannelAdded(in Channel channel);
	void onChannelRemoved(in Channel channel);
	void onChannelUpdated(in Channel channel);

	void onCurrentChannelChanged();
	
	void onCurrentUserUpdated();
	
	void onUserAdded(in User user);
	void onUserRemoved(in User user, in String reason);
	void onUserUpdated(in User user);
	
	void onMessageReceived(in Message msg);
	void onMessageSent(in Message msg);
	
	/**
	 * Called when the connection state changes.
	 */
	void onConnectionStateChanged(int state);
	
	void onPermissionDenied(in String reason, in int denyType);
}
