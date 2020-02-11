package bcluxs.DBRepository;

import bcluxs.DBDao.Commodity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommodityRepository extends JpaRepository<Commodity,String> {
}
