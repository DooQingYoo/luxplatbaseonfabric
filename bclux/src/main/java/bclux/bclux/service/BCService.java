package bclux.bclux.service;

import bclux.bclux.BCDao.BCCommodity;
import bclux.bclux.BCDao.BCHide;
import bclux.bclux.BCDao.BCLeather;
import bclux.bclux.BCDao.BCSoldCommodity;
import bclux.bclux.BCRepository.FabricSDK;
import bclux.bclux.DBDao.*;
import bclux.bclux.utils.Tools;
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
        if (bcSoldCommodity.getQueried()) {
            soldCommodity.setQueryTime(1);
            Message message = new Message();
            message.setNewMSG(true);
            message.setSerialNum(serialNum);
            message.setMessageType(MessageType.DataBaseAbsent);
            message.setTime(new Date());
            dbService.saveMessage(message);
        } else {
            soldCommodity.setQueryTime(0);
        }
        soldCommodity.setRetailer(dbService.getRetailer(bcSoldCommodity.getRetailer()));
        soldCommodity.setLastQuery(null);
        soldCommodity.setTransactionTime(Tools.transTimestamp(bcSoldCommodity.getTransactionTime()));
        if (dbService.commodityExist(bcSoldCommodity.getCommNum())) {
            soldCommodity.setCommodity(dbService.queryCommodity(bcSoldCommodity.getCommNum()));
        } else {
            soldCommodity.setCommodity(queryCommodity(bcSoldCommodity.getCommNum()));
        }
        SoldCommodity newSold = new SoldCommodity(soldCommodity.getSerialNum(),
                soldCommodity.getRetailer(), soldCommodity.getTransactionTime(), soldCommodity.getCommodity(),
                soldCommodity.getQueryTime() + 1, new Date());
        dbService.save(newSold);
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
        if (dbService.leatherExist(bcCommodity.getLeatherNum())) {
            commodity.setLeather(dbService.queryLeather(bcCommodity.getLeatherNum()));
        } else {
            commodity.setLeather(queryLeather(bcCommodity.getLeatherNum()));
        }
        dbService.saveCommodity(commodity);
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
        if (dbService.hideExist(bcLeather.getHideNum())) {
            leather.setHide(dbService.queryHide(bcLeather.getHideNum()));
        } else {
            leather.setHide(queryHide(bcLeather.getHideNum()));
        }
        dbService.saveLeather(leather);
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
        dbService.saveHide(hide);
        return hide;
    }

    public String sell(String id, String commoditySerialNum) {
        if (commoditySerialNum.length() != 43) {
            return null;
        }
        boolean ok = fabricSDK.marketchannelSell(commoditySerialNum);
        if (!ok) {
            return null;
        }
        String serialNum = fabricSDK.querychannelSell(id, commoditySerialNum);
        return serialNum;
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
