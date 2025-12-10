package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class NicknameData {
	private String uuid;
	private String nickname;
	
	@SerializedName("server_uuid")
	private String serverUuid;
	
	@SerializedName("last_used")
	private Long lastUsed;

	public NicknameData() {
	}

	public NicknameData(String uuid, String nickname, String serverUuid, Long lastUsed) {
		this.uuid = uuid;
		this.nickname = nickname;
		this.serverUuid = serverUuid;
		this.lastUsed = lastUsed;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getServerUuid() {
		return serverUuid;
	}

	public void setServerUuid(String serverUuid) {
		this.serverUuid = serverUuid;
	}

	public Long getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(Long lastUsed) {
		this.lastUsed = lastUsed;
	}
}

