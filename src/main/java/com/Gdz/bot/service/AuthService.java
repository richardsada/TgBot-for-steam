package com.Gdz.bot.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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
            log.info("Отправка POST-запроса на URL: {}. Тело запроса: telegramId={}, steamLink={}",
                    url, telegramId, steamLink);

            Map<String, Object> body = Map.of(
                    "telegramId", telegramId,
                    "steamLink", steamLink
            );

            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            log.info("Получен ответ от сервера. Статус: {}, Тело: {}",
                    response.getStatusCode(), response.getBody());

            if (response.getBody() == null) {
                log.warn("Тело ответа пустое для telegramId: {}", telegramId);
                return "Ошибка: пустой ответ от сервера.";
            }

            JSONObject json = new JSONObject(response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Успешная привязка аккаунта Steam для telegramId: {}", telegramId);
            } else {
                log.warn("Привязка не выполнена для telegramId: {}. Статус: {}", telegramId, response.getStatusCode());
            }

            return formatBindResponse(json);

        } catch (HttpClientErrorException e) {

            log.warn("Клиентская ошибка при привязке для telegramId: {}. Статус: {}", telegramId, e.getStatusCode());

            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(responseBody);
                    return formatBindResponse(json);
                } catch (Exception ex) {
                    log.error("Ошибка при парсинге тела ответа для telegramId: {}", telegramId, ex);
                }
            }


            return String.format("""
                Привязка аккаунта к вашему Тг
                Статус: error
                Сообщение: %s
                Steam ID: -
                """, e.getMessage());

        } catch (RestClientException e) {
            log.error("Ошибка при запросе к Auth-сервису для telegramId: {}. Сообщение: {}", telegramId, e.getMessage(), e);
            return "Ошибка при привязке аккаунта. Попробуйте позже.";
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке ответа от Auth-сервиса для telegramId: {}. Сообщение: {}", telegramId, e.getMessage(), e);
            return "Произошла ошибка при обработке данных.";
        }
    }

    public String requestUnbind(Long telegramId) {
        String url = authUrl + "/link/" + telegramId;
        log.info("Запрос отвязки аккаунта Steam для telegramId: {}", telegramId);

        try {
            log.info("Отправка DELETE-запроса на URL: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
            log.info("Получен ответ от сервера. Статус: {}, Тело: {}",
                    response.getStatusCode(), response.getBody());

            if (response.getBody() == null) {
                log.warn("Тело ответа пустое для telegramId: {}", telegramId);
                return "Ошибка: пустой ответ от сервера.";
            }

            JSONObject json = new JSONObject(response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Успешная отвязка аккаунта Steam для telegramId: {}", telegramId);
            } else {
                log.warn("Отвязка не выполнена для telegramId: {}. Статус: {}", telegramId, response.getStatusCode());
            }

            return formatUnbindResponse(json);

        } catch (HttpClientErrorException e) {

            log.warn("Клиентская ошибка при отвязке для telegramId: {}. Статус: {}", telegramId, e.getStatusCode());

            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(responseBody);
                    return formatUnbindResponse(json);
                } catch (Exception ex) {
                    log.error("Ошибка при парсинге тела ответа для telegramId: {}", telegramId, ex);
                }
            }


            return String.format("""
                Отвязка аккаунта Steam
                Статус: error
                Сообщение: %s
                """, e.getMessage());

        } catch (RestClientException e) {
            log.error("Ошибка при запросе к Auth-сервису для telegramId: {}. Сообщение: {}", telegramId, e.getMessage(), e);
            return "Ошибка при отвязке аккаунта. Попробуйте позже.";
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке ответа от Auth-сервиса для telegramId: {}. Сообщение: {}", telegramId, e.getMessage(), e);
            return "Произошла ошибка при обработке данных.";
        }
    }

    private String formatBindResponse(JSONObject json) {
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
    }

    private String formatUnbindResponse(JSONObject json) {
        return String.format("""
            Отвязка аккаунта Steam
            Статус: %s
            Сообщение: %s
            """,
                json.optString("status", "-"),
                json.optString("message", "-")
        );
    }
}