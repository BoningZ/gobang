package org.fatmansoft.teach.controllers;

import org.fatmansoft.teach.models.Room;
import org.fatmansoft.teach.models.Student;
import org.fatmansoft.teach.payload.request.DataRequest;
import org.fatmansoft.teach.payload.response.DataResponse;
import org.fatmansoft.teach.repository.RoomRepository;
import org.fatmansoft.teach.repository.StudentRepository;
import org.fatmansoft.teach.repository.UserRepository;
import org.fatmansoft.teach.util.CommonMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/teach")
public class HomeController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    RoomRepository roomRepository;

    @PostMapping("/getRoomList")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public DataResponse getRoomList(@Valid @RequestBody DataRequest dataRequest){
        Boolean my=dataRequest.getBoolean("my");
        String rid=dataRequest.getString("rid");
        String hostId=dataRequest.getString("hostId");
        List dataList=new ArrayList();
        if(my!=null&&my){
            Integer id= CommonMethod.getUserId();
            Student student=userRepository.findByUserId(id).get().getStudent();
            List<Room> rList=roomRepository.getRoomByHostOrGuest(student,student);
            for(Room r:rList)dataList.add(CommonMethod.objectToMap(r));
            return CommonMethod.getReturnData(dataList);
        }
        if(rid!=null&&!rid.equals("")){
            Optional<Room> r=roomRepository.findById(Integer.valueOf(rid));
            r.ifPresent(room -> dataList.add(CommonMethod.objectToMap(room)));
            return CommonMethod.getReturnData(dataList);
        }
        if(hostId!=null&&!hostId.equals("")){
            Optional<Student> s=studentRepository.findStudentBySid(hostId);
            List<Room> rList=new ArrayList<>();
            s.ifPresent(student -> rList.addAll(roomRepository.getRoomByHost(student)));
            for(Room r:rList)dataList.add(CommonMethod.objectToMap(r));
            return CommonMethod.getReturnData(dataList);
        }
        List<Room> rList=roomRepository.findAll();
        for(Room r:rList)dataList.add(CommonMethod.objectToMap(r));
        return CommonMethod.getReturnData(dataList);
    }

    @PostMapping("/addRoom")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public DataResponse addRoom(@Valid @RequestBody DataRequest dataRequest){
        Integer id= CommonMethod.getUserId();
        Student student=userRepository.findByUserId(id).get().getStudent();
        Room r=new Room();
        r.setHost(student);
        roomRepository.save(r);
        return CommonMethod.getReturnMessageOK();
    }

    @PostMapping("/joinRoom")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public DataResponse joinRoom(@Valid @RequestBody DataRequest dataRequest){
        Integer id= CommonMethod.getUserId();
        Student student=userRepository.findByUserId(id).get().getStudent();
        String rid=dataRequest.getString("rid");
        Room r=roomRepository.getById(Integer.valueOf(rid));
        Map m=new HashMap<>();
        if(r.getHost().equals(student)){m.put("isHost",true);return CommonMethod.getReturnData(m);}
        m.put("isHost",false);
        if(r.getGuest()==null){r.setGuest(student);roomRepository.save(r);return CommonMethod.getReturnData(m);}
        if(!r.getGuest().equals(student))return CommonMethod.getReturnMessageError("");
        return CommonMethod.getReturnData(m);
    }

}
