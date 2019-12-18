package bclux.bclux.DBDao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class LeatherProducer {
    @Id
    Integer id;
    String name;
    String password;
}
