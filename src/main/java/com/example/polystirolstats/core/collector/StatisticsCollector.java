package com.example.polystirolstats.core.collector;

import com.example.polystirolstats.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CopyOnWriteArrayList;

public class StatisticsCollector {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsCollector.class);
	
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
		
		// Всегда устанавливаем все поля, даже если списки пустые, чтобы они отправлялись как пустые массивы []
		batch.setServers(new ArrayList<>(servers));
		batch.setUsers(new ArrayList<>(users));
		batch.setUserInfo(new ArrayList<>(userInfo));
		batch.setSessions(new ArrayList<>(sessions));
		batch.setNicknames(new ArrayList<>(nicknames));
		batch.setKills(new ArrayList<>(kills));
		batch.setPings(new ArrayList<>(pings));
		batch.setPlatforms(new ArrayList<>(platforms));
		batch.setPluginVersions(new ArrayList<>(pluginVersions));
		batch.setTps(new ArrayList<>(tps));
		batch.setWorlds(new ArrayList<>(worlds));
		
		// Фильтруем записи с null world_id перед отправкой
		List<WorldTimeData> validWorldTimes = worldTimes.stream()
				.filter(wt -> {
					if (wt.getWorldId() == null) {
						LOGGER.warn("Пропущена запись WorldTimeData с null world_id для игрока {}", wt.getUuid());
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());
		batch.setWorldTimes(validWorldTimes);
		
		batch.setVersionProtocols(new ArrayList<>(versionProtocols));
		batch.setGeolocations(new ArrayList<>(geolocations));
		
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

