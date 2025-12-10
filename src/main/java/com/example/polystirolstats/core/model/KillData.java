package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class KillData {
	@SerializedName("killer_uuid")
	private String killerUuid;
	
	@SerializedName("victim_uuid")
	private String victimUuid;
	
	@SerializedName("server_uuid")
	private String serverUuid;
	
	private String weapon;
	private Long date;
	
	@SerializedName("session_id")
	private Long sessionId;

	public KillData() {
	}

	public KillData(String killerUuid, String victimUuid, String serverUuid, String weapon, Long date, Long sessionId) {
		this.killerUuid = killerUuid;
		this.victimUuid = victimUuid;
		this.serverUuid = serverUuid;
		this.weapon = weapon;
		this.date = date;
		this.sessionId = sessionId;
	}

	public String getKillerUuid() {
		return killerUuid;
	}

	public void setKillerUuid(String killerUuid) {
		this.killerUuid = killerUuid;
	}

	public String getVictimUuid() {
		return victimUuid;
	}

	public void setVictimUuid(String victimUuid) {
		this.victimUuid = victimUuid;
	}

	public String getServerUuid() {
		return serverUuid;
	}

	public void setServerUuid(String serverUuid) {
		this.serverUuid = serverUuid;
	}

	public String getWeapon() {
		return weapon;
	}

	public void setWeapon(String weapon) {
		this.weapon = weapon;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public Long getSessionId() {
		return sessionId;
	}

	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}
}

