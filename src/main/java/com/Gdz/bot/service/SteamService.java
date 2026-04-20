package com.Gdz.bot.service;

import com.Gdz.bot.dto.SteamStatsDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SteamService {

    private static final Logger log = LoggerFactory.getLogger(SteamService.class);

    private final RestTemplate restTemplate;

    @Value("${steam.stats.service.url}")
    private String steamUrl;

    public SteamStatsDto getStats(Long telegramId) {
        String url = steamUrl + "/stats/" + telegramId;
        log.info("Запрос статистики Steam для telegramId: {}", telegramId);

        try {
            log.info("Отправка GET-запроса на URL: {}", url);
            SteamStatsDto stats = restTemplate.getForObject(url, SteamStatsDto.class);

            if (stats == null) {
                log.warn("Ответ от сервера пуст для telegramId: {}", telegramId);
                return null;
            }

            log.info("Статистика Steam успешно получена для telegramId: {}", telegramId);
            return stats;
        } catch (RestClientException e) {
            log.error("Ошибка при запросе к Steam-сервису для telegramId: {}. Сообщение: {}", telegramId, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке ответа от Steam-сервиса для telegramId: {}. Сообщение: {}", telegramId, e.getMessage(), e);
            return null;
        }
    }
}
