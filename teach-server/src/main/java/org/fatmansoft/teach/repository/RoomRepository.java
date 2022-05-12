package org.fatmansoft.teach.repository;

import org.fatmansoft.teach.models.Room;
import org.fatmansoft.teach.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    public List<Room> getRoomByHostOrGuest(Student student,Student student1);
    public List<Room> getRoomByHost(Student student);
}
