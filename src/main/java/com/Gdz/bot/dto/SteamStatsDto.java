package com.Gdz.bot.dto;

import lombok.Data;

@Data
public class SteamStatsDto {

    private String nickname;
    private int gamesCount;
    private int hoursTotal;
    private String topGame;
}