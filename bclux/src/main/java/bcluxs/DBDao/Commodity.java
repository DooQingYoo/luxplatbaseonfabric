package bcluxs.DBDao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@ToString
public class Commodity {
    String serialNum;
    Factory factory;
    Date transactionTime;
    Leather leather;
}
