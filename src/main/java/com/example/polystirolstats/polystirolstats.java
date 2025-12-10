package com.example.polystirolstats;

import com.example.polystirolstats.core.api.StatisticsApiClient;
import com.example.polystirolstats.core.collector.StatisticsCollector;
import com.example.polystirolstats.core.model.BatchRequest;
import com.example.polystirolstats.neoforge.config.NeoForgeConfig;
import com.example.polystirolstats.neoforge.events.PlayerEventListener;
import com.example.polystirolstats.neoforge.events.ServerEventListener;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(polystirolstats.MODID)
public class polystirolstats {
	// Define mod id in a common place for everything to reference
	public static final String MODID = "polystirolstats";
	// Directly reference a slf4j logger
	public static final Logger LOGGER = LogUtils.getLogger();
	
	private StatisticsCollector collector;
	private StatisticsApiClient apiClient;
	private NeoForgeConfig config;
	private PlayerEventListener playerEventListener;
	private ServerEventListener serverEventListener;
	private int sendIntervalTicks;
	private int tickCounter = 0;

	// The constructor for the mod class is the first code that is run when your mod is loaded.
	// FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
	public polystirolstats(IEventBus modEventBus, ModContainer modContainer) {
		// Register config
		modContainer.registerConfig(ModConfig.Type.SERVER, NeoForgeConfig.SPEC);
		modEventBus.addListener(this::onConfigLoad);
		
		// Register ourselves for server and other game events we are interested in.
		NeoForge.EVENT_BUS.register(this);
	}
	
	private void onConfigLoad(ModConfigEvent event) {
		if (event.getConfig().getSpec() == NeoForgeConfig.SPEC) {
			config = new NeoForgeConfig();
			
			String serverUuid = config.getServerUuid();
			String backendUrl = config.getBackendUrl();
			
			if (serverUuid == null || serverUuid.isEmpty()) {
				LOGGER.error("serverUuid не настроен в конфигурации! Мод не будет работать.");
				return;
			}
			
			if (backendUrl == null || backendUrl.isEmpty()) {
				LOGGER.error("backendUrl не настроен в конфигурации! Мод не будет работать.");
				return;
			}
			
			// Инициализация компонентов
			collector = new StatisticsCollector();
			apiClient = new StatisticsApiClient(backendUrl);
			sendIntervalTicks = config.getSendInterval() * 20; // Конвертируем секунды в тики
			
			// Регистрация обработчиков событий
			playerEventListener = new PlayerEventListener(collector, serverUuid);
			serverEventListener = new ServerEventListener(collector, serverUuid);
			NeoForge.EVENT_BUS.register(playerEventListener);
			NeoForge.EVENT_BUS.register(serverEventListener);
			
			LOGGER.info("PolystirolStats инициализирован. Server UUID: {}, Backend URL: {}, Send Interval: {} секунд", 
					serverUuid, backendUrl, config.getSendInterval());
		}
	}

	@SubscribeEvent
	public void onServerTick(ServerTickEvent.Post event) {
		if (collector == null || apiClient == null) {
			return;
		}
		
		tickCounter++;
		
		// Периодическая отправка данных
		if (tickCounter >= sendIntervalTicks) {
			tickCounter = 0;
			sendBatchData(event.getServer());
		}
	}
	
	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		// Отправляем оставшиеся данные при выключении
		if (collector != null && apiClient != null && config != null) {
			sendBatchData(event.getServer());
			LOGGER.info("Финальные данные статистики отправлены при остановке сервера");
		}
	}
	
	private void sendBatchData(MinecraftServer server) {
		if (server == null || config == null) {
			return;
		}
		
		String serverUuid = config.getServerUuid();
		BatchRequest batch = collector.getBatchData(serverUuid);
		
		// Проверяем, есть ли данные для отправки
		boolean hasData = batch.getServers() != null && !batch.getServers().isEmpty() ||
				batch.getUsers() != null && !batch.getUsers().isEmpty() ||
				batch.getSessions() != null && !batch.getSessions().isEmpty() ||
				batch.getKills() != null && !batch.getKills().isEmpty() ||
				batch.getPings() != null && !batch.getPings().isEmpty() ||
				batch.getTps() != null && !batch.getTps().isEmpty() ||
				batch.getWorlds() != null && !batch.getWorlds().isEmpty() ||
				batch.getUserInfo() != null && !batch.getUserInfo().isEmpty() ||
				batch.getNicknames() != null && !batch.getNicknames().isEmpty() ||
				batch.getPlatforms() != null && !batch.getPlatforms().isEmpty() ||
				batch.getPluginVersions() != null && !batch.getPluginVersions().isEmpty() ||
				batch.getWorldTimes() != null && !batch.getWorldTimes().isEmpty() ||
				batch.getVersionProtocols() != null && !batch.getVersionProtocols().isEmpty() ||
				batch.getGeolocations() != null && !batch.getGeolocations().isEmpty();
		
		if (hasData) {
			// Отправляем асинхронно, чтобы не блокировать основной поток
			server.execute(() -> {
				boolean success = apiClient.sendBatch(batch);
				if (success) {
					collector.clear();
					if (config.isDebugEnabled()) {
						LOGGER.debug("Статистика успешно отправлена и очищена");
					}
				}
			});
		}
	}
}
