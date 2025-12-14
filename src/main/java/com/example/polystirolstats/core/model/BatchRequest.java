package com.example.polystirolstats.core.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BatchRequest {
	@SerializedName("server_uuid")
	private String serverUuid;
	
	private List<ServerData> servers;
	private List<UserData> users;
	
	@SerializedName("user_info")
	private List<UserInfoData> userInfo;
	
	private List<SessionData> sessions;
	private List<NicknameData> nicknames;
	private List<KillData> kills;
	private List<PingData> pings;
	private List<PlatformData> platforms;
	
	@SerializedName("plugin_versions")
	private List<PluginVersionData> pluginVersions;
	
	private List<TpsData> tps;
	private List<WorldData> worlds;
	
	@SerializedName("world_times")
	private List<WorldTimeData> worldTimes;
	
	@SerializedName("version_protocols")
	private List<VersionProtocolData> versionProtocols;
	
	private List<GeolocationData> geolocations;
	
	private List<CounterData> counters;

	public BatchRequest() {
	}

	public BatchRequest(String serverUuid) {
		this.serverUuid = serverUuid;
	}

	public String getServerUuid() {
		return serverUuid;
	}

	public void setServerUuid(String serverUuid) {
		this.serverUuid = serverUuid;
	}

	public List<ServerData> getServers() {
		return servers;
	}

	public void setServers(List<ServerData> servers) {
		this.servers = servers;
	}

	public List<UserData> getUsers() {
		return users;
	}

	public void setUsers(List<UserData> users) {
		this.users = users;
	}

	public List<UserInfoData> getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(List<UserInfoData> userInfo) {
		this.userInfo = userInfo;
	}

	public List<SessionData> getSessions() {
		return sessions;
	}

	public void setSessions(List<SessionData> sessions) {
		this.sessions = sessions;
	}

	public List<NicknameData> getNicknames() {
		return nicknames;
	}

	public void setNicknames(List<NicknameData> nicknames) {
		this.nicknames = nicknames;
	}

	public List<KillData> getKills() {
		return kills;
	}

	public void setKills(List<KillData> kills) {
		this.kills = kills;
	}

	public List<PingData> getPings() {
		return pings;
	}

	public void setPings(List<PingData> pings) {
		this.pings = pings;
	}

	public List<PlatformData> getPlatforms() {
		return platforms;
	}

	public void setPlatforms(List<PlatformData> platforms) {
		this.platforms = platforms;
	}

	public List<PluginVersionData> getPluginVersions() {
		return pluginVersions;
	}

	public void setPluginVersions(List<PluginVersionData> pluginVersions) {
		this.pluginVersions = pluginVersions;
	}

	public List<TpsData> getTps() {
		return tps;
	}

	public void setTps(List<TpsData> tps) {
		this.tps = tps;
	}

	public List<WorldData> getWorlds() {
		return worlds;
	}

	public void setWorlds(List<WorldData> worlds) {
		this.worlds = worlds;
	}

	public List<WorldTimeData> getWorldTimes() {
		return worldTimes;
	}

	public void setWorldTimes(List<WorldTimeData> worldTimes) {
		this.worldTimes = worldTimes;
	}

	public List<VersionProtocolData> getVersionProtocols() {
		return versionProtocols;
	}

	public void setVersionProtocols(List<VersionProtocolData> versionProtocols) {
		this.versionProtocols = versionProtocols;
	}

	public List<GeolocationData> getGeolocations() {
		return geolocations;
	}

	public void setGeolocations(List<GeolocationData> geolocations) {
		this.geolocations = geolocations;
	}

	public List<CounterData> getCounters() {
		return counters;
	}

	public void setCounters(List<CounterData> counters) {
		this.counters = counters;
	}
}

