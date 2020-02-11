package bcluxs.DBDao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeatherProducer {
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
