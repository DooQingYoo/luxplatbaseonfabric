package bclux.bclux.DBRepository;

import bclux.bclux.DBDao.HideProducer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HideProducerRepository extends JpaRepository<HideProducer,Integer> {
    HideProducer getByName(String name);
}
