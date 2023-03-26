package com.hps.botrunningsysytem.service;

public interface BotRunningService {
    //input 当前地图的输入信息
    String addBot(Integer userId, String botCode, String input);
}
