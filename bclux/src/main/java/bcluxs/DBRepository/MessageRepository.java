package bcluxs.DBRepository;

import bcluxs.DBDao.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> getAllByNewMSGTrue();

    int countAllByNewMSGTrue();
}
