package com.example.polystirolstats.neoforge.events;

import com.example.polystirolstats.core.collector.StatisticsCollector;
import com.example.polystirolstats.core.model.*;
import com.example.polystirolstats.core.util.WorldIdMapper;
import com.example.polystirolstats.neoforge.adapter.NeoForgeStatisticsAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerEventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerEventListener.class);
	
	private final StatisticsCollector collector;
	private final String serverUuid;
	private final WorldIdMapper worldIdMapper;
	private final Map<UUID, PlayerSession> activeSessions = new ConcurrentHashMap<>();
	private final Map<UUID, BlockPos> lastPositions = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> blocksTraveled = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> messagesSent = new ConcurrentHashMap<>();
	private final Map<UUID, Long> lastActivityTime = new ConcurrentHashMap<>();
	private final Map<UUID, Long> afkTime = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> quartzMined = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> deepslateMined = new ConcurrentHashMap<>();
	
	// Порог AFK: 5 минут без активности (в миллисекундах)
	private static final long AFK_THRESHOLD_MS = 5 * 60 * 1000L;
	
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
		
		// Инициализируем отслеживание позиции
		lastPositions.put(uuid, player.blockPosition());
		blocksTraveled.put(uuid, 0);
		messagesSent.put(uuid, 0);
		lastActivityTime.put(uuid, now);
		afkTime.put(uuid, 0L);
		quartzMined.put(uuid, 0);
		deepslateMined.put(uuid, 0);
		
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
		
		// Собираем счетчики игрока перед очисткой, чтобы они не потерялись
		collectPlayerCounters(uuid);
		
		PlayerSession session = activeSessions.remove(uuid);
		
		// Очищаем данные отслеживания движения
		lastPositions.remove(uuid);
		blocksTraveled.remove(uuid);
		messagesSent.remove(uuid);
		lastActivityTime.remove(uuid);
		afkTime.remove(uuid);
		quartzMined.remove(uuid);
		deepslateMined.remove(uuid);
		
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
	
	
	public void finalizeAllSessions() {
		long now = System.currentTimeMillis();
		int sessionCount = activeSessions.size();
		
		for (Map.Entry<UUID, PlayerSession> entry : activeSessions.entrySet()) {
			UUID uuid = entry.getKey();
			PlayerSession session = entry.getValue();
			
			// Завершаем сессию
			session.endTime = now;
			
			// Вычисляем время в игровых режимах на основе общей длительности сессии
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
		
		activeSessions.clear();
		lastPositions.clear();
		blocksTraveled.clear();
		messagesSent.clear();
		lastActivityTime.clear();
		afkTime.clear();
		quartzMined.clear();
		deepslateMined.clear();
		if (sessionCount > 0) {
			LOGGER.info("Завершено {} активных сессий при остановке сервера", sessionCount);
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		if (!(event.getPlayer() instanceof ServerPlayer player)) {
			return;
		}
		
		UUID uuid = player.getUUID();
		if (!activeSessions.containsKey(uuid)) {
			return;
		}
		
		Block block = event.getState().getBlock();
		ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
		
		if (blockId == null) {
			return;
		}
		
		String blockName = blockId.toString();
		
		// Проверяем кварц (nether quartz ore и deepslate variants)
		if (block == Blocks.NETHER_QUARTZ_ORE || 
			blockName.contains("quartz_ore") || 
			blockName.contains("quartz_block")) {
			quartzMined.merge(uuid, 1, Integer::sum);
			// Обновляем время последней активности
			lastActivityTime.put(uuid, System.currentTimeMillis());
		}
		
		// Проверяем глубинный сланец
		if (block == Blocks.DEEPSLATE || blockName.contains("deepslate")) {
			deepslateMined.merge(uuid, 1, Integer::sum);
			// Обновляем время последней активности
			lastActivityTime.put(uuid, System.currentTimeMillis());
		}
	}
	
	@SubscribeEvent
	public void onServerChat(ServerChatEvent event) {
		ServerPlayer player = event.getPlayer();
		if (player == null) {
			return;
		}
		
		UUID uuid = player.getUUID();
		if (activeSessions.containsKey(uuid)) {
			// Увеличиваем счетчик отправленных сообщений
			messagesSent.merge(uuid, 1, Integer::sum);
			// Обновляем время последней активности
			lastActivityTime.put(uuid, System.currentTimeMillis());
		}
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		
		UUID uuid = player.getUUID();
		if (!activeSessions.containsKey(uuid)) {
			return;
		}
		
		BlockPos currentPos = player.blockPosition();
		BlockPos lastPos = lastPositions.get(uuid);
		
		long currentTime = System.currentTimeMillis();
		Long lastActivity = lastActivityTime.get(uuid);
		
		if (lastPos != null && !currentPos.equals(lastPos)) {
			// Вычисляем количество пройденных блоков
			int dx = Math.abs(currentPos.getX() - lastPos.getX());
			int dy = Math.abs(currentPos.getY() - lastPos.getY());
			int dz = Math.abs(currentPos.getZ() - lastPos.getZ());
			int blocks = dx + dy + dz;
			
			// Накапливаем пройденные блоки
			blocksTraveled.merge(uuid, blocks, Integer::sum);
			
			// Обновляем последнюю позицию
			lastPositions.put(uuid, currentPos);
			
			// Обновляем время последней активности при движении
			lastActivityTime.put(uuid, currentTime);
		} else if (lastPos == null) {
			// Инициализируем позицию, если её еще нет
			lastPositions.put(uuid, currentPos);
		}
		
		// Проверяем AFK статус
		if (lastActivity != null) {
			long timeSinceLastActivity = currentTime - lastActivity;
			
			if (timeSinceLastActivity >= AFK_THRESHOLD_MS) {
				// Игрок в AFK - накапливаем время AFK
				// Добавляем время одного тика (50ms при 20 TPS)
				afkTime.merge(uuid, 50L, Long::sum);
			}
		}
	}
	
	/**
	 * Собирает счетчики конкретного игрока и добавляет их в collector
	 */
	private void collectPlayerCounters(UUID uuid) {
		Integer blocks = blocksTraveled.get(uuid);
		Integer messages = messagesSent.get(uuid);
		Long afk = afkTime.get(uuid);
		Integer quartz = quartzMined.get(uuid);
		Integer deepslate = deepslateMined.get(uuid);
		
		// Проверяем, есть ли хотя бы один счетчик > 0
		if ((blocks != null && blocks > 0) || (messages != null && messages > 0) || 
			(afk != null && afk > 0) || (quartz != null && quartz > 0) || 
			(deepslate != null && deepslate > 0)) {
			Map<String, Integer> countersMap = new HashMap<>();
			
			if (blocks != null && blocks > 0) {
				countersMap.put("blocks_traveled", blocks);
			}
			
			if (messages != null && messages > 0) {
				countersMap.put("messages_sent", messages);
			}
			
			if (afk != null && afk > 0) {
				// Конвертируем миллисекунды в секунды для отправки
				countersMap.put("afk_time", (int)(afk / 1000));
			}
			
			if (quartz != null && quartz > 0) {
				countersMap.put("quartz_mined", quartz);
			}
			
			if (deepslate != null && deepslate > 0) {
				countersMap.put("deepslate_mined", deepslate);
			}
			
			if (!countersMap.isEmpty()) {
				CounterData counterData = new CounterData();
				counterData.setUuid(uuid.toString());
				counterData.setServerUuid(serverUuid);
				counterData.setCounters(countersMap);
				
				collector.addCounter(counterData);
			}
		}
	}
	
	public void collectAndSendCounters() {
		// Объединяем все UUID из всех счетчиков
		Map<UUID, Map<String, Integer>> allCounters = new HashMap<>();
		
		// Добавляем blocks_traveled
		for (Map.Entry<UUID, Integer> entry : blocksTraveled.entrySet()) {
			UUID uuid = entry.getKey();
			Integer blocks = entry.getValue();
			if (blocks > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("blocks_traveled", blocks);
			}
		}
		
		// Добавляем messages_sent
		for (Map.Entry<UUID, Integer> entry : messagesSent.entrySet()) {
			UUID uuid = entry.getKey();
			Integer messages = entry.getValue();
			if (messages > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("messages_sent", messages);
			}
		}
		
		// Добавляем afk_time
		for (Map.Entry<UUID, Long> entry : afkTime.entrySet()) {
			UUID uuid = entry.getKey();
			Long afk = entry.getValue();
			if (afk > 0) {
				// Конвертируем миллисекунды в секунды для отправки
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("afk_time", (int)(afk / 1000));
			}
		}
		
		// Добавляем quartz_mined
		for (Map.Entry<UUID, Integer> entry : quartzMined.entrySet()) {
			UUID uuid = entry.getKey();
			Integer quartz = entry.getValue();
			if (quartz > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("quartz_mined", quartz);
			}
		}
		
		// Добавляем deepslate_mined
		for (Map.Entry<UUID, Integer> entry : deepslateMined.entrySet()) {
			UUID uuid = entry.getKey();
			Integer deepslate = entry.getValue();
			if (deepslate > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("deepslate_mined", deepslate);
			}
		}
		
		// Создаем CounterData для каждого игрока с хотя бы одним счетчиком > 0
		for (Map.Entry<UUID, Map<String, Integer>> entry : allCounters.entrySet()) {
			UUID uuid = entry.getKey();
			Map<String, Integer> countersMap = entry.getValue();
			
			if (!countersMap.isEmpty()) {
				CounterData counterData = new CounterData();
				counterData.setUuid(uuid.toString());
				counterData.setServerUuid(serverUuid);
				counterData.setCounters(countersMap);
				
				collector.addCounter(counterData);
			}
		}
	}
	
	public void resetCounters() {
		// Сбрасываем счетчики пройденных блоков, но сохраняем последние позиции
		for (UUID uuid : blocksTraveled.keySet()) {
			blocksTraveled.put(uuid, 0);
		}
		// Сбрасываем счетчики отправленных сообщений
		for (UUID uuid : messagesSent.keySet()) {
			messagesSent.put(uuid, 0);
		}
		// Сбрасываем счетчики AFK времени
		for (UUID uuid : afkTime.keySet()) {
			afkTime.put(uuid, 0L);
		}
		// Сбрасываем счетчики добытого кварца
		for (UUID uuid : quartzMined.keySet()) {
			quartzMined.put(uuid, 0);
		}
		// Сбрасываем счетчики добытого глубинного сланца
		for (UUID uuid : deepslateMined.keySet()) {
			deepslateMined.put(uuid, 0);
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

