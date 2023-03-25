package com.hps.backend.consumer;

import com.hps.backend.consumer.utils.JwtAuthentication;
import com.hps.backend.mapper.UserMapper;
import com.hps.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾  作用：将目前的类定义成一个websocket服务器端,
public class WebSocketServer {

    //一个静态变量线程安全map，用来存放每个客户端对应的WebSocket对象，都能看到，设计为线程安全的  <userId,websocket>
    private static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();

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
        if(this.user != null){
            users.put(userId, this);
        }else {
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

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        System.out.println("=======received message=======");
        System.out.println("message: " + message);
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