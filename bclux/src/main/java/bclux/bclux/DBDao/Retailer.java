package bclux.bclux.DBDao;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Retailer {
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
