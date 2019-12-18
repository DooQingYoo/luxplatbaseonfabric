package bclux.bclux.DBDao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class HideProducer {
    @Id
    Integer id;
    String name;
    String password;
}
