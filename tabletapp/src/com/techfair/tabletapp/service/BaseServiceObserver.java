package com.techfair.tabletapp.service;

import android.os.IBinder;
import android.os.RemoteException;

import com.techfair.tabletapp.service.IServiceObserver;
import com.techfair.tabletapp.service.model.Channel;
import com.techfair.tabletapp.service.model.Message;
import com.techfair.tabletapp.service.model.User;

public class BaseServiceObserver implements IServiceObserver {
	@Override
	public IBinder asBinder() {
		return null;
	}

	@Override
	public void onChannelAdded(final Channel channel) throws RemoteException {
	}

	@Override
	public void onChannelRemoved(final Channel channel) throws RemoteException {
	}

	@Override
	public void onChannelUpdated(final Channel channel) throws RemoteException {
	}

	@Override
	public void onConnectionStateChanged(final int state)
		throws RemoteException {
	}

	@Override
	public void onCurrentChannelChanged() throws RemoteException {
	}

	@Override
	public void onCurrentUserUpdated() throws RemoteException {
	}

	@Override
	public void onMessageReceived(final Message msg) throws RemoteException {
	}

	@Override
	public void onMessageSent(final Message msg) throws RemoteException {
	}

	@Override
	public void onUserAdded(final User user) throws RemoteException {
	}

	@Override
	public void onUserRemoved(final User user, final String reason) throws RemoteException {
	}

	@Override
	public void onUserUpdated(final User user) throws RemoteException {
	}
	
	public void onPermissionDenied(String reason, int denyType) throws RemoteException {
	}
}
