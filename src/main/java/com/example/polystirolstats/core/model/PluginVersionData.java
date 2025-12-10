package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;

public class PluginVersionData {
	@SerializedName("server_uuid")
	private String serverUuid;
	
	@SerializedName("plugin_name")
	private String pluginName;
	
	private String version;
	private Long modified;

	public PluginVersionData() {
	}

	public PluginVersionData(String serverUuid, String pluginName, String version, Long modified) {
		this.serverUuid = serverUuid;
		this.pluginName = pluginName;
		this.version = version;
		this.modified = modified;
	}

	public String getServerUuid() {
		return serverUuid;
	}

	public void setServerUuid(String serverUuid) {
		this.serverUuid = serverUuid;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getModified() {
		return modified;
	}

	public void setModified(Long modified) {
		this.modified = modified;
	}
}

