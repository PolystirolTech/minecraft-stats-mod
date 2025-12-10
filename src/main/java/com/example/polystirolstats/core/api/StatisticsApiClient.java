package com.example.polystirolstats.core.api;

import com.example.polystirolstats.core.model.BatchRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class StatisticsApiClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsApiClient.class);
	
	private final String backendUrl;
	private final HttpClient httpClient;
	private final Gson gson;
	
	public StatisticsApiClient(String backendUrl) {
		this.backendUrl = backendUrl;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();
		this.gson = new GsonBuilder().create();
	}
	
	public boolean sendBatch(BatchRequest batchRequest) {
		try {
			String json = gson.toJson(batchRequest);
			
			URI uri = URI.create(backendUrl + "/api/v1/statistics/minecraft/batch");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(uri)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(Duration.ofSeconds(10))
					.build();
			
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			
			int statusCode = response.statusCode();
			if (statusCode == 200 || statusCode == 201) {
				LOGGER.debug("Статистика успешно отправлена на сервер. Код ответа: {}", statusCode);
				return true;
			} else {
				LOGGER.warn("Ошибка отправки статистики. Код ответа: {}. Ответ: {}", statusCode, response.body());
				return false;
			}
		} catch (Exception e) {
			LOGGER.error("Ошибка при отправке статистики: {}", e.getMessage(), e);
			return false;
		}
	}
}

