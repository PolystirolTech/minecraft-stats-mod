package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class PingData {
	private String uuid;
	
	@SerializedName("server_uuid")
	private String serverUuid;
	
	private Long date;
	
	@SerializedName("max_ping")
	private Integer maxPing;
	
	@SerializedName("min_ping")
	private Integer minPing;
	
	@SerializedName("avg_ping")
	private Integer avgPing;

	public PingData() {
	}

	public PingData(String uuid, String serverUuid, Long date, Integer maxPing, Integer minPing, Integer avgPing) {
		this.uuid = uuid;
		this.serverUuid = serverUuid;
		this.date = date;
		this.maxPing = maxPing;
		this.minPing = minPing;
		this.avgPing = avgPing;
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

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public Integer getMaxPing() {
		return maxPing;
	}

	public void setMaxPing(Integer maxPing) {
		this.maxPing = maxPing;
	}

	public Integer getMinPing() {
		return minPing;
	}

	public void setMinPing(Integer minPing) {
		this.minPing = minPing;
	}

	public Integer getAvgPing() {
		return avgPing;
	}

	public void setAvgPing(Integer avgPing) {
		this.avgPing = avgPing;
	}
}

