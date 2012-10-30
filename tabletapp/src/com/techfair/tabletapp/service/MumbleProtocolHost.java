package com.techfair.tabletapp.service;

import com.techfair.tabletapp.service.model.Channel;
import com.techfair.tabletapp.service.model.Message;
import com.techfair.tabletapp.service.model.User;

/**
 * Callback interface for Protocol to communicate back to the Service
 *
 * @author Rantanen
 *
 */
public interface MumbleProtocolHost {
	public void channelAdded(Channel channel);

	public void channelRemoved(int channelId);

	public void channelUpdated(Channel channel);

	public void currentChannelChanged();

	public void currentUserUpdated();

	public void messageReceived(Message msg);

	public void messageSent(Message msg);

	public void setError(String error);

	public void setSynchronized(boolean synced);

	public void userAdded(User user);

	public void userRemoved(int userId, String reason);

	public void userUpdated(User user);
	
	public void permissionDenied(String reason, int denyType);
}
