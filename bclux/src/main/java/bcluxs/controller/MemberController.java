package bcluxs.controller;

import bcluxs.DBDao.*;
import bcluxs.service.BCService;
import bcluxs.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping("/query")
    public String query() {
        return "query";
    }

    @GetMapping("/details")
    public String details() {
        return "details";
    }

    @GetMapping("/userlogin")
    public String login() {
        return "login";
    }

    @GetMapping("/talk")
    public String talk() {
        return "talk";
    }

    @GetMapping("/main")
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
        map.addAttribute("substance", "商品防伪查询码");
        return "soldResult";
    }

    @PostMapping("/hide")
    public String hide(@RequestParam("hideProducer") String hideProducer, @RequestParam("animalType") String animalType, @RequestParam("reserveType") String reserveType, ModelMap map) {
        String serialNum = bcService.produceHide(animalType, reserveType, hideProducer);
        if (serialNum == null) {
            return produceFail(map);
        }
        map.addAttribute("serialNum", serialNum);
        map.addAttribute("substance", "生皮序列号");
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
        map.addAttribute("substance", "皮革序列号");
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
        map.addAttribute("substance", "商品序列号");
        return "soldResult";
    }

    @GetMapping("/search")
    public String search() {
        return "search";
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
