package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class VersionProtocolData {
	private String uuid;
	
	@SerializedName("protocol_version")
	private Integer protocolVersion;

	public VersionProtocolData() {
	}

	public VersionProtocolData(String uuid, Integer protocolVersion) {
		this.uuid = uuid;
		this.protocolVersion = protocolVersion;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Integer getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(Integer protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
}

