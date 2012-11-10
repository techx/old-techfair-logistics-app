package com.techfair.tabletapp.app.db;

public class Favourite {

	private Integer id;
	private Integer serverId;
	private Integer channelId;
	
	protected Favourite(Integer id, Integer serverId, Integer channelId) {
		this.id = id;
		this.serverId = serverId;
		this.channelId = channelId;
	}

	public Integer getId() {
		return id;
	}

	public Integer getServerId() {
		return serverId;
	}

	public Integer getChannelId() {
		return channelId;
	}
}
