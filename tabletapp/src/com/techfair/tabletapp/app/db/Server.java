package com.techfair.tabletapp.app.db;

public class Server {
	
	private Integer id;
	private String name;
	private String host;
	private Integer port;
	private String username;
	private String password;
	
	protected Server(Integer id,
			String name,
			String host,
			Integer port,
			String username,
			String password) {
		this.id = id;
		this.name = name;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}	
}