package bclux.bclux.DBRepository;

import bclux.bclux.DBDao.Commodity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommodityRepository extends JpaRepository<Commodity,String> {
}
