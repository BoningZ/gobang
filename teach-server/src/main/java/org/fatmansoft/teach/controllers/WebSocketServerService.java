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
import org.fatmansoft.teach.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;


@EqualsAndHashCode
@Service
@ServerEndpoint("/room/{rid}")
@Slf4j
@Component
public class WebSocketServerService {

    private String rid;
    private Session session;
    private static CopyOnWriteArraySet<WebSocketServerService> webSocketSet = new CopyOnWriteArraySet<>();



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



    @OnOpen
    public void onOpen(Session session, @PathParam("rid") String rid) {
        var msg = new JSONObject();
        this.session = session;
        this.rid=rid;
        webSocketSet.add(this);
        Room room=webSocketServerService.roomRepository.getById(Integer.valueOf(rid));
        msg.put("type", 0);
        msg.put("chess",room.getChess());
        msg.put("isBlack",room.isBlack());
        msg.put("win",room.wins());
        msg.put("room",rid);
        broadcast(msg.toJSONString());
    }


    @OnClose
    public void onClose() throws IOException {
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
        if(room.getGuest()==null){
            bm.put("type",1);
            bm.put("msg","没有客人");
            broadcast(bm.toJSONString());
            return;
        }
        if(room.wins()>0){
            bm.put("type",1);
            bm.put("msg","本局已结束");
            broadcast(bm.toJSONString());
            return;
        }
        if((room.isBlack()&&room.getHost().getStudentId()==student.getStudentId())||
                (!room.isBlack()&&room.getGuest().getStudentId()==student.getStudentId())){
            int x=message.getX(),y=message.getY();
            if(room.setChess(x,y)>0){
                bm.put("type",1);
                bm.put("msg","此处已有棋子");
                broadcast(bm.toJSONString());
                return;
            }
            webSocketServerService.roomRepository.save(room);
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


    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误,用户：{}，发生错误", session.getId());
        error.printStackTrace();
    }


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
