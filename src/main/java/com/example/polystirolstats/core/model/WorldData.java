package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class WorldData {
	@SerializedName("server_uuid")
	private String serverUuid;
	
	@SerializedName("world_name")
	private String worldName;

	public WorldData() {
	}

	public WorldData(String serverUuid, String worldName) {
		this.serverUuid = serverUuid;
		this.worldName = worldName;
	}

	public String getServerUuid() {
		return serverUuid;
	}

	public void setServerUuid(String serverUuid) {
		this.serverUuid = serverUuid;
	}

	public String getWorldName() {
		return worldName;
	}

	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}
}

