package bcluxs.service;

import bcluxs.DBDao.Factory;
import bcluxs.DBDao.HideProducer;
import bcluxs.DBDao.LeatherProducer;
import bcluxs.DBDao.Retailer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserSecurityService implements UserDetailsService {

    @Autowired
    DBService dbService;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (username.startsWith("生皮加工厂")) {
            HideProducer hideProducer = dbService.getHideProducer(username);
            if (hideProducer == null)
                throw new UsernameNotFoundException("用户不存在");
            return new User(username, hideProducer.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("Hide")));
        }
        if (username.startsWith("皮革加工厂")) {
            LeatherProducer leatherProducer = dbService.getLeatherProducer(username);
            if (leatherProducer == null)
                throw new UsernameNotFoundException("用户不存在");
            return new User(username, leatherProducer.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("Leather")));
        }
        if (username.startsWith("皮包加工厂")) {
            Factory factory = dbService.getFactory(username);
            if (factory == null)
                throw new UsernameNotFoundException("用户不存在");
            return new User(username, factory.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("Factory")));

        }
        if (username.startsWith("零售商")) {
            Retailer retailer = dbService.getRetailer(username);
            if (retailer == null)
                throw new UsernameNotFoundException("用户不存在");
            return new User(username, retailer.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("Retailer")));

        }
        if (username.equals("管理员")) {
            return new User(username, encoder.encode("123456"), Collections.singletonList(new SimpleGrantedAuthority("Admin")));
        }
        throw new UsernameNotFoundException("用户不存在");
    }
}
