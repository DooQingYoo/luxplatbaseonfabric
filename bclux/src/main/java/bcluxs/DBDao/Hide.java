package bcluxs.DBDao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
@ToString
public class Hide {
    @Id
    @Column(nullable = false)
    String serialNum;
    Integer animalType;
    Integer reserveType;
    @ManyToOne
    @JoinColumn
    HideProducer producer;
    @Temporal(TemporalType.TIMESTAMP)
    Date transactionTime;
}
