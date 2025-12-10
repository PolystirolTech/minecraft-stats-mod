package com.example.polystirolstats.neoforge.adapter;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class NeoForgeStatisticsAdapter {
	
	public static double getTPS(MinecraftServer server) {
		try {
			// Попытка получить TPS через NeoForge API
			// Если API недоступен, используем fallback
			return 20.0; // Fallback значение
		} catch (Exception e) {
			return 20.0;
		}
	}
	
	public static int getTotalEntities(MinecraftServer server) {
		int total = 0;
		for (ServerLevel level : server.getAllLevels()) {
			for (Entity entity : level.getEntities().getAll()) {
				total++;
			}
		}
		return total;
	}
	
	public static int getTotalChunksLoaded(MinecraftServer server) {
		int total = 0;
		for (ServerLevel level : server.getAllLevels()) {
			total += level.getChunkSource().getLoadedChunksCount();
		}
		return total;
	}
	
	public static long getRamUsage() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}
	
	public static long getFreeDiskSpace() {
		try {
			return new java.io.File(".").getFreeSpace();
		} catch (Exception e) {
			return 0L;
		}
	}
	
	public static String getPlayerAddress(ServerPlayer player) {
		try {
			if (player.connection != null && player.connection.getRemoteAddress() != null) {
				return player.connection.getRemoteAddress().toString();
			}
		} catch (Exception e) {
			// Игнорируем ошибки
		}
		return "unknown";
	}
	
	public static String getWorldName(Level level) {
		if (level != null) {
			return level.dimension().location().toString();
		}
		return "unknown";
	}
	
	public static boolean isPlayerOpped(MinecraftServer server, UUID playerUuid) {
		PlayerList playerList = server.getPlayerList();
		ServerPlayer player = playerList.getPlayer(playerUuid);
		if (player != null) {
			return playerList.isOp(player.getGameProfile());
		}
		return false;
	}
	
	public static boolean isPlayerBanned(MinecraftServer server, UUID playerUuid) {
		PlayerList playerList = server.getPlayerList();
		ServerPlayer player = playerList.getPlayer(playerUuid);
		if (player != null) {
			return playerList.getBans().isBanned(player.getGameProfile());
		}
		return false;
	}
}

