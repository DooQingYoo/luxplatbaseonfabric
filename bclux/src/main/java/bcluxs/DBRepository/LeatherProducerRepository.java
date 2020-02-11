package bcluxs.DBRepository;

import bcluxs.DBDao.LeatherProducer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeatherProducerRepository extends JpaRepository<LeatherProducer,Integer> {
    LeatherProducer getByName(String name);
}
