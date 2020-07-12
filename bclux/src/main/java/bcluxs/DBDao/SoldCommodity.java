package bcluxs.DBDao;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SoldCommodity {
    String serialNum;
    Retailer retailer;
    Date transactionTime;
    Commodity commodity;
    Integer queryTime;
    Date lastQuery;
}
