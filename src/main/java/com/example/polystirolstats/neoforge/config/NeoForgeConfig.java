package com.example.polystirolstats.neoforge.config;

import com.example.polystirolstats.core.config.CoreConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeConfig implements CoreConfig {
	public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	
	public static final ModConfigSpec.ConfigValue<String> SERVER_UUID = BUILDER
			.comment("UUID сервера из таблицы game_servers")
			.define("serverUuid", "");
	
	public static final ModConfigSpec.ConfigValue<String> BACKEND_URL = BUILDER
			.comment("URL бэкенда для отправки статистики")
			.define("backendUrl", "http://localhost:8000");
	
	public static final ModConfigSpec.IntValue SEND_INTERVAL = BUILDER
			.comment("Интервал отправки данных в секундах (по умолчанию 300 = 5 минут)")
			.defineInRange("sendInterval", 300, 10, Integer.MAX_VALUE);
	
	public static final ModConfigSpec.BooleanValue ENABLE_DEBUG = BUILDER
			.comment("Включить детальное логирование")
			.define("enableDebug", false);
	
	public static final ModConfigSpec SPEC = BUILDER.build();
	
	private final ModConfigSpec.ConfigValue<String> serverUuid;
	private final ModConfigSpec.ConfigValue<String> backendUrl;
	private final ModConfigSpec.IntValue sendInterval;
	private final ModConfigSpec.BooleanValue enableDebug;
	
	public NeoForgeConfig() {
		this.serverUuid = SERVER_UUID;
		this.backendUrl = BACKEND_URL;
		this.sendInterval = SEND_INTERVAL;
		this.enableDebug = ENABLE_DEBUG;
	}
	
	@Override
	public String getServerUuid() {
		return serverUuid.get();
	}
	
	@Override
	public String getBackendUrl() {
		return backendUrl.get();
	}
	
	@Override
	public int getSendInterval() {
		return sendInterval.get();
	}
	
	@Override
	public boolean isDebugEnabled() {
		return enableDebug.get();
	}
}

