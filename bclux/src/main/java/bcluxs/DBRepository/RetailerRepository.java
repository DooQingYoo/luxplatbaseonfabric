package bcluxs.DBRepository;

import bcluxs.DBDao.Retailer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerRepository extends JpaRepository<Retailer,Integer> {
    Retailer getByName(String name);
}
