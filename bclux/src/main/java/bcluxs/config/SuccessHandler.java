package bcluxs.config;

import bcluxs.DBDao.Factory;
import bcluxs.DBDao.HideProducer;
import bcluxs.DBDao.LeatherProducer;
import bcluxs.DBDao.Retailer;
import bcluxs.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    DBService dbService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        String au = authentication.getAuthorities().iterator().next().getAuthority();
        HttpSession session = httpServletRequest.getSession();
        switch (au) {
            case "Hide":
                HideProducer hideProducer = dbService.getHideProducer(authentication.getName());
                session.setAttribute("user", hideProducer);
                break;
            case "Leather":
                LeatherProducer leatherProducer = dbService.getLeatherProducer(authentication.getName());
                session.setAttribute("user", leatherProducer);
                break;
            case "Factory":
                Factory factory = dbService.getFactory(authentication.getName());
                session.setAttribute("user", factory);
                break;
            case "Retailer":
                Retailer retailer = dbService.getRetailer(authentication.getName());
                session.setAttribute("user", retailer);
                break;
            case "Admin":
                session.setAttribute("user", "Admin");
        }
        httpServletResponse.sendRedirect("/main");
    }
}