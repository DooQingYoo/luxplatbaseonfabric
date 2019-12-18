package bclux.bclux.service;

import bclux.bclux.DBDao.*;
import bclux.bclux.DBRepository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class DBService {

    private final SoldCommodityRepository soldCommodity;
    private final CommodityRepository commodity;
    private final LeatherRepository leather;
    private final HideRepository hide;
    private final FactoryRepository factory;
    private final HideProducerRepository hideProducer;
    private final LeatherProducerRepository leatherProducer;
    private final RetailerRepository retailer;

    @Autowired
    public DBService(SoldCommodityRepository soldCommodity, CommodityRepository commodity, LeatherRepository leather, HideRepository hide, FactoryRepository factory, HideProducerRepository hideProducer, LeatherProducerRepository leatherProducer, RetailerRepository retailer) {
        this.soldCommodity = soldCommodity;
        this.commodity = commodity;
        this.leather = leather;
        this.hide = hide;
        this.factory = factory;
        this.hideProducer = hideProducer;
        this.leatherProducer = leatherProducer;
        this.retailer = retailer;
    }

    public boolean queryExist(String serialNum) {
        return soldCommodity.existsById(serialNum);
    }

    public SoldCommodity query(String serialNum) {
        return soldCommodity.getOne(serialNum);
    }

    boolean commodityExist(String serilNum) {
        return commodity.existsById(serilNum);
    }

    Commodity queryCommodity(String serilNum) {
        return commodity.getOne(serilNum);
    }

    boolean leatherExist(String serialNum) {
        return leather.existsById(serialNum);
    }

    Leather queryLeather(String serialNum) {
        return leather.getOne(serialNum);
    }

    boolean hideExist(String serialNum) {
        return hide.existsById(serialNum);
    }

    Hide queryHide(String serialNum) {
        return hide.getOne(serialNum);
    }

    public HideProducer getHideProducer(String name) {
        return hideProducer.getByName(name);
    }

    public LeatherProducer getLeatherProducer(String name) {
        return leatherProducer.getByName(name);
    }

    public Factory getFactory(String name) {
        return factory.getByName(name);
    }

    public Retailer getRetailer(String name) {
        return retailer.getByName(name);
    }

    @Transactional
    public void save(SoldCommodity soldCommodity1) {
        soldCommodity.save(soldCommodity1);
    }

    @Transactional
    public void saveCommodity(Commodity commodity1) {
        commodity.save(commodity1);
    }

    @Transactional
    public void saveLeather(Leather leather1) {
        leather.save(leather1);
    }

    @Transactional
    public void saveHide(Hide hide1) {
        hide.save(hide1);
    }

    Retailer getRetailer(Integer id) {
        return retailer.getOne(id);
    }

    public Factory getFactory(Integer id) {
        return factory.getOne(id);
    }

    LeatherProducer getLeatherProducer(Integer id) {
        return leatherProducer.getOne(id);
    }

    HideProducer getHideProducer(Integer id) {
        return hideProducer.getOne(id);
    }
}
