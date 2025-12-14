package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class CounterData {
	private String uuid;
	
	@SerializedName("server_uuid")
	private String serverUuid;
	
	private Map<String, Integer> counters;

	public CounterData() {
	}

	public CounterData(String uuid, String serverUuid, Map<String, Integer> counters) {
		this.uuid = uuid;
		this.serverUuid = serverUuid;
		this.counters = counters;
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

	public Map<String, Integer> getCounters() {
		return counters;
	}

	public void setCounters(Map<String, Integer> counters) {
		this.counters = counters;
	}
}
