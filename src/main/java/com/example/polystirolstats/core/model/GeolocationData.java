package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class GeolocationData {
	private String uuid;
	private String geolocation;
	
	@SerializedName("last_used")
	private Long lastUsed;

	public GeolocationData() {
	}

	public GeolocationData(String uuid, String geolocation, Long lastUsed) {
		this.uuid = uuid;
		this.geolocation = geolocation;
		this.lastUsed = lastUsed;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getGeolocation() {
		return geolocation;
	}

	public void setGeolocation(String geolocation) {
		this.geolocation = geolocation;
	}

	public Long getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(Long lastUsed) {
		this.lastUsed = lastUsed;
	}
}

