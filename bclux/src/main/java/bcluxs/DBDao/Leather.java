package bcluxs.DBDao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@ToString
public class Leather {
    String serialNum;
    Integer tanning;
    Integer layer;
    LeatherProducer producer;
    Date transactionTime;
    Hide hide;
}
