package bcluxs.DBDao;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class SoldCommodity {
    @Id
    @Column(nullable = false)
    String serialNum;
    @ManyToOne
    @JoinColumn
    Retailer retailer;
    @Temporal(TemporalType.TIMESTAMP)
    Date transactionTime;
    @ManyToOne
    @JoinColumn
    Commodity commodity;
    Integer queryTime;
    Date LastQuery;
}
