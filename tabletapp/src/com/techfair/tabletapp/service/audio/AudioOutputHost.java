package com.techfair.tabletapp.service.audio;

import com.techfair.tabletapp.service.model.User;

public interface AudioOutputHost {
	public static final int STATE_PASSIVE = 0;
	public static final int STATE_TALKING = 1;

	public void setTalkState(User user, int talkState);
	public void setMuted(User user, boolean muted);
	public void setDeafened(User user, boolean deafened);
}
