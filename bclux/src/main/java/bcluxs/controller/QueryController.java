package bcluxs.controller;

import bcluxs.BCDao.BCCommodity;
import bcluxs.DBDao.*;
import bcluxs.service.BCService;
import bcluxs.service.DBService;
import bcluxs.utils.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Date;

@Controller
public class QueryController {
    private final DBService dbService;
    private final BCService bcService;

    @Autowired
    public QueryController(DBService dbService, BCService bcService) {
        this.dbService = dbService;
        this.bcService = bcService;
    }

    @PostMapping("/query")
    public String query(@RequestParam("serial") String serialNum, ModelMap modelMap) {
        if (dbService.queryExist(serialNum)) {
            SoldCommodity soldCommodity = dbService.query(serialNum);
            if (soldCommodity.getQueryTime() >= 20) {
                // 发送消息
                Message message = new Message();
                message.setNewMSG(true);
                message.setMessageType(MessageType.MultiTimes);
                message.setSerialNum(serialNum);
                message.setTime(new Date());
                dbService.saveMessage(message);
            }
            if (!Tools.verifySerial(soldCommodity, serialNum)) {
                // 发送消息
                Message message = new Message();
                message.setNewMSG(true);
                message.setMessageType(MessageType.VerifyNotPass);
                message.setSerialNum(serialNum);
                message.setTime(new Date());
                dbService.saveMessage(message);
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

    @PostMapping("/search/{type}")
    public String commodity(@RequestParam("serialNum") String serialNum, @PathVariable("type") String type, ModelMap map, HttpSession session) {
        switch (type) {
            case "rawhide":
                Hide hide = null;
                if (dbService.hideExist(serialNum)) {
                    hide = dbService.queryHide(serialNum);
                } else {
                    hide = bcService.queryHide(serialNum);
                }
                if (hide == null) {
                    map.addAttribute("h1", "未找到该批次生皮");
                    map.addAttribute("p1", "请检查序列号是否输入正确");
                    map.addAttribute("p2", "或稍后尝试");
                    return "searchfail";
                }
                Object user = session.getAttribute("user");
                if (user instanceof HideProducer) {
                    HideProducer hp = (HideProducer) user;
                    if (!hp.getId().equals(hide.getProducer().getId())) {
                        return noPrivilege(map, "生皮");
                    }
                }
                map.addAttribute("hide", hide);
                map.addAttribute("serialNum", serialNum);
                return "hideResult";
            case "leather":
                return noPrivilege(map, "皮革");
            case "commodity":
                BCCommodity bcCommodity = bcService.queryBCCommodity(serialNum);
                if (bcCommodity == null) {
                    map.addAttribute("h1", "未找到该批次商品");
                    map.addAttribute("p1", "请检查序列号是否输入正确");
                    map.addAttribute("p2", "或稍后尝试");
                    return "searchfail";
                }
                Factory factory = dbService.getFactory(bcCommodity.getFactory());
                map.addAttribute("commodity", bcCommodity);
                map.addAttribute("serialNum", serialNum);
                map.addAttribute("factory", factory.getName());
                map.addAttribute("transactionTime", Tools.transTimestamp(bcCommodity.getTransactionTime()));
                return "commodityResult";
        }
        map.addAttribute("h1", "查询类型错误");
        map.addAttribute("p1", "请从正规入口查询");
        map.addAttribute("p2", "不要搞事情");
        return "searchfail";
    }

    @GetMapping("/code")
    public String code() {
        return "code";
    }

    public String noPrivilege(ModelMap map, String good) {
        map.addAttribute("h1", "查询失败");
        map.addAttribute("p1", "您没有查询该批次" + good + "的权限");
        map.addAttribute("p2", "请重新输入序列号");
        return "searchfail";
    }
}