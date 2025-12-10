package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class PlatformData {
	private String uuid;
	private Integer platform;
	
	@SerializedName("bedrock_username")
	private String bedrockUsername;
	
	@SerializedName("java_username")
	private String javaUsername;
	
	@SerializedName("linked_player")
	private String linkedPlayer;
	
	@SerializedName("language_code")
	private String languageCode;
	
	private String version;

	public PlatformData() {
	}

	public PlatformData(String uuid, Integer platform, String bedrockUsername, String javaUsername, String linkedPlayer, String languageCode, String version) {
		this.uuid = uuid;
		this.platform = platform;
		this.bedrockUsername = bedrockUsername;
		this.javaUsername = javaUsername;
		this.linkedPlayer = linkedPlayer;
		this.languageCode = languageCode;
		this.version = version;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Integer getPlatform() {
		return platform;
	}

	public void setPlatform(Integer platform) {
		this.platform = platform;
	}

	public String getBedrockUsername() {
		return bedrockUsername;
	}

	public void setBedrockUsername(String bedrockUsername) {
		this.bedrockUsername = bedrockUsername;
	}

	public String getJavaUsername() {
		return javaUsername;
	}

	public void setJavaUsername(String javaUsername) {
		this.javaUsername = javaUsername;
	}

	public String getLinkedPlayer() {
		return linkedPlayer;
	}

	public void setLinkedPlayer(String linkedPlayer) {
		this.linkedPlayer = linkedPlayer;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}

