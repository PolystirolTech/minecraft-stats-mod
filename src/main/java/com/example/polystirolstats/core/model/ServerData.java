package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class ServerData {
	@SerializedName("server_uuid")
	private String serverUuid;
	
	private String name;
	
	@SerializedName("web_address")
	private String webAddress;
	
	@SerializedName("is_installed")
	private Boolean isInstalled;
	
	@SerializedName("is_proxy")
	private Boolean isProxy;
	
	@SerializedName("max_players")
	private Integer maxPlayers;
	
	@SerializedName("plan_version")
	private String planVersion;

	public ServerData() {
	}

	public ServerData(String serverUuid, String name, String webAddress, Boolean isInstalled, Boolean isProxy, Integer maxPlayers, String planVersion) {
		this.serverUuid = serverUuid;
		this.name = name;
		this.webAddress = webAddress;
		this.isInstalled = isInstalled;
		this.isProxy = isProxy;
		this.maxPlayers = maxPlayers;
		this.planVersion = planVersion;
	}

	public String getServerUuid() {
		return serverUuid;
	}

	public void setServerUuid(String serverUuid) {
		this.serverUuid = serverUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWebAddress() {
		return webAddress;
	}

	public void setWebAddress(String webAddress) {
		this.webAddress = webAddress;
	}

	public Boolean getIsInstalled() {
		return isInstalled;
	}

	public void setIsInstalled(Boolean isInstalled) {
		this.isInstalled = isInstalled;
	}

	public Boolean getIsProxy() {
		return isProxy;
	}

	public void setIsProxy(Boolean isProxy) {
		this.isProxy = isProxy;
	}

	public Integer getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(Integer maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public String getPlanVersion() {
		return planVersion;
	}

	public void setPlanVersion(String planVersion) {
		this.planVersion = planVersion;
	}
}

