package bclux.bclux.DBRepository;

import bclux.bclux.DBDao.LeatherProducer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeatherProducerRepository extends JpaRepository<LeatherProducer,Integer> {
    LeatherProducer getByName(String name);
}
