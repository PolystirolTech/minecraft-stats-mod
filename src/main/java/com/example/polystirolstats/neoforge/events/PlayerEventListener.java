package com.example.polystirolstats.neoforge.events;

import com.example.polystirolstats.core.collector.StatisticsCollector;
import com.example.polystirolstats.core.model.*;
import com.example.polystirolstats.core.util.WorldIdMapper;
import com.example.polystirolstats.neoforge.adapter.NeoForgeStatisticsAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
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
	private final Map<UUID, Integer> playerKills = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> fallDeaths = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> creeperKillsWithEgg = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> diamondsMined = new ConcurrentHashMap<>();
	private final Map<UUID, Long> activeTime = new ConcurrentHashMap<>(); // Время онлайн без AFK
	private final Map<UUID, Integer> cannyCatMessages = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> firstJoinAfterRestart = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> nightMobKills = new ConcurrentHashMap<>();
	
	// Порог AFK: 5 минут без активности (в миллисекундах)
	private static final long AFK_THRESHOLD_MS = 5 * 60 * 1000L;
	
	// Флаг первого входа после перезапуска
	private static volatile boolean isFirstJoinAfterRestart = true;
	
	public static void setFirstJoinAfterRestart(boolean value) {
		isFirstJoinAfterRestart = value;
	}
	
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
		playerKills.put(uuid, 0);
		fallDeaths.put(uuid, 0);
		creeperKillsWithEgg.put(uuid, 0);
		diamondsMined.put(uuid, 0);
		activeTime.put(uuid, 0L);
		cannyCatMessages.put(uuid, 0);
		nightMobKills.put(uuid, 0);
		
		// Проверяем первый вход после перезапуска
		if (isFirstJoinAfterRestart) {
			firstJoinAfterRestart.put(uuid, 1);
			isFirstJoinAfterRestart = false; // Сбрасываем флаг после первого входа
		} else {
			firstJoinAfterRestart.put(uuid, 0);
		}
		
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
		playerKills.remove(uuid);
		fallDeaths.remove(uuid);
		creeperKillsWithEgg.remove(uuid);
		diamondsMined.remove(uuid);
		activeTime.remove(uuid);
		cannyCatMessages.remove(uuid);
		firstJoinAfterRestart.remove(uuid);
		nightMobKills.remove(uuid);
		
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
		// Обрабатываем смерть игрока
		if (event.getEntity() instanceof ServerPlayer victim) {
			handlePlayerDeath(event, victim);
		}
		// Обрабатываем убийство крипера куриным яйцом
		else if (event.getEntity() instanceof Creeper creeper) {
			handleCreeperDeath(event, creeper);
		}
		// Обрабатываем убийство монстров ночью
		else if (event.getEntity() instanceof Monster monster) {
			handleMonsterDeath(event, monster);
		}
	}
	
	private void handlePlayerDeath(LivingDeathEvent event, ServerPlayer victim) {
		
		UUID victimUuid = victim.getUUID();
		PlayerSession session = activeSessions.get(victimUuid);
		if (session != null) {
			session.deaths++;
		}
		
		DamageSource damageSource = event.getSource();
		
		// Проверяем смерть от падения
		if (damageSource.is(DamageTypes.FALL)) {
			if (activeSessions.containsKey(victimUuid)) {
				fallDeaths.merge(victimUuid, 1, Integer::sum);
			}
		}
		
		// Проверяем, есть ли убийца-игрок
		if (damageSource.getEntity() instanceof ServerPlayer killer) {
			UUID killerUuid = killer.getUUID();
			
			// Увеличиваем счетчик убийств игроков для убийцы
			if (activeSessions.containsKey(killerUuid)) {
				playerKills.merge(killerUuid, 1, Integer::sum);
			}
			
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
	
	private void handleCreeperDeath(LivingDeathEvent event, Creeper creeper) {
		DamageSource damageSource = event.getSource();
		
		// Проверяем, убил ли игрок крипера куриным яйцом
		if (damageSource.getEntity() instanceof ServerPlayer killer) {
			UUID killerUuid = killer.getUUID();
			
			// Проверяем, что в руке у игрока куриное яйцо
			boolean killedWithEgg = false;
			if (killer.getMainHandItem() != null && !killer.getMainHandItem().isEmpty()) {
				if (killer.getMainHandItem().is(Items.EGG)) {
					killedWithEgg = true;
				}
			}
			// Также проверяем вторую руку
			if (!killedWithEgg && killer.getOffhandItem() != null && !killer.getOffhandItem().isEmpty()) {
				if (killer.getOffhandItem().is(Items.EGG)) {
					killedWithEgg = true;
				}
			}
			
			if (killedWithEgg && activeSessions.containsKey(killerUuid)) {
				creeperKillsWithEgg.merge(killerUuid, 1, Integer::sum);
				// Обновляем время последней активности
				lastActivityTime.put(killerUuid, System.currentTimeMillis());
			}
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
		playerKills.clear();
		fallDeaths.clear();
		creeperKillsWithEgg.clear();
		diamondsMined.clear();
		activeTime.clear();
		cannyCatMessages.clear();
		firstJoinAfterRestart.clear();
		nightMobKills.clear();
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
		
		// Проверяем алмазную руду
		if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE || 
			blockName.contains("diamond_ore")) {
			diamondsMined.merge(uuid, 1, Integer::sum);
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
			
			// Проверяем, содержит ли сообщение "cannyCat"
			String message = event.getRawText();
			if (message != null && message.toLowerCase().contains("cannycat")) {
				cannyCatMessages.merge(uuid, 1, Integer::sum);
			}
			
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
		Long sessionStart = activeSessions.get(uuid) != null ? activeSessions.get(uuid).startTime : null;
		
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
		
		// Проверяем AFK статус и накапливаем активное время
		if (lastActivity != null && sessionStart != null) {
			long timeSinceLastActivity = currentTime - lastActivity;
			
			if (timeSinceLastActivity >= AFK_THRESHOLD_MS) {
				// Игрок в AFK - накапливаем время AFK
				// Добавляем время одного тика (50ms при 20 TPS)
				afkTime.merge(uuid, 50L, Long::sum);
			} else {
				// Игрок активен - накапливаем активное время
				// Добавляем время одного тика (50ms при 20 TPS)
				activeTime.merge(uuid, 50L, Long::sum);
			}
		}
	}
	
	private void handleMonsterDeath(LivingDeathEvent event, Monster monster) {
		DamageSource damageSource = event.getSource();
		
		// Проверяем, убил ли игрок монстра
		if (damageSource.getEntity() instanceof ServerPlayer killer) {
			UUID killerUuid = killer.getUUID();
			
			if (!activeSessions.containsKey(killerUuid)) {
				return;
			}
			
			// Проверяем, что сейчас ночь
			Level level = monster.level();
			if (level != null && isNightTime(level)) {
				nightMobKills.merge(killerUuid, 1, Integer::sum);
				// Обновляем время последней активности
				lastActivityTime.put(killerUuid, System.currentTimeMillis());
			}
		}
	}
	
	private boolean isNightTime(Level level) {
		long dayTime = level.getDayTime();
		// Ночь в Minecraft: время суток от 13000 до 23000 тиков
		return dayTime >= 13000 && dayTime < 23000;
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
		Integer playerKillsCount = playerKills.get(uuid);
		Integer fallDeathsCount = fallDeaths.get(uuid);
		Integer creeperKills = creeperKillsWithEgg.get(uuid);
		Integer diamonds = diamondsMined.get(uuid);
		Long active = activeTime.get(uuid);
		Integer cannyCat = cannyCatMessages.get(uuid);
		Integer firstJoin = firstJoinAfterRestart.get(uuid);
		Integer nightKills = nightMobKills.get(uuid);
		
		// Проверяем, есть ли хотя бы один счетчик > 0
		if ((blocks != null && blocks > 0) || (messages != null && messages > 0) || 
			(afk != null && afk > 0) || (quartz != null && quartz > 0) || 
			(deepslate != null && deepslate > 0) || (playerKillsCount != null && playerKillsCount > 0) ||
			(fallDeathsCount != null && fallDeathsCount > 0) || (creeperKills != null && creeperKills > 0) ||
			(diamonds != null && diamonds > 0) || (active != null && active > 0) ||
			(cannyCat != null && cannyCat > 0) || (firstJoin != null && firstJoin > 0) ||
			(nightKills != null && nightKills > 0)) {
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
			
			if (playerKillsCount != null && playerKillsCount > 0) {
				countersMap.put("player_kills", playerKillsCount);
			}
			
			if (fallDeathsCount != null && fallDeathsCount > 0) {
				countersMap.put("fall_deaths", fallDeathsCount);
			}
			
			if (creeperKills != null && creeperKills > 0) {
				countersMap.put("creeper_kills_with_egg", creeperKills);
			}
			
			if (diamonds != null && diamonds > 0) {
				countersMap.put("diamonds_mined", diamonds);
			}
			
			if (active != null && active > 0) {
				// Конвертируем миллисекунды в секунды для отправки
				countersMap.put("active_time", (int)(active / 1000));
			}
			
			if (cannyCat != null && cannyCat > 0) {
				countersMap.put("canny_cat_messages", cannyCat);
			}
			
			if (firstJoin != null && firstJoin > 0) {
				countersMap.put("first_join_after_restart", firstJoin);
			}
			
			if (nightKills != null && nightKills > 0) {
				countersMap.put("night_mob_kills", nightKills);
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
		
		// Добавляем player_kills
		for (Map.Entry<UUID, Integer> entry : playerKills.entrySet()) {
			UUID uuid = entry.getKey();
			Integer kills = entry.getValue();
			if (kills > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("player_kills", kills);
			}
		}
		
		// Добавляем fall_deaths
		for (Map.Entry<UUID, Integer> entry : fallDeaths.entrySet()) {
			UUID uuid = entry.getKey();
			Integer deaths = entry.getValue();
			if (deaths > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("fall_deaths", deaths);
			}
		}
		
		// Добавляем creeper_kills_with_egg
		for (Map.Entry<UUID, Integer> entry : creeperKillsWithEgg.entrySet()) {
			UUID uuid = entry.getKey();
			Integer kills = entry.getValue();
			if (kills > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("creeper_kills_with_egg", kills);
			}
		}
		
		// Добавляем diamonds_mined
		for (Map.Entry<UUID, Integer> entry : diamondsMined.entrySet()) {
			UUID uuid = entry.getKey();
			Integer diamonds = entry.getValue();
			if (diamonds > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("diamonds_mined", diamonds);
			}
		}
		
		// Добавляем active_time
		for (Map.Entry<UUID, Long> entry : activeTime.entrySet()) {
			UUID uuid = entry.getKey();
			Long active = entry.getValue();
			if (active > 0) {
				// Конвертируем миллисекунды в секунды для отправки
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("active_time", (int)(active / 1000));
			}
		}
		
		// Добавляем canny_cat_messages
		for (Map.Entry<UUID, Integer> entry : cannyCatMessages.entrySet()) {
			UUID uuid = entry.getKey();
			Integer cannyCat = entry.getValue();
			if (cannyCat > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("canny_cat_messages", cannyCat);
			}
		}
		
		// Добавляем first_join_after_restart
		for (Map.Entry<UUID, Integer> entry : firstJoinAfterRestart.entrySet()) {
			UUID uuid = entry.getKey();
			Integer firstJoin = entry.getValue();
			if (firstJoin > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("first_join_after_restart", firstJoin);
			}
		}
		
		// Добавляем night_mob_kills
		for (Map.Entry<UUID, Integer> entry : nightMobKills.entrySet()) {
			UUID uuid = entry.getKey();
			Integer kills = entry.getValue();
			if (kills > 0) {
				allCounters.computeIfAbsent(uuid, k -> new HashMap<>()).put("night_mob_kills", kills);
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
		// Сбрасываем счетчики убийств игроков
		for (UUID uuid : playerKills.keySet()) {
			playerKills.put(uuid, 0);
		}
		// Сбрасываем счетчики смертей от падения
		for (UUID uuid : fallDeaths.keySet()) {
			fallDeaths.put(uuid, 0);
		}
		// Сбрасываем счетчики убийств крипера куриным яйцом
		for (UUID uuid : creeperKillsWithEgg.keySet()) {
			creeperKillsWithEgg.put(uuid, 0);
		}
		// Сбрасываем счетчики добытых алмазов
		for (UUID uuid : diamondsMined.keySet()) {
			diamondsMined.put(uuid, 0);
		}
		// Сбрасываем счетчики активного времени
		for (UUID uuid : activeTime.keySet()) {
			activeTime.put(uuid, 0L);
		}
		// Сбрасываем счетчики сообщений с cannyCat
		for (UUID uuid : cannyCatMessages.keySet()) {
			cannyCatMessages.put(uuid, 0);
		}
		// Сбрасываем счетчики первого входа после перезапуска
		for (UUID uuid : firstJoinAfterRestart.keySet()) {
			firstJoinAfterRestart.put(uuid, 0);
		}
		// Сбрасываем счетчики убийств монстров ночью
		for (UUID uuid : nightMobKills.keySet()) {
			nightMobKills.put(uuid, 0);
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

