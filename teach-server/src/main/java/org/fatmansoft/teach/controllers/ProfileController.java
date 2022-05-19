package org.fatmansoft.teach.controllers;

import org.fatmansoft.teach.models.EUserType;
import org.fatmansoft.teach.models.Student;
import org.fatmansoft.teach.models.User;
import org.fatmansoft.teach.payload.request.DataRequest;
import org.fatmansoft.teach.payload.response.DataResponse;
import org.fatmansoft.teach.repository.StudentRepository;
import org.fatmansoft.teach.repository.UserRepository;
import org.fatmansoft.teach.util.CommonMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/teach")
public class ProfileController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    StudentRepository studentRepository;

    @PostMapping("/getProfile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public DataResponse getProfile(@Valid @RequestBody DataRequest dataRequest){
        Integer userId= CommonMethod.getUserId();
        User user;
        Optional<User> tmp = userRepository.findByUserId(userId);
        user = tmp.get();
        Map m=new HashMap();
        Student student=user.getStudent();
        m.put("sid",student.getSid());
        m.put("name",student.getName());

        return CommonMethod.getReturnData(m);
    }

    @PostMapping("/submitProfile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public DataResponse submitProfile(@Valid @RequestBody DataRequest dataRequest){
        Integer userId= CommonMethod.getUserId();
        User user;
        Optional<User> tmp = userRepository.findByUserId(userId);
        user = tmp.get();

        Student student=user.getStudent();
        student.setSid(dataRequest.getString("sid"));
        student.setName(dataRequest.getString("name"));
        studentRepository.save(student);

        return CommonMethod.getReturnMessageOK();
    }

}

