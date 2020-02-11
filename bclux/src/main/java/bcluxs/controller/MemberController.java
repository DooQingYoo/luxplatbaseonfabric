package bcluxs.controller;

import bclux.bclux.DBDao.*;
import bcluxs.service.BCService;
import bcluxs.service.DBService;
import bcluxs.DBDao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class MemberController {

    private final DBService dbService;
    private final BCService bcService;

    @Autowired
    public MemberController(DBService dbService, BCService bcService) {
        this.dbService = dbService;
        this.bcService = bcService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String checkLogin(@RequestParam("login_name") String name, @RequestParam("password") String password, HttpSession session, ModelMap map, HttpServletRequest request) {
        if (name.startsWith("生皮加工厂")) {
            HideProducer hideProducer = dbService.getHideProducer(name);
            if (hideProducer == null || !hideProducer.getPassword().equals(password)) {
                return loginFail(map);
            }
            session.setAttribute("user", hideProducer);
            return "redirect:/main";
        }
        if (name.startsWith("皮革加工厂")) {
            LeatherProducer leatherProducer = dbService.getLeatherProducer(name);
            if (leatherProducer == null || !leatherProducer.getPassword().equals(password)) {
                return loginFail(map);
            }
            session.setAttribute("user", leatherProducer);
            return "redirect:/main";
        }
        if (name.startsWith("工厂")) {
            Factory factory = dbService.getFactory(name);
            if (factory == null || !factory.getPassword().equals(password)) {
                return loginFail(map);
            }
            session.setAttribute("user", factory);
            return "redirect:/main";
        }
        if (name.startsWith("专卖店")) {
            Retailer retailer = dbService.getRetailer(name);
            if (retailer == null || !retailer.getPassword().equals(password)) {
                return loginFail(map);
            }
            session.setAttribute("user", retailer);
            return "redirect:/main";
        }
        if (name.equals("管理员")) {
            if (!password.equals("123456")) {
                return loginFail(map);
            }
            session.setAttribute("user", "Admin");
            return "redirect:/main";
        }

        return loginFail(map);
    }

    @RequestMapping("/main")
    public String mainPage(HttpSession session, ModelMap map) {
        Object user = session.getAttribute("user");
        if ("Admin".equals(user)) {
            session.setAttribute("message", dbService.getMessageCount());
            map.addAttribute("messages", dbService.getMessages());
            return "admin";
        }
        if (user instanceof Retailer) {
            return "retailer";
        }
        if (user instanceof Factory) {
            return "factory";
        }
        if (user instanceof LeatherProducer) {
            return "leather";
        }
        if (user instanceof HideProducer) {
            return "hide";
        }
        return loginFail(map);
    }

    @PostMapping("/retail")
    public String retail(@RequestParam("retailer") String id, @RequestParam("serialNum") String commoditySerialNum, ModelMap map) {
        String serialNum = bcService.sell(id, commoditySerialNum);
        if (serialNum == null) {
            map.addAttribute("h1", "售出失败");
            map.addAttribute("p1", "请稍后尝试");
            map.addAttribute("p2", "或联系管理员");
            return "fail";
        }
        map.addAttribute("serialNum", serialNum);
        map.addAttribute("substance", "商品");
        return "soldResult";
    }

    @PostMapping("/hide")
    public String hide(@RequestParam("hideProducer") String hideProducer, @RequestParam("animalType") String animalType, @RequestParam("reserveType") String reserveType, ModelMap map) {
        String serialNum = bcService.produceHide(animalType, reserveType, hideProducer);
        if (serialNum == null) {
            return produceFail(map);
        }
        map.addAttribute("serialNum", serialNum);
        map.addAttribute("substance", "生皮");
        return "soldResult";
    }

    @PostMapping("/leather")
    public String leather(@RequestParam("hideProducer") String producer, @RequestParam("tanning") String tanning, @RequestParam("layer") String layer, @RequestParam("hideSerial") String hideSerial, ModelMap map) {
        Hide hide = bcService.queryHide(hideSerial);
        if (hide == null) {
            map.addAttribute("h1", "录入失败");
            map.addAttribute("p1", "未找到序列号为" + hideSerial + "的生皮");
            map.addAttribute("p2", "请重新输入");
        }
        String serialNum = bcService.produceLeather(tanning, layer, producer, hideSerial);
        if (serialNum == null) {
            return produceFail(map);
        }
        map.addAttribute("serialNum", serialNum);
        map.addAttribute("substance", "皮革");
        return "soldResult";
    }

    @PostMapping("/factory")
    public String factory(@RequestParam("factory") String factory, @RequestParam("totalCount") String totalCount, @RequestParam("leatherNum") String leatherNum, ModelMap map) {
        Leather leather = bcService.queryLeather(leatherNum);
        if (leather == null) {
            map.addAttribute("h1", "录入失败");
            map.addAttribute("p1", "未找到序列号为" + leatherNum + "的皮革");
            map.addAttribute("p2", "请重新输入");
            return "fail";
        }
        String serianNum = bcService.produceCommodity(factory, totalCount, leatherNum);
        if (serianNum == null) {
            return produceFail(map);
        }
        map.addAttribute("serialNum", serianNum);
        map.addAttribute("substance", "商品");
        return "soldResult";
    }

    private String loginFail(ModelMap map) {
        map.addAttribute("h1", "登录失败");
        map.addAttribute("p1", "用户名或密码错误");
        map.addAttribute("p2", "请重新登录");
        return "fail";
    }

    private String produceFail(ModelMap map) {
        map.addAttribute("h1", "数据录入失败");
        map.addAttribute("p1", "稍后尝试");
        map.addAttribute("p2", "或联系管理员");
        return "fail";
    }
}
