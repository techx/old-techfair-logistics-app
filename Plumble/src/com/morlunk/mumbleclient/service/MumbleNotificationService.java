/**
 * Copyright (C) 2012 Andrew Comminos
 * All rights reserved.
 */
package com.morlunk.mumbleclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Utility class to for notifications to perform actions with.
 * Used in Jellybean notifications to push to talk, etc.
 * @author andrew
 */
public class MumbleNotificationService extends Service {
	
	public static final String MUMBLE_NOTIFICATION_ACTION_KEY = "notificationKey";
	public static final String MUMBLE_NOTIFICATION_ACTION_TALK = "pushToTalk";
	public static final String MUMBLE_NOTIFICATION_ACTION_DEAFEN = "deafen";
	
	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		MumbleService service = MumbleService.getCurrentService();
		
		if(intent != null &&
				intent.getExtras() != null && 
				intent.getExtras().containsKey(MUMBLE_NOTIFICATION_ACTION_KEY)) {
			String keyString = intent.getExtras().getString(MUMBLE_NOTIFICATION_ACTION_KEY);
			
			if(keyString.equals(MUMBLE_NOTIFICATION_ACTION_TALK)) {
				service.setMuted(!service.isMuted());
			}
			
			if(keyString.equals(MUMBLE_NOTIFICATION_ACTION_DEAFEN)) {
					service.setDeafened(!service.isDeafened());
			}
		} else {
			Log.i("Plumble", "Notification service: action not specified!");
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
