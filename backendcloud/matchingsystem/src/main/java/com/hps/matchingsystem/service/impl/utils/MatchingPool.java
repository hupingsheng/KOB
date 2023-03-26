package com.hps.matchingsystem.service.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MatchingPool extends Thread {

    //players多个线程共用
    private static List<Player> players = new ArrayList<>();

    private ReentrantLock lock = new ReentrantLock();

    private static RestTemplate restTemplate;

    private final static String startGameUrl = "http://127.0.0.1:3000/pk/start/game/";


    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        MatchingPool.restTemplate = restTemplate;
    }

    /**
     * 向匹配池中添加一名玩家,初始等待时间为0
     * @param userId
     * @param rating
     */
    public void addPlayer(Integer userId, Integer rating) {
        lock.lock();
        try {
            players.add(new Player(userId, rating, 0));
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从匹配池中删除一名玩家
     * @param userId
     */
    public void removePlayer(Integer userId) {
        lock.unlock();
        try {
            List<Player> newPlayers = new ArrayList<>();
            for (Player player : players) {
                if (!player.getUserId().equals(userId)) {
                    newPlayers.add(player);
                }
            }
            players = newPlayers;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将所有玩家的等待时间 + 1
     */
    public void increaseWaitingTime() {
        for (Player player : players) {
            player.setWaitingTime(player.getWaitingTime() + 1);
        }
    }


    /**
     * 采用某种策略判断两名玩家是否匹配
     * 策略：分差越小，等待时间越长，就越容易匹配到
     */
    public boolean checkMatching(Player a, Player b) {
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return ratingDelta <= waitingTime * 10;
    }

    /**
     * 从匹配系统 向 后端发送a和b的匹配结果
     * @param a
     * @param b
     */
    private void sendResult(Player a, Player b) {
        System.out.println("====匹配成功======");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("a_id", a.getUserId().toString());
        data.add("b_id", b.getUserId().toString());
        restTemplate.postForObject(startGameUrl, data, String.class);
    }

    /**
     * 尝试匹配所有玩家
     */
    private void matchPlayers() {
        System.out.println("match players: " + players.toString());
        boolean[] used = new boolean[players.size()];
        for (int i = 0; i < players.size(); i++) {
            if (used[i]) continue;
            for (int j = i + 1; j < players.size(); j++) {
                if (used[j]) continue;
                Player a = players.get(i), b = players.get(j);
                if (checkMatching(a, b)) {
                    used[i] = used[j] = true;
                    sendResult(a, b);
                    break;
                }
            }
        }
        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (!used[i]) {
                newPlayers.add(players.get(i));
            }
        }
        players = newPlayers;
    }


    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                lock.lock();
                try {
                    increaseWaitingTime();
                    matchPlayers();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }


}
