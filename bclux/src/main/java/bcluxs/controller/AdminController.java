package bcluxs.controller;

import bcluxs.DBDao.*;
import bcluxs.service.BCService;
import bcluxs.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.util.List;

@Controller
public class AdminController {
    private final DBService dbService;

    @Autowired
    public AdminController(DBService dbService, BCService bcService) {
        this.dbService = dbService;
    }

    @GetMapping("/allmembers")
    public String allMembers(ModelMap map) {
        List<HideProducer> allHide = dbService.getAllHide();
        List<LeatherProducer> allLeather = dbService.getAllLeather();
        List<Factory> allFactory = dbService.getAllFactory();
        List<Retailer> allRetailer = dbService.getAllRetailer();
        map.addAttribute("hides", allHide)
                .addAttribute("leathers", allLeather)
                .addAttribute("factories", allFactory)
                .addAttribute("retailers", allRetailer);
        return "members";
    }

    @GetMapping("/allhide")
    public String allhide(ModelMap map) {
        List<HideProducer> allHide = dbService.getAllHide();
        map.addAttribute("hides", allHide);
        return "members";
    }

    @GetMapping("/allleather")
    public String allleather(ModelMap map) {
        List<LeatherProducer> allLeather = dbService.getAllLeather();
        map.addAttribute("leathers", allLeather);
        return "members";
    }

    @GetMapping("/allfactory")
    public String allfactory(ModelMap map) {
        List<Factory> allFactory = dbService.getAllFactory();
        map.addAttribute("factories", allFactory);
        return "members";
    }

    @GetMapping("/allretailer")
    public String allretailer(ModelMap map) {
        List<Retailer> allRetailer = dbService.getAllRetailer();
        map.addAttribute("retailers", allRetailer);
        return "members";
    }

    @GetMapping("/hide/{id}")
    public String hide(@PathVariable("id") Integer id, ModelMap map) {
        HideProducer hideProducer = dbService.getHideProducer(id);
        map.addAttribute("member", hideProducer);
        map.addAttribute("type", "hide");
        return "modify";
    }

    @GetMapping("/leather/{id}")
    public String leather(@PathVariable("id") Integer id, ModelMap map) {
        LeatherProducer leatherProducer = dbService.getLeatherProducer(id);
        map.addAttribute("member", leatherProducer);
        map.addAttribute("type", "leather");
        return "modify";
    }

    @GetMapping("/factory/{id}")
    public String factory(@PathVariable("id") Integer id, ModelMap map) {
        Factory factory = dbService.getFactory(id);
        map.addAttribute("member", factory);
        map.addAttribute("type", "factory");
        return "modify";
    }

    @GetMapping("/retailer/{id}")
    public String retailer(@PathVariable("id") Integer id, ModelMap map) {
        Retailer retailer = dbService.getRetailer(id);
        map.addAttribute("member", retailer);
        map.addAttribute("type", "retailer");
        return "modify";
    }

    @GetMapping("/add/{type}")
    public String add(@PathVariable("type") String memberType, ModelMap map) {
        switch (memberType) {
            case "hide":
                HideProducer hideProducer = new HideProducer();
                hideProducer.setId(dbService.nextHideProducerId());
                hideProducer.setMemb(true);
                map.addAttribute("member", hideProducer);
                break;
            case "leather":
                LeatherProducer leatherProducer = new LeatherProducer();
                leatherProducer.setId(dbService.nextLeatherProducerId());
                leatherProducer.setMemb(true);
                map.addAttribute("member", leatherProducer);
                break;
            case "factory":
                Factory factory = new Factory();
                factory.setId(dbService.nextFactoryId());
                factory.setMemb(true);
                map.addAttribute("member", factory);
                break;
            case "retailer":
                Retailer retailer = new Retailer();
                retailer.setId(dbService.nextRetailerId());
                retailer.setMemb(true);
                map.addAttribute("member", retailer);
                break;
            default:
                map.addAttribute("h1", "增加失败");
                map.addAttribute("p1", "请按步骤进入增加页面");
                map.addAttribute("p2", "不要搞事情");
                return "fail";
        }
        map.addAttribute("type", memberType);
        return "modify";
    }

    @PostMapping("/modify/{type}")
    public String retailer(@PathVariable("type") String memeberType, @RequestParam("id") Integer id,
                           @RequestParam("name") String name, @RequestParam("address") String address,
                           @RequestParam("contact") String contact, @RequestParam("page") String page,
                           @RequestParam("legal") String legal, @RequestParam("memb") Boolean memb,
                           @RequestParam("password") String password, ModelMap map) {
        switch (memeberType) {
            case "hide":
                HideProducer hide = new HideProducer(id, name, password, address, contact, page, legal, memb);
                dbService.saveHideProducer(hide);
                break;
            case "leather":
                LeatherProducer leather = new LeatherProducer(id, name, password, address, contact, page, legal, memb);
                dbService.saveLeatherProducer(leather);
                break;
            case "factory":
                Factory factory = new Factory(id, name, password, address, contact, page, legal, memb);
                dbService.saveFactory(factory);
                break;
            case "retailer":
                Retailer retailer = new Retailer(id, name, password, address, contact, page, legal, memb);
                dbService.saveRetailer(retailer);
                break;
            default:
                map.addAttribute("h1", "修改失败");
                map.addAttribute("p1", "请按照步骤进入修改页面");
                map.addAttribute("p2", "不要搞事情");
                return "fail";
        }
        return "redirect:/all" + memeberType;
    }

    @GetMapping("/message/{id}")
    public String message(@PathVariable("id") int id, HttpSession session, ModelMap map) {
        Message message = dbService.getMessage(id);
        if (message == null) {
            map.addAttribute("h1", "不存在该条消息");
            map.addAttribute("p1", "请按正确步骤查看消息");
            map.addAttribute("p2", "不要搞事情");
            return "fail";
        }
        message.setNewMSG(false);
        dbService.saveMessage(message);
        int count = (int) session.getAttribute("message");
        session.setAttribute("message", --count);
        map.addAttribute("h1", message.getMessageType() == MessageType.MultiTimes ? "异常状况：多次查询" : message.getMessageType() == MessageType.VerifyNotPass ? "异常状况：校验错误" : "异常状况：数据丢失");
        map.addAttribute("p1", "查询时间：" + DateFormat.getInstance().format(message.getTime()));
        map.addAttribute("p2", "异常查询码：" + message.getSerialNum());
        return "fail";
    }
}
