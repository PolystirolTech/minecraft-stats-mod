package com.example.polystirolstats.neoforge.events;

import com.example.polystirolstats.core.collector.StatisticsCollector;
import com.example.polystirolstats.core.model.*;
import com.example.polystirolstats.core.util.WorldIdMapper;
import com.example.polystirolstats.neoforge.adapter.NeoForgeStatisticsAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerEventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerEventListener.class);
	
	private final StatisticsCollector collector;
	private final String serverUuid;
	private final WorldIdMapper worldIdMapper;
	private int tickCounter = 0;
	private int tpsCollectionCounter = 0;
	private int pingCollectionCounter = 0;
	
	// Интервалы в тиках (20 тиков = 1 секунда)
	private static final int TPS_COLLECTION_INTERVAL = 20 * 60; // Каждую минуту
	private static final int PING_COLLECTION_INTERVAL = 20 * 60 * 5; // Каждые 5 минут
	
	public ServerEventListener(StatisticsCollector collector, String serverUuid, WorldIdMapper worldIdMapper) {
		this.collector = collector;
		this.serverUuid = serverUuid;
		this.worldIdMapper = worldIdMapper;
	}
	
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		MinecraftServer server = event.getServer();
		
		// Отправляем данные сервера
		ServerData serverData = new ServerData();
		serverData.setServerUuid(serverUuid);
		serverData.setName(server.getServerModName());
		serverData.setWebAddress(null);
		serverData.setIsInstalled(true);
		serverData.setIsProxy(false);
		serverData.setMaxPlayers(server.getMaxPlayers());
		serverData.setPlanVersion(null);
		collector.addServer(serverData);
		
		// Добавляем миры и регистрируем их в маппере
		for (ServerLevel level : server.getAllLevels()) {
			String worldName = NeoForgeStatisticsAdapter.getWorldName(level);
			
			// Регистрируем мир в маппере для получения ID
			worldIdMapper.registerWorld(worldName);
			
			WorldData worldData = new WorldData();
			worldData.setServerUuid(serverUuid);
			worldData.setWorldName(worldName);
			collector.addWorld(worldData);
		}
		
		// Добавляем версию плагина
		PluginVersionData pluginVersion = new PluginVersionData();
		pluginVersion.setServerUuid(serverUuid);
		pluginVersion.setPluginName("polystirolstats");
		pluginVersion.setVersion("1.0.0"); // TODO: получить из мода
		pluginVersion.setModified(System.currentTimeMillis());
		collector.addPluginVersion(pluginVersion);
	}
	
	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		// При остановке сервера можно отправить финальные данные
		LOGGER.info("Сервер останавливается, финальные данные будут отправлены");
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent.Post event) {
		MinecraftServer server = event.getServer();
		tickCounter++;
		tpsCollectionCounter++;
		pingCollectionCounter++;
		
		// Собираем TPS метрики каждую минуту
		if (tpsCollectionCounter >= TPS_COLLECTION_INTERVAL) {
			tpsCollectionCounter = 0;
			collectServerMetrics(server);
		}
		
		// Собираем пинг каждые 5 минут
		if (pingCollectionCounter >= PING_COLLECTION_INTERVAL) {
			pingCollectionCounter = 0;
			collectPingData(server);
		}
	}
	
	private void collectServerMetrics(MinecraftServer server) {
		TpsData tpsData = new TpsData();
		tpsData.setServerUuid(serverUuid);
		tpsData.setDate(System.currentTimeMillis());
		tpsData.setTps(NeoForgeStatisticsAdapter.getTPS(server));
		tpsData.setPlayersOnline(server.getPlayerList().getPlayerCount());
		tpsData.setCpuUsage(null); // Требует дополнительных библиотек
		tpsData.setRamUsage(NeoForgeStatisticsAdapter.getRamUsage());
		tpsData.setEntities(NeoForgeStatisticsAdapter.getTotalEntities(server));
		tpsData.setChunksLoaded(NeoForgeStatisticsAdapter.getTotalChunksLoaded(server));
		tpsData.setFreeDiskSpace(NeoForgeStatisticsAdapter.getFreeDiskSpace());
		collector.addTps(tpsData);
	}
	
	private void collectPingData(MinecraftServer server) {
		server.getPlayerList().getPlayers().forEach(player -> {
			int ping = player.connection != null ? player.connection.latency() : 0;
			
			PingData pingData = new PingData();
			pingData.setUuid(player.getUUID().toString());
			pingData.setServerUuid(serverUuid);
			pingData.setDate(System.currentTimeMillis());
			pingData.setMaxPing(ping);
			pingData.setMinPing(ping);
			pingData.setAvgPing(ping);
			collector.addPing(pingData);
		});
	}
}

