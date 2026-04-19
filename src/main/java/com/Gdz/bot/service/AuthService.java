package com.Gdz.bot.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authUrl;

    public String requestBindLink(Long telegramId, String steamLink) {
        String url = authUrl + "/bind";
        log.info("Запрос привязки аккаунта Steam для telegramId: {}", telegramId);

        try {
            log.debug("Отправка POST-запроса на URL: {}. Тело запроса: telegramId={}, steamLink={}",
                    url, telegramId, steamLink);

            Map<String, Object> body = Map.of(
                    "telegramId", telegramId,
                    "steamLink", steamLink
            );

            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            log.debug("Получен ответ от сервера. Статус: {}, Тело: {}",
                    response.getStatusCode(), response.getBody());

            if (response.getBody() == null) {
                log.warn("Тело ответа пустое для telegramId: {}", telegramId);
                return "Ошибка: пустой ответ от сервера.";
            }

            JSONObject json = new JSONObject(response.getBody());

            log.info("Успешная привязка аккаунта Steam для telegramId: {}", telegramId);

            return String.format("""
                    Привязка аккаунта к вашему Тг
                    Статус: %s
                    Сообщение: %s
                    Steam ID: %s
                    """,
                    json.optString("status", "-"),
                    json.optString("message", "-"),
                    json.optString("steamId", "-")
            );
        } catch (RestClientException e) {
            log.error("Ошибка при запросе к Auth-сервису для telegramId: {}. Сообщение: {}", telegramId, e.getMessage(), e);
            return "Ошибка при привязке аккаунта. Попробуйте позже.";
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке ответа от Auth-сервиса для telegramId: {}. Сообщение: {}", telegramId, e.getMessage(), e);
            return "Произошла ошибка при обработке данных.";
        }
    }
}