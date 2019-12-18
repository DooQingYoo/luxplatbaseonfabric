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
public class Commodity {
    @Id
    @Column(nullable = false)
    String serialNum;
    @ManyToOne
    @JoinColumn
    Factory factory;
    @Temporal(TemporalType.TIMESTAMP)
    Date transactionTime;
    @ManyToOne
    @JoinColumn
    Leather leather;
}
