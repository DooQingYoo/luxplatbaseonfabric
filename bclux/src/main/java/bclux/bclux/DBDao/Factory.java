package bclux.bclux.DBDao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Factory {
    @Id
    Integer id;
    String name;
    String password;
    String address;
    String contact;
    String page;
    String legal;
    Boolean memb;
}
