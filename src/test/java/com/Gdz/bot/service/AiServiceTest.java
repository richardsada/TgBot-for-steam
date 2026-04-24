package com.Gdz.bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AiService aiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "aiUrl", "http://localhost:8083/ai");
    }

    @Test
    void getAiStats_ShouldCallCorrectUrl() {
        Long telegramId = 987654321L;
        String expectedUrl = "http://localhost:8083/ai/api/ai/summaries/" + telegramId;

        String jsonResponse = """
                {
                    "status": "success",
                    "summary": "Тестовое описание"
                }
                """;

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);

        aiService.getAiStats(telegramId);

        verify(restTemplate).getForObject(expectedUrl, String.class);
    }

    @ParameterizedTest
    @CsvSource({
            "'success', 'Игрок активный геймер с 500+ часами в CS2', 'success', 'Игрок активный геймер с 500+ часами в CS2'",
            "'error', 'Не удалось создать описание', 'error', 'Не удалось создать описание'",
            "null, null, -, -"
    })
    void getAiStats_ParameterizedTest(String responseStatus, String responseSummary,
                                      String expectedStatus, String expectedSummary) {
        Long telegramId = 123456789L;
        String expectedUrl = "http://localhost:8083/ai/api/ai/summaries/" + telegramId;

        String jsonResponse;
        if ("null".equals(responseStatus)) {
            when(restTemplate.getForObject(expectedUrl, String.class))
                    .thenReturn(null);
            String result = aiService.getAiStats(telegramId);
            assertNull(result);
            verify(restTemplate, times(1)).getForObject(expectedUrl, String.class);
            return;
        } else {
            jsonResponse = String.format("""
                    {
                        "status": "%s",
                        "summary": "%s"
                    }
                    """, responseStatus, responseSummary);
            when(restTemplate.getForObject(expectedUrl, String.class))
                    .thenReturn(jsonResponse);
        }

        String result = aiService.getAiStats(telegramId);

        assertNotNull(result);
        assertTrue(result.contains(expectedStatus));
        assertTrue(result.contains(expectedSummary));

        verify(restTemplate, times(1)).getForObject(expectedUrl, String.class);
    }
}