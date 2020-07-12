package bcluxs.controller;

import bcluxs.BCDao.BCCommodity;
import bcluxs.DBDao.*;
import bcluxs.service.BCService;
import bcluxs.service.DBService;
import bcluxs.utils.Tools;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
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

        SoldCommodity sold = bcService.querySold(serialNum);
        if (sold == null) {
            modelMap.addAttribute("h1", "未查询到相关产品");
            modelMap.addAttribute("p1", "该页表示未您的产品序列号输入有误");
            modelMap.addAttribute("p2", "或者您购买的产品不是正品");
            return "fail";
        }
        modelMap.addAttribute("soldCommodity", sold);
        if (sold.getQueryTime() >= 20) {
            // 发送消息
            Message message = new Message();
            message.setNewMSG(true);
            message.setMessageType(MessageType.MultiTimes);
            message.setSerialNum(serialNum);
            message.setTime(new Date());
            dbService.saveMessage(message);
        }
        if (!Tools.verifySerial(sold, serialNum)) {
            // 发送消息
            Message message = new Message();
            message.setNewMSG(true);
            message.setMessageType(MessageType.VerifyNotPass);
            message.setSerialNum(serialNum);
            message.setTime(new Date());
            dbService.saveMessage(message);
        }
        return "result";
    }

    @PostMapping("/search/{type}")
    public String commodity(@RequestParam("serialNum") String serialNum, @PathVariable("type") String type, ModelMap map, HttpSession session) {
        Object user = session.getAttribute("user");
        switch (type) {
            case "rawhide":
                Hide hide = bcService.queryHide(serialNum);
                if (hide == null) {
                    map.addAttribute("h1", "未找到该批次生皮");
                    map.addAttribute("p1", "请检查序列号是否输入正确");
                    map.addAttribute("p2", "或稍后尝试");
                    return "searchfail";
                }
                if (user instanceof HideProducer) {
                    HideProducer hp = (HideProducer) user;
                    if (!hp.getId().equals(hide.getProducer().getId())) {
                        return noPrivilege(map, "生皮", hide.getProducer().getName());
                    }
                }
                map.addAttribute("hide", hide);
                return "hideResult";
            case "leather":
                Leather leather = bcService.queryLeather(serialNum);
                if (leather == null) {
                    map.addAttribute("h1", "未找到该批次皮革");
                    map.addAttribute("p1", "请检查序列号是否输入正确");
                    map.addAttribute("p2", "或稍后尝试");
                    return "searchfail";
                }
                if (user instanceof LeatherProducer) {
                    LeatherProducer lp = (LeatherProducer) user;
                    if (!lp.getId().equals(leather.getProducer().getId()))
                        return noPrivilege(map, "皮革", leather.getProducer().getName());
                }
                map.addAttribute("leather", leather);
                return "leatherResult";
            case "commodity":
                BCCommodity bcCommodity = bcService.queryBCCommodity(serialNum);
                if (bcCommodity == null) {
                    map.addAttribute("h1", "未找到该批次商品");
                    map.addAttribute("p1", "请检查序列号是否输入正确");
                    map.addAttribute("p2", "或稍后尝试");
                    return "searchfail";
                }
                Factory factory = dbService.getFactory(bcCommodity.getFactory());
                if (user instanceof Factory) {
                    Factory f = (Factory) user;
                    if (!f.getId().equals(factory.getId()))
                        return noPrivilege(map, "皮包", factory.getName());
                }
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

    public String noPrivilege(ModelMap map, String good, String owner) {
        map.addAttribute("h1", "查询失败");
        map.addAttribute("p1", "您没有查询该批次" + good + "的权限");
        map.addAttribute("p2", "该批货物属于" + owner);

        return "searchfail";
    }
}