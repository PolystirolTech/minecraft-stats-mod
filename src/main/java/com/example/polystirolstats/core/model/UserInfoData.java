package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class UserInfoData {
	private String uuid;
	
	@SerializedName("server_uuid")
	private String serverUuid;
	
	@SerializedName("join_address")
	private String joinAddress;
	
	private Long registered;
	private Boolean opped;
	private Boolean banned;

	public UserInfoData() {
	}

	public UserInfoData(String uuid, String serverUuid, String joinAddress, Long registered, Boolean opped, Boolean banned) {
		this.uuid = uuid;
		this.serverUuid = serverUuid;
		this.joinAddress = joinAddress;
		this.registered = registered;
		this.opped = opped;
		this.banned = banned;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getServerUuid() {
		return serverUuid;
	}

	public void setServerUuid(String serverUuid) {
		this.serverUuid = serverUuid;
	}

	public String getJoinAddress() {
		return joinAddress;
	}

	public void setJoinAddress(String joinAddress) {
		this.joinAddress = joinAddress;
	}

	public Long getRegistered() {
		return registered;
	}

	public void setRegistered(Long registered) {
		this.registered = registered;
	}

	public Boolean getOpped() {
		return opped;
	}

	public void setOpped(Boolean opped) {
		this.opped = opped;
	}

	public Boolean getBanned() {
		return banned;
	}

	public void setBanned(Boolean banned) {
		this.banned = banned;
	}
}

