package com.Gdz.bot.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final RestTemplate restTemplate;

    @Value("${ai.stats.service.url}")
    private String aiUrl;

    public String getAiStats(Long telegramId) {
        String url = aiUrl + "/api/ai/summaries/" + telegramId;
        log.info("Запрос сводки от ИИ для telegramId: {}", telegramId);

        try {
            log.debug("Отправка GET-запроса на URL: {}", url);
            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                log.warn("Ответ от сервера пуст для telegramId: {}", telegramId);
                return null;
            }

            log.debug("Получен ответ от сервера: {}", response);

            JSONObject json = new JSONObject(response);
            String status = json.optString("status", "-");
            String summary = json.optString("summary", "-");

            log.info("Сводка от ИИ успешно получена для telegramId: {}", telegramId);

            return String.format("""
                    Описание от ИИ
                    Статус: %s
                    Сообщение: %s
                    """,
                    status,
                    summary
            );
        } catch (RestClientException e) {
            log.error("Ошибка при запросе к AI-сервису для telegramId: {}. Сообщение: {}", telegramId, e.getMessage(), e);
            return "Ошибка при получении сводки от ИИ. Попробуйте позже.";
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке ответа от AI-сервиса для telegramId: {}. Сообщение: {}", telegramId, e.getMessage(), e);
            return "Произошла ошибка при обработке данных.";
        }
    }
}