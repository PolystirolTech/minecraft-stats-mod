package com.example.polystirolstats.core.collector;

import com.example.polystirolstats.core.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StatisticsCollector {
	private final List<ServerData> servers = new CopyOnWriteArrayList<>();
	private final List<UserData> users = new CopyOnWriteArrayList<>();
	private final List<UserInfoData> userInfo = new CopyOnWriteArrayList<>();
	private final List<SessionData> sessions = new CopyOnWriteArrayList<>();
	private final List<NicknameData> nicknames = new CopyOnWriteArrayList<>();
	private final List<KillData> kills = new CopyOnWriteArrayList<>();
	private final List<PingData> pings = new CopyOnWriteArrayList<>();
	private final List<PlatformData> platforms = new CopyOnWriteArrayList<>();
	private final List<PluginVersionData> pluginVersions = new CopyOnWriteArrayList<>();
	private final List<TpsData> tps = new CopyOnWriteArrayList<>();
	private final List<WorldData> worlds = new CopyOnWriteArrayList<>();
	private final List<WorldTimeData> worldTimes = new CopyOnWriteArrayList<>();
	private final List<VersionProtocolData> versionProtocols = new CopyOnWriteArrayList<>();
	private final List<GeolocationData> geolocations = new CopyOnWriteArrayList<>();
	
	public void addServer(ServerData server) {
		servers.add(server);
	}
	
	public void addUser(UserData user) {
		users.add(user);
	}
	
	public void addUserInfo(UserInfoData info) {
		userInfo.add(info);
	}
	
	public void addSession(SessionData session) {
		sessions.add(session);
	}
	
	public void addNickname(NicknameData nickname) {
		nicknames.add(nickname);
	}
	
	public void addKill(KillData kill) {
		kills.add(kill);
	}
	
	public void addPing(PingData ping) {
		pings.add(ping);
	}
	
	public void addPlatform(PlatformData platform) {
		platforms.add(platform);
	}
	
	public void addPluginVersion(PluginVersionData pluginVersion) {
		pluginVersions.add(pluginVersion);
	}
	
	public void addTps(TpsData tpsData) {
		tps.add(tpsData);
	}
	
	public void addWorld(WorldData world) {
		worlds.add(world);
	}
	
	public void addWorldTime(WorldTimeData worldTime) {
		worldTimes.add(worldTime);
	}
	
	public void addVersionProtocol(VersionProtocolData versionProtocol) {
		versionProtocols.add(versionProtocol);
	}
	
	public void addGeolocation(GeolocationData geolocation) {
		geolocations.add(geolocation);
	}
	
	public BatchRequest getBatchData(String serverUuid) {
		BatchRequest batch = new BatchRequest(serverUuid);
		
		if (!servers.isEmpty()) {
			batch.setServers(new ArrayList<>(servers));
		}
		if (!users.isEmpty()) {
			batch.setUsers(new ArrayList<>(users));
		}
		if (!userInfo.isEmpty()) {
			batch.setUserInfo(new ArrayList<>(userInfo));
		}
		if (!sessions.isEmpty()) {
			batch.setSessions(new ArrayList<>(sessions));
		}
		if (!nicknames.isEmpty()) {
			batch.setNicknames(new ArrayList<>(nicknames));
		}
		if (!kills.isEmpty()) {
			batch.setKills(new ArrayList<>(kills));
		}
		if (!pings.isEmpty()) {
			batch.setPings(new ArrayList<>(pings));
		}
		if (!platforms.isEmpty()) {
			batch.setPlatforms(new ArrayList<>(platforms));
		}
		if (!pluginVersions.isEmpty()) {
			batch.setPluginVersions(new ArrayList<>(pluginVersions));
		}
		if (!tps.isEmpty()) {
			batch.setTps(new ArrayList<>(tps));
		}
		if (!worlds.isEmpty()) {
			batch.setWorlds(new ArrayList<>(worlds));
		}
		if (!worldTimes.isEmpty()) {
			batch.setWorldTimes(new ArrayList<>(worldTimes));
		}
		if (!versionProtocols.isEmpty()) {
			batch.setVersionProtocols(new ArrayList<>(versionProtocols));
		}
		if (!geolocations.isEmpty()) {
			batch.setGeolocations(new ArrayList<>(geolocations));
		}
		
		return batch;
	}
	
	public void clear() {
		servers.clear();
		users.clear();
		userInfo.clear();
		sessions.clear();
		nicknames.clear();
		kills.clear();
		pings.clear();
		platforms.clear();
		pluginVersions.clear();
		tps.clear();
		worlds.clear();
		worldTimes.clear();
		versionProtocols.clear();
		geolocations.clear();
	}
}

