package bcluxs.service;

import bcluxs.BCDao.BCCommodity;
import bcluxs.BCDao.BCHide;
import bcluxs.BCDao.BCLeather;
import bcluxs.BCDao.BCSoldCommodity;
import bcluxs.BCRepository.FabricSDK;
import bcluxs.DBDao.*;
import bcluxs.utils.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BCService {

    private final FabricSDK fabricSDK;
    private final DBService dbService;

    @Autowired
    public BCService(FabricSDK fabricSDK, DBService dbService) {
        this.fabricSDK = fabricSDK;
        this.dbService = dbService;
    }

    public SoldCommodity querySold(String serialNum) {
        BCSoldCommodity bcSoldCommodity = fabricSDK.querySold(serialNum);
        if (bcSoldCommodity == null) {
            return null;
        }
        SoldCommodity soldCommodity = new SoldCommodity();
        soldCommodity.setSerialNum(serialNum);
        soldCommodity.setQueryTime(bcSoldCommodity.getQueryTimes());
        soldCommodity.setRetailer(dbService.getRetailer(bcSoldCommodity.getRetailer()));
        soldCommodity.setLastQuery(Tools.transTimestamp(bcSoldCommodity.getLastQuery()));
        soldCommodity.setTransactionTime(Tools.transTimestamp(bcSoldCommodity.getTransactionTime()));
        soldCommodity.setCommodity(queryCommodity(bcSoldCommodity.getCommNum()));
        return soldCommodity;
    }

    private Commodity queryCommodity(String serialNum) {
        BCCommodity bcCommodity = fabricSDK.queryCommodity(serialNum);
        if (bcCommodity == null) {
            return null;
        }
        Commodity commodity = new Commodity();
        commodity.setSerialNum(serialNum);
        commodity.setFactory(dbService.getFactory(bcCommodity.getFactory()));
        commodity.setTransactionTime(Tools.transTimestamp(bcCommodity.getTransactionTime()));
        commodity.setLeather(queryLeather(bcCommodity.getLeatherNum()));
        return commodity;
    }


    public Leather queryLeather(String serialNum) {
        BCLeather bcLeather = fabricSDK.queryLeather(serialNum);
        if (bcLeather == null) {
            return null;
        }
        Leather leather = new Leather();
        leather.setSerialNum(serialNum);
        leather.setLayer(bcLeather.getLayer());
        leather.setTanning(bcLeather.getTanning());
        leather.setTransactionTime(Tools.transTimestamp(bcLeather.getTransactionTime()));
        leather.setProducer(dbService.getLeatherProducer(bcLeather.getProducer()));
        leather.setHide(queryHide(bcLeather.getHideNum()));
        return leather;
    }

    public Hide queryHide(String serialNum) {
        BCHide bcHide = fabricSDK.queryHide(serialNum);
        if (bcHide == null) {
            return null;
        }
        Hide hide = new Hide();
        hide.setSerialNum(serialNum);
        hide.setAnimalType(bcHide.getAnimalType());
        hide.setReserveType(bcHide.getReserveType());
        hide.setTransactionTime(Tools.transTimestamp(bcHide.getTransactionTime()));
        hide.setProducer(dbService.getHideProducer(bcHide.getProducer()));
        return hide;
    }

    public String sell(String id, String commoditySerialNum) {
        if (commoditySerialNum.length() != 43) {
            return null;
        }
        boolean ok = fabricSDK.marketchannelSell(commoditySerialNum);
        if (!ok) {
            Message message = new Message();
            message.setNewMSG(true);
            message.setMessageType(MessageType.WrongNumber);
            message.setSerialNum(commoditySerialNum);
            message.setTime(new Date());
            dbService.saveMessage(message);
            return null;
        }
        return fabricSDK.querychannelSell(id, commoditySerialNum);
    }

    public BCCommodity queryBCCommodity(String serialNum) {
        return fabricSDK.queryCommodity(serialNum);
    }

    public String produceCommodity(String factory, String totalCount, String leatherNum) {
        return fabricSDK.produceCommodity(factory, totalCount, leatherNum);
    }

    public String produceLeather(String tanning, String layer, String producer, String hideSerial) {
        return fabricSDK.produceLeather(tanning, layer, producer, hideSerial);
    }

    public String produceHide(String animalType, String reserveType, String producer) {
        return fabricSDK.produceHide(animalType, reserveType, producer);
    }
}
