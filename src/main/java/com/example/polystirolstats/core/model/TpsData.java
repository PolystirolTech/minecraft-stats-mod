package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class TpsData {
	@SerializedName("server_uuid")
	private String serverUuid;
	
	private Long date;
	private Double tps;
	
	@SerializedName("players_online")
	private Integer playersOnline;
	
	@SerializedName("cpu_usage")
	private Double cpuUsage;
	
	@SerializedName("ram_usage")
	private Long ramUsage;
	
	private Integer entities;
	
	@SerializedName("chunks_loaded")
	private Integer chunksLoaded;
	
	@SerializedName("free_disk_space")
	private Long freeDiskSpace;

	public TpsData() {
	}

	public TpsData(String serverUuid, Long date, Double tps, Integer playersOnline, Double cpuUsage, Long ramUsage, Integer entities, Integer chunksLoaded, Long freeDiskSpace) {
		this.serverUuid = serverUuid;
		this.date = date;
		this.tps = tps;
		this.playersOnline = playersOnline;
		this.cpuUsage = cpuUsage;
		this.ramUsage = ramUsage;
		this.entities = entities;
		this.chunksLoaded = chunksLoaded;
		this.freeDiskSpace = freeDiskSpace;
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

	public Double getTps() {
		return tps;
	}

	public void setTps(Double tps) {
		this.tps = tps;
	}

	public Integer getPlayersOnline() {
		return playersOnline;
	}

	public void setPlayersOnline(Integer playersOnline) {
		this.playersOnline = playersOnline;
	}

	public Double getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(Double cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public Long getRamUsage() {
		return ramUsage;
	}

	public void setRamUsage(Long ramUsage) {
		this.ramUsage = ramUsage;
	}

	public Integer getEntities() {
		return entities;
	}

	public void setEntities(Integer entities) {
		this.entities = entities;
	}

	public Integer getChunksLoaded() {
		return chunksLoaded;
	}

	public void setChunksLoaded(Integer chunksLoaded) {
		this.chunksLoaded = chunksLoaded;
	}

	public Long getFreeDiskSpace() {
		return freeDiskSpace;
	}

	public void setFreeDiskSpace(Long freeDiskSpace) {
		this.freeDiskSpace = freeDiskSpace;
	}
}

