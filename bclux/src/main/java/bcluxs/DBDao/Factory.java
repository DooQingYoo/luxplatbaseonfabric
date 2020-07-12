package bcluxs.DBDao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
