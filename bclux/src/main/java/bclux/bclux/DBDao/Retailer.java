package bclux.bclux.DBDao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@ToString
@Entity
public class Retailer {
    @Id
    Integer id;
    String name;
    String address;
    String password;
}
