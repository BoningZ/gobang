package org.fatmansoft.teach.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.fatmansoft.teach.models.Room;
import org.fatmansoft.teach.models.Student;
import org.fatmansoft.teach.repository.RoomRepository;
import org.fatmansoft.teach.repository.StudentRepository;
import org.fatmansoft.teach.repository.UserRepository;
import org.fatmansoft.teach.util.BeanUtils;
import org.fatmansoft.teach.util.CommonMethod;
import org.fatmansoft.teach.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * webSocketServer服务
 * <p>
 * //TODO
 * WebSocketServerService.java
 * </p>
 *
 * @author 佐斯特勒
 * @version v1.0.0
 * @date 2020/6/29 21:03
 * @see WebSocketServerService
 **/
@EqualsAndHashCode
@Service
@ServerEndpoint("/room/{rid}")
@Slf4j
@Component
public class WebSocketServerService {

    private String rid;
    private Session session;
    private static CopyOnWriteArraySet<WebSocketServerService> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     * 用来记录用户名和该session进行绑定
     */
    private static Map<String, Session> map = new ConcurrentHashMap<>();

    /**
     * 记录用户 session_id:nickName
     */
    private static Map<String,String> peoples = new ConcurrentHashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RoomRepository roomRepository;

    private static WebSocketServerService webSocketServerService;

    @PostConstruct  //关键点3
    public void init(){
        webSocketServerService = this;
    }


    /**
     * 建立会话
     *
     * @param session  会话
     * @param rid 房间号
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("rid") String rid) {
        var msg = new JSONObject();
        this.session = session;
        this.rid=rid;
        peoples.put(session.getId(), rid);
        map.put(session.getId(), session);
        webSocketSet.add(this);
        Room room=webSocketServerService.roomRepository.getById(Integer.valueOf(rid));
        msg.put("type", 0);
        msg.put("chess",room.getChess());
        msg.put("isBlack",room.isBlack());
        msg.put("win",room.wins());
        msg.put("room",rid);
        broadcast(msg.toJSONString());
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose() throws IOException {
        peoples.remove(this.session.getId());
        var msg = new JSONObject();
        msg.put("type", 1);
        msg.put("room",rid);
        msg.put("msg","对方已离开");
        broadcast(msg.toJSONString());
        webSocketSet.remove(this);
    }

    @OnMessage
    public void OnMessage(Session session, @PathParam("rid") String rid, String msg) {
        Room room=webSocketServerService.roomRepository.getById(Integer.valueOf(rid));
        Message message;
        message = JSON.parseObject(msg, Message.class);
        Integer id= message.getId();
        Student student=webSocketServerService.userRepository.findByUserId(id).get().getStudent();
        var bm = new JSONObject();
        bm.put("room",rid);
        System.out.println((room.getHost().equals(student)));
        if((room.isBlack()&&room.getHost().equals(student))||(!room.isBlack()&&room.getGuest().equals(student))){
            if(room.wins()>0){
                bm.put("type",1);
                bm.put("msg","本局已结束");
                broadcast(bm.toJSONString());
                return;
            }
            int x=message.getX(),y=message.getY();
            room.setChess(x,y);webSocketServerService.roomRepository.save(room);
            bm.put("type",0);
            bm.put("chess",room.getChess());
            bm.put("isBlack",room.isBlack());
            bm.put("win",room.wins());
            broadcast(bm.toJSONString());
            return;
        }
        bm.put("type", 1);
        bm.put("msg", "请等待对方落子");
        broadcast(bm.toJSONString());
    }

    /**
     * 发生错误时调用   
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误,用户：{}，发生错误", session.getId());
        error.printStackTrace();
    }

    /**
     * 群发自定义消息
     */
    public void broadcast(String message) {
        webSocketSet.forEach(item -> {
            try {
                if (item.session.isOpen()){
                    item.session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
