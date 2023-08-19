package com.hps.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.hps.backend.consumer.utils.Game;
import com.hps.backend.consumer.utils.JwtAuthentication;
import com.hps.backend.mapper.BotMapper;
import com.hps.backend.mapper.RecordMapper;
import com.hps.backend.mapper.UserMapper;
import com.hps.backend.pojo.Bot;
import com.hps.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾  作用：将目前的类定义成一个websocket服务器端,类似于controller
public class WebSocketServer {

    //一个静态变量线程安全map，用来存放每个客户端对应的WebSocket对象，都能看到，设计为线程安全的  <userId,websocket>
    //websocket连接的在线用户
    final public static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();

    private User user;

    //后端向前端发送消息，需要用到websocket的session
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session = null;

    public static UserMapper userMapper;

    public static RecordMapper recordMapper;

    public static RestTemplate restTemplate;

    public Game game = null;

    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";

    private static BotMapper botMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper){
        WebSocketServer.botMapper = botMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session = session;
        System.out.println("=====connected======");
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);
        //用户存在，则加入连接管理，否则连接关闭
        if (this.user != null) {
            users.put(userId, this);
        } else {
            this.session.close();
        }
        System.out.println(users);
    }

    @OnClose
    public void onClose() {
        // 关闭连接
        System.out.println("=====closed======");
        if (this.user != null) {
            users.remove(this.user.getId());
        }
    }

    /**
     * 收集用户a b的初始化信息和共同的地图信息
     * @param aId
     * @param bId
     */
    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId) {
        User a = userMapper.selectById(aId);
        User b = userMapper.selectById(bId);

        Bot botA = botMapper.selectById(aBotId), botB = botMapper.selectById(bBotId);

        //初始化地图
        Game game = new Game(
                13,
                14,
                20,
                a.getId(),
                botA,
                b.getId(),
                botB
        );
        game.createMap();

        if (users.get(a.getId()) != null){
            users.get(a.getId()).game = game;
        }
        if(users.get(b.getId()) != null){
            users.get(b.getId()).game = game;
        }

        game.start();

        //两名玩家的共同维护的地图信息
        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());

        //a发送消息回去，包含对手b的信息和共同维护的地图信息
        JSONObject respA = new JSONObject();
        respA.put("event", "start-matching");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game", respGame);
        if(users.get(a.getId()) != null){
            users.get(a.getId()).sendMessage(respA.toJSONString());
        }


        //b发送消息给客户端，包含对手a的信息和共同维护的地图信息
        JSONObject respB = new JSONObject();
        respB.put("event", "start-matching");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        if(users.get(b.getId()) != null){
            users.get(b.getId()).sendMessage(respB.toJSONString());
        }
    }

    /**
     *
     */
    private void startMatching(Integer botId) {
        System.out.println("start matching!");

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();

        data.add("user_id", this.user.getId().toString());
        data.add("rating", this.user.getRating().toString());
        data.add("bot_id", botId.toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class);
    }

    private void stopMatching() {
        System.out.println("stop matching!");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();

        data.add("user_id", this.user.getId().toString());

        restTemplate.postForObject(removePlayerUrl, data, String.class);
    }

    /**
     * set是已经加锁的
     *
     * @param direction
     */
    public void move(int direction) {
        if (game.getPlayerA().getId().equals(user.getId())) {
            if(game.getPlayerA().getBotId().equals(-1))     //亲自出马
                game.setNextStepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            if(game.getPlayerB().getBotId().equals(-1))
                game.setNextStepB(direction);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        System.out.println("=======received message=======");
        JSONObject data = JSONObject.parseObject(message);
        //前端发起websocket连接有两种情形：开始匹配、取消匹配  用event字段来区分
        String event = data.getString("event");
        if ("start-matching".equals(event)) {
            startMatching(data.getInteger("bot_id"));
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        } else if ("move".equals(event)) {
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    /**
     * 自定义一个后端发送给前端消息的方法
     *
     * @param message
     */
    public void sendMessage(String message) {
        //TODO 为什么session加锁
        synchronized (this.session) {
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}