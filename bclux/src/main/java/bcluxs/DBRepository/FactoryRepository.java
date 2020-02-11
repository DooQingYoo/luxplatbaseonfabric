package bcluxs.DBRepository;

import bcluxs.DBDao.Factory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactoryRepository extends JpaRepository<Factory,Integer> {
    Factory getByName(String name);
}
