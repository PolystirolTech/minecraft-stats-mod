package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class UserData {
	private String uuid;
	private Long registered;
	private String name;
	
	@SerializedName("times_kicked")
	private Integer timesKicked;

	public UserData() {
	}

	public UserData(String uuid, Long registered, String name, Integer timesKicked) {
		this.uuid = uuid;
		this.registered = registered;
		this.name = name;
		this.timesKicked = timesKicked;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getRegistered() {
		return registered;
	}

	public void setRegistered(Long registered) {
		this.registered = registered;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getTimesKicked() {
		return timesKicked;
	}

	public void setTimesKicked(Integer timesKicked) {
		this.timesKicked = timesKicked;
	}
}

