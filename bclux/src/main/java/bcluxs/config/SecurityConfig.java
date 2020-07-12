package bcluxs.config;

import bcluxs.service.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserSecurityService userSecurityService;
    @Autowired
    SuccessHandler handler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable();
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/hide").hasAuthority("Hide")
                .antMatchers(HttpMethod.POST, "/leather").hasAuthority("Leather")
                .antMatchers(HttpMethod.POST, "/factory").hasAuthority("Factory")
                .antMatchers(HttpMethod.POST, "/retail").hasAuthority("Retailer")
                .antMatchers(HttpMethod.POST, "/search/**").hasAnyAuthority("Hide", "Leather", "Factory", "Retailer")
                .antMatchers(HttpMethod.POST, "modify/**").hasAuthority("Admin")
                .antMatchers(HttpMethod.POST, "/raw").hasAuthority("Admin")
                .and().formLogin().successHandler(handler)
                .loginPage("/userlogin").loginProcessingUrl("/login")
                .and().rememberMe().alwaysRemember(true);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userSecurityService).passwordEncoder(encoder());
//        auth.inMemoryAuthentication().passwordEncoder(encoder()).withUser("管理员").password(encoder().encode("123456")).roles("Admin");
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
