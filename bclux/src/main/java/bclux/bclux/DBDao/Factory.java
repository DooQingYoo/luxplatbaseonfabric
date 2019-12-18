package bclux.bclux.DBDao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Setter
@Getter
@Entity
public class Factory {
    @Id
    Integer id;
    String name;
    String password;
}
