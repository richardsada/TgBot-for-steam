package com.Gdz.bot;

import com.Gdz.bot.dto.SteamStatsDto;
import com.Gdz.bot.service.AuthService;
import com.Gdz.bot.service.SteamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SteamHelperBot extends TelegramLongPollingBot {

    private final AuthService authService;
    private final SteamService steamService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText().trim();
        String lowerText = text.toLowerCase();
        long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        log.info("Сообщение от {}: {}", telegramId, text);

        SendMessage response;

        if (lowerText.startsWith("/bind")) {
            String steamLink = text.substring("/bind".length()).trim();
            if (steamLink.isEmpty()) {
                response = createMessage(chatId, "Используй /bind <ссылка на Steam аккаунт>");
            } else {
                response = handleBind(chatId, telegramId, steamLink);
            }
        } else {
            response = switch (lowerText) {

                case "/start" -> createMessage(chatId, """
                        Добро пожаловать!

                        Я помогу тебе посмотреть статистику Steam.
                        Используй /bind <ссылка на Steam> чтобы привязать аккаунт.
                        """);

                case "/help" -> createMessage(chatId, """
                        Доступные команды:

                        /bind <ссылка на Steam> — привязать Steam
                        /stats — показать статистику
                        /help — помощь
                        """);

                case "/stats" -> handleStats(chatId, telegramId);

                default -> createMessage(chatId, "Неизвестная команда. Напиши /help");
            };
        }

        try {
            execute(response);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    private SendMessage handleBind(long chatId, Long telegramId, String steamLink) {
        try {
            String result = authService.requestBindLink(telegramId, steamLink);

            return createMessage(chatId, """
                    🔗 Привязка Steam:

                    %s
                    """.formatted(result));

        } catch (Exception e) {
            log.error("Ошибка bind", e);
            return createMessage(chatId, "Ошибка сервиса авторизации. Попробуй позже.");
        }
    }

    private SendMessage handleStats(long chatId, Long telegramId) {
        try {
            SteamStatsDto stats = steamService.getStats(telegramId);

            if (stats == null) {
                return createMessage(chatId, """
                        ❌ Аккаунт не привязан.
                        Используй /bind <ссылка на Steam>
                        """);
            }

            return createMessage(chatId, """
                    🎮 Статистика Steam

                    👤 Ник: %s
                    🎲 Игр: %d
                    ⏱ Часов всего: %d
                    🏆 Топ игра: %s
                    """
                    .formatted(
                            stats.getNickname(),
                            stats.getGamesCount(),
                            stats.getHoursTotal(),
                            stats.getTopGame()
                    ));

        } catch (Exception e) {
            log.error("Ошибка получения статистики", e);
            return createMessage(chatId, "Ошибка Steam сервиса. Попробуй позже.");
        }
    }

    private SendMessage createMessage(long chatId, String text) {
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
    }
}