package com.hps.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.hps.backend.consumer.utils.Game;
import com.hps.backend.consumer.utils.JwtAuthentication;
import com.hps.backend.mapper.UserMapper;
import com.hps.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾  作用：将目前的类定义成一个websocket服务器端,类似于controller
public class WebSocketServer {

    //一个静态变量线程安全map，用来存放每个客户端对应的WebSocket对象，都能看到，设计为线程安全的  <userId,websocket>
    final private static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();

    //匹配池
    final private static CopyOnWriteArraySet<User> matchpool = new CopyOnWriteArraySet<>();

    private User user;

    //后端向前端发送消息，需要用到websocket的session
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session = null;

    private static UserMapper userMapper;


    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
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
            matchpool.remove(this.user);

        }
    }

    private void startMatching() {
        System.out.println("start matching!");
        matchpool.add(this.user);

        while (matchpool.size() >= 2){
            Iterator<User> it = matchpool.iterator();
            User a = it.next(), b = it.next();
            matchpool.remove(a);
            matchpool.remove(b);

            //初始化地图
            Game game = new Game(13, 14, 20);
            game.createMap();

            //a发送消息回去，包含对手b的信息
            JSONObject respA = new JSONObject();
            respA.put("event","start-matching");
            respA.put("opponent_username",b.getUsername());
            respA.put("opponent_photo",b.getPhoto());
            respA.put("gamemap",game.getG());
            users.get(a.getId()).sendMessage(respA.toJSONString());

            //b发送消息给客户端，包含对手a的信息
            JSONObject respB = new JSONObject();
            respB.put("event","start-matching");
            respB.put("opponent_username",a.getUsername());
            respB.put("opponent_photo",a.getPhoto());
            respB.put("gamemap",game.getG());
            users.get(b.getId()).sendMessage(respB.toJSONString());


        }
    }

    private void stopMatching() {
        System.out.println("stop matching!");
        matchpool.remove(this.user);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        System.out.println("=======received message=======");
        JSONObject data = JSONObject.parseObject(message);
        //前端发起websocket连接有两种情形：开始匹配、取消匹配  用event字段来区分
        String event = data.getString("event");
        if ("start-matching".equals(event)) {
            startMatching();
        } else if ("stop-matching".equals(event)) {
            stopMatching();
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