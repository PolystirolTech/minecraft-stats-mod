package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class WorldTimeData {
	private String uuid;
	
	@SerializedName("world_id")
	private Integer worldId;
	
	@SerializedName("server_uuid")
	private String serverUuid;
	
	@SerializedName("session_id")
	private Long sessionId;
	
	@SerializedName("survival_time")
	private Long survivalTime;
	
	@SerializedName("creative_time")
	private Long creativeTime;
	
	@SerializedName("adventure_time")
	private Long adventureTime;
	
	@SerializedName("spectator_time")
	private Long spectatorTime;

	public WorldTimeData() {
	}

	public WorldTimeData(String uuid, Integer worldId, String serverUuid, Long sessionId, Long survivalTime, Long creativeTime, Long adventureTime, Long spectatorTime) {
		this.uuid = uuid;
		this.worldId = worldId;
		this.serverUuid = serverUuid;
		this.sessionId = sessionId;
		this.survivalTime = survivalTime;
		this.creativeTime = creativeTime;
		this.adventureTime = adventureTime;
		this.spectatorTime = spectatorTime;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Integer getWorldId() {
		return worldId;
	}

	public void setWorldId(Integer worldId) {
		this.worldId = worldId;
	}

	public String getServerUuid() {
		return serverUuid;
	}

	public void setServerUuid(String serverUuid) {
		this.serverUuid = serverUuid;
	}

	public Long getSessionId() {
		return sessionId;
	}

	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}

	public Long getSurvivalTime() {
		return survivalTime;
	}

	public void setSurvivalTime(Long survivalTime) {
		this.survivalTime = survivalTime;
	}

	public Long getCreativeTime() {
		return creativeTime;
	}

	public void setCreativeTime(Long creativeTime) {
		this.creativeTime = creativeTime;
	}

	public Long getAdventureTime() {
		return adventureTime;
	}

	public void setAdventureTime(Long adventureTime) {
		this.adventureTime = adventureTime;
	}

	public Long getSpectatorTime() {
		return spectatorTime;
	}

	public void setSpectatorTime(Long spectatorTime) {
		this.spectatorTime = spectatorTime;
	}
}

