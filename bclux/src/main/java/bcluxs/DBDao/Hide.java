package bcluxs.DBDao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@ToString
public class Hide {
    String serialNum;
    Integer animalType;
    Integer reserveType;
    HideProducer producer;
    Date transactionTime;
}
