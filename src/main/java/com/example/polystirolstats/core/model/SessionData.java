package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class SessionData {
	private String uuid;
	
	@SerializedName("server_uuid")
	private String serverUuid;
	
	@SerializedName("session_start")
	private Long sessionStart;
	
	@SerializedName("session_end")
	private Long sessionEnd;
	
	@SerializedName("mob_kills")
	private Integer mobKills;
	
	private Integer deaths;
	
	@SerializedName("afk_time")
	private Long afkTime;
	
	@SerializedName("join_address")
	private String joinAddress;

	public SessionData() {
	}

	public SessionData(String uuid, String serverUuid, Long sessionStart, Long sessionEnd, Integer mobKills, Integer deaths, Long afkTime, String joinAddress) {
		this.uuid = uuid;
		this.serverUuid = serverUuid;
		this.sessionStart = sessionStart;
		this.sessionEnd = sessionEnd;
		this.mobKills = mobKills;
		this.deaths = deaths;
		this.afkTime = afkTime;
		this.joinAddress = joinAddress;
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

	public Long getSessionStart() {
		return sessionStart;
	}

	public void setSessionStart(Long sessionStart) {
		this.sessionStart = sessionStart;
	}

	public Long getSessionEnd() {
		return sessionEnd;
	}

	public void setSessionEnd(Long sessionEnd) {
		this.sessionEnd = sessionEnd;
	}

	public Integer getMobKills() {
		return mobKills;
	}

	public void setMobKills(Integer mobKills) {
		this.mobKills = mobKills;
	}

	public Integer getDeaths() {
		return deaths;
	}

	public void setDeaths(Integer deaths) {
		this.deaths = deaths;
	}

	public Long getAfkTime() {
		return afkTime;
	}

	public void setAfkTime(Long afkTime) {
		this.afkTime = afkTime;
	}

	public String getJoinAddress() {
		return joinAddress;
	}

	public void setJoinAddress(String joinAddress) {
		this.joinAddress = joinAddress;
	}
}

