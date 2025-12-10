package com.example.polystirolstats.core.config;

public interface CoreConfig {
	String getServerUuid();
	String getBackendUrl();
	int getSendInterval();
	boolean isDebugEnabled();
}

