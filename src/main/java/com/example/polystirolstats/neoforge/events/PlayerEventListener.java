package com.example.polystirolstats.neoforge.events;

import com.example.polystirolstats.core.collector.StatisticsCollector;
import com.example.polystirolstats.core.model.*;
import com.example.polystirolstats.core.util.WorldIdMapper;
import com.example.polystirolstats.neoforge.adapter.NeoForgeStatisticsAdapter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerEventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerEventListener.class);
	
	private final StatisticsCollector collector;
	private final String serverUuid;
	private final WorldIdMapper worldIdMapper;
	private final Map<UUID, PlayerSession> activeSessions = new ConcurrentHashMap<>();
	
	public PlayerEventListener(StatisticsCollector collector, String serverUuid, WorldIdMapper worldIdMapper) {
		this.collector = collector;
		this.serverUuid = serverUuid;
		this.worldIdMapper = worldIdMapper;
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		
		UUID uuid = player.getUUID();
		long now = System.currentTimeMillis();
		
		// Создаем запись пользователя
		UserData userData = new UserData();
		userData.setUuid(uuid.toString());
		userData.setRegistered(now);
		userData.setName(player.getGameProfile().getName());
		userData.setTimesKicked(0);
		collector.addUser(userData);
		
		// Создаем UserInfo
		UserInfoData userInfo = new UserInfoData();
		userInfo.setUuid(uuid.toString());
		userInfo.setServerUuid(serverUuid);
		userInfo.setJoinAddress(NeoForgeStatisticsAdapter.getPlayerAddress(player));
		userInfo.setRegistered(now);
		userInfo.setOpped(NeoForgeStatisticsAdapter.isPlayerOpped(player.getServer(), uuid));
		userInfo.setBanned(NeoForgeStatisticsAdapter.isPlayerBanned(player.getServer(), uuid));
		collector.addUserInfo(userInfo);
		
		// Создаем сессию
		PlayerSession session = new PlayerSession();
		session.uuid = uuid.toString();
		session.serverUuid = serverUuid;
		session.startTime = now;
		session.mobKills = 0;
		session.deaths = 0;
		session.afkTime = 0;
		session.joinAddress = NeoForgeStatisticsAdapter.getPlayerAddress(player);
		session.currentGameType = player.gameMode.getGameModeForPlayer();
		session.currentWorld = NeoForgeStatisticsAdapter.getWorldName(player.level());
		activeSessions.put(uuid, session);
		
		// Добавляем никнейм
		NicknameData nickname = new NicknameData();
		nickname.setUuid(uuid.toString());
		nickname.setNickname(player.getGameProfile().getName());
		nickname.setServerUuid(serverUuid);
		nickname.setLastUsed(now);
		collector.addNickname(nickname);
		
		// Добавляем платформу
		PlatformData platform = new PlatformData();
		platform.setUuid(uuid.toString());
		platform.setPlatform(0); // Java Edition
		platform.setJavaUsername(player.getGameProfile().getName());
		// Версия протокола будет определяться на стороне сервера или оставлена как "unknown"
		platform.setVersion("unknown");
		collector.addPlatform(platform);
		
		// Версия протокола - опциональное поле, можно оставить пустым
		// или реализовать получение через другой API в будущем
	}
	
	@SubscribeEvent
	public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		
		UUID uuid = player.getUUID();
		PlayerSession session = activeSessions.remove(uuid);
		
		if (session != null) {
			// Завершаем сессию
			session.endTime = System.currentTimeMillis();
			
			// Вычисляем время в игровых режимах на основе общей длительности сессии
			// Упрощенный подход: считаем, что игрок был в текущем режиме все время
			// В реальной реализации можно отслеживать изменения через события
			long sessionDuration = session.endTime - session.startTime;
			if (session.currentGameType != null) {
				saveGameModeTime(uuid, session, session.currentGameType, sessionDuration);
			}
			
			SessionData sessionData = new SessionData();
			sessionData.setUuid(session.uuid);
			sessionData.setServerUuid(session.serverUuid);
			sessionData.setSessionStart(session.startTime);
			sessionData.setSessionEnd(session.endTime);
			sessionData.setMobKills(session.mobKills);
			sessionData.setDeaths(session.deaths);
			sessionData.setAfkTime(session.afkTime);
			sessionData.setJoinAddress(session.joinAddress);
			collector.addSession(sessionData);
		}
	}
	
	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer victim)) {
			return;
		}
		
		UUID victimUuid = victim.getUUID();
		PlayerSession session = activeSessions.get(victimUuid);
		if (session != null) {
			session.deaths++;
		}
		
		// Проверяем, есть ли убийца
		if (event.getSource().getEntity() instanceof ServerPlayer killer) {
			UUID killerUuid = killer.getUUID();
			String weapon = "UNKNOWN";
			
			if (killer.getMainHandItem() != null && !killer.getMainHandItem().isEmpty()) {
				weapon = killer.getMainHandItem().getItem().toString();
				if (weapon.length() > 30) {
					weapon = weapon.substring(0, 30);
				}
			}
			
			KillData killData = new KillData();
			killData.setKillerUuid(killerUuid.toString());
			killData.setVictimUuid(victimUuid.toString());
			killData.setServerUuid(serverUuid);
			killData.setWeapon(weapon);
			killData.setDate(System.currentTimeMillis());
			killData.setSessionId(null);
			collector.addKill(killData);
		}
	}
	
	
	private void saveGameModeTime(UUID uuid, PlayerSession session, GameType gameType, long timeSpent) {
		// Получаем world_id из маппера по имени мира
		String worldName = session.currentWorld;
		if (worldName == null || worldName.isEmpty()) {
			LOGGER.warn("Не удалось сохранить время игрового режима для игрока {}: имя мира не указано", uuid);
			return;
		}
		
		Integer worldId = worldIdMapper.getWorldId(worldName);
		if (worldId == null) {
			LOGGER.warn("Не удалось сохранить время игрового режима для игрока {}: мир '{}' не зарегистрирован в маппере", uuid, worldName);
			return;
		}
		
		WorldTimeData worldTime = new WorldTimeData();
		worldTime.setUuid(uuid.toString());
		worldTime.setWorldId(worldId);
		worldTime.setServerUuid(serverUuid);
		worldTime.setSessionId(null);
		
		switch (gameType) {
			case SURVIVAL -> worldTime.setSurvivalTime(timeSpent);
			case CREATIVE -> worldTime.setCreativeTime(timeSpent);
			case ADVENTURE -> worldTime.setAdventureTime(timeSpent);
			case SPECTATOR -> worldTime.setSpectatorTime(timeSpent);
		}
		
		collector.addWorldTime(worldTime);
	}
	
	
	private static class PlayerSession {
		String uuid;
		String serverUuid;
		long startTime;
		Long endTime;
		int mobKills;
		int deaths;
		long afkTime;
		String joinAddress;
		GameType currentGameType;
		String currentWorld;
	}
}

