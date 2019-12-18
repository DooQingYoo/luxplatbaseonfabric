package bclux.bclux.DBDao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
@ToString
public class Leather {
    @Id
    @Column(nullable = false)
    String serialNum;
    Integer tanning;
    Integer layer;
    @ManyToOne
    @JoinColumn
    LeatherProducer producer;
    @Temporal(TemporalType.TIMESTAMP)
    Date transactionTime;
    @ManyToOne
    @JoinColumn
    Hide hide;
}
