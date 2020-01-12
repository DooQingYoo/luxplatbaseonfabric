package bclux.bclux.DBRepository;

import bclux.bclux.DBDao.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> getAllByNewMSGTrue();

    int countAllByNewMSGTrue();
}
