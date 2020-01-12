package bclux.bclux.DBDao;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HideProducer {
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
