package bclux.bclux.controller;

import bclux.bclux.BCDao.BCCommodity;
import bclux.bclux.DBDao.*;
import bclux.bclux.service.BCService;
import bclux.bclux.service.DBService;
import bclux.bclux.utils.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Date;

@Controller
public class RealController {

    private final DBService dbService;
    private final BCService bcService;

    @Autowired
    public RealController(DBService dbService, BCService bcService) {
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
    public String checkLogin(@RequestParam("login_name") String name, @RequestParam("password") String password, HttpSession session, ModelMap map) {
        if (name.startsWith("生皮加工厂")) {
            HideProducer hideProducer = dbService.getHideProducer(name);
            if (hideProducer == null || !hideProducer.getPassword().equals(password)) {
                return loginFail(map);
            }
            session.setAttribute("user", hideProducer);
            return "hide";
        }
        if (name.startsWith("皮革加工厂")) {
            LeatherProducer leatherProducer = dbService.getLeatherProducer(name);
            if (leatherProducer == null || !leatherProducer.getPassword().equals(password)) {
                return loginFail(map);
            }
            session.setAttribute("user", leatherProducer);
            return "leather";
        }
        if (name.startsWith("工厂")) {
            Factory factory = dbService.getFactory(name);
            if (factory == null || !factory.getPassword().equals(password)) {
                return loginFail(map);
            }
            session.setAttribute("user", factory);
            return "factory";
        }
        if (name.startsWith("专卖店")) {
            Retailer retailer = dbService.getRetailer(name);
            if (retailer == null || !retailer.getPassword().equals(password)) {
                return loginFail(map);
            }
            session.setAttribute("user", retailer);
            return "retailer";
        }

        return loginFail(map);
    }

    @PostMapping("/query")
    public String query(@RequestParam("serial") String serialNum, ModelMap modelMap) {
        if (dbService.queryExist(serialNum)) {
            SoldCommodity soldCommodity = dbService.query(serialNum);
            if (!Tools.verifySerial(soldCommodity, serialNum)) {
                return searchBC(serialNum, modelMap);
            }
            SoldCommodity newsold = new SoldCommodity(soldCommodity.getSerialNum(), soldCommodity.getRetailer(),
                    soldCommodity.getTransactionTime(), soldCommodity.getCommodity(),
                    soldCommodity.getQueryTime(), soldCommodity.getLastQuery());
            soldCommodity.setQueryTime(soldCommodity.getQueryTime() + 1);
            soldCommodity.setLastQuery(new Date());
            dbService.save(soldCommodity);
            modelMap.addAttribute("soldCommodity", newsold);
            return "result";
        } else {
            return searchBC(serialNum, modelMap);
        }
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

    @PostMapping("/commodity")
    public String commodity(@RequestParam("serialNum") String serialNum, ModelMap map) {
        BCCommodity bcCommodity = bcService.queryBCCommodity(serialNum);
        if (bcCommodity == null) {
            map.addAttribute("h1", "未找到该批次商品");
            map.addAttribute("p1", "请检查序列号是否输入正确");
            map.addAttribute("p2", "或稍后尝试");
            return "fail";
        }
        Factory factory = dbService.getFactory(bcCommodity.getFactory());
        map.addAttribute("commodity", bcCommodity);
        map.addAttribute("serialNum", serialNum);
        map.addAttribute("factory", factory.getName());
        map.addAttribute("transactionTime", Tools.transTimestamp(bcCommodity.getTransactionTime()));
        return "commodityResult";
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
        String serialNum = bcService.produceLeather(tanning, layer, producer, hideSerial);
        if (serialNum == null) {
            return produceFail(map);
        }
        map.addAttribute("serialNum", serialNum);
        map.addAttribute("substance", "皮革");
        return "soldResult";
    }

    @PostMapping("/facotry")
    public String factory(@RequestParam("factory") String factory, @RequestParam("totalCount") String totalCount, @RequestParam("leatherNum") String leatherNum, ModelMap map) {
        String serianNum = bcService.produceCommodity(factory, totalCount, leatherNum);
        if (serianNum == null) {
            return produceFail(map);
        }
        map.addAttribute("serialNum", serianNum);
        map.addAttribute("substance", "商品");
        return "soldResult";
    }

    private String searchBC(String serialNum, ModelMap modelMap) {
        SoldCommodity sold = bcService.querySold(serialNum);
        if (sold != null) {
            modelMap.addAttribute("soldCommodity", sold);
            return "result";
        }
        modelMap.addAttribute("h1", "未查询到相关产品");
        modelMap.addAttribute("p1", "该页表示未您的产品序列号输入有误");
        modelMap.addAttribute("p2", "或者您购买的产品不是正品");
        return "fail";
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
