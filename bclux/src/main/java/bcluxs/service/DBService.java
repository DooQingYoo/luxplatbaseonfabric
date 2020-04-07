package bcluxs.service;

import bcluxs.DBDao.*;
import bcluxs.DBRepository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

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
    private final MessageRepository message;

    @Autowired
    public DBService(SoldCommodityRepository soldCommodity, CommodityRepository commodity, LeatherRepository leather, HideRepository hide, FactoryRepository factory, HideProducerRepository hideProducer, LeatherProducerRepository leatherProducer, RetailerRepository retailer, MessageRepository message) {
        this.soldCommodity = soldCommodity;
        this.commodity = commodity;
        this.leather = leather;
        this.hide = hide;
        this.factory = factory;
        this.hideProducer = hideProducer;
        this.leatherProducer = leatherProducer;
        this.retailer = retailer;
        this.message = message;
    }

    public boolean queryExist(String serialNum) {
        return soldCommodity.existsById(serialNum);
    }

    public SoldCommodity query(String serialNum) {
        return soldCommodity.getOne(serialNum);
    }

    public boolean commodityExist(String serilNum) {
        return commodity.existsById(serilNum);
    }

    public Commodity queryCommodity(String serilNum) {
        return commodity.getOne(serilNum);
    }

    public boolean leatherExist(String serialNum) {
        return leather.existsById(serialNum);
    }

    public Leather queryLeather(String serialNum) {
        return leather.getOne(serialNum);
    }

    public boolean hideExist(String serialNum) {
        return hide.existsById(serialNum);
    }

    public Hide queryHide(String serialNum) {
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

    public Retailer getRetailer(Integer id) {
        return retailer.getOne(id);
    }

    public Factory getFactory(Integer id) {
        return factory.getOne(id);
    }

    public LeatherProducer getLeatherProducer(Integer id) {
        return leatherProducer.getOne(id);
    }

    public HideProducer getHideProducer(Integer id) {
        return hideProducer.getOne(id);
    }

    public List<HideProducer> getAllHide() {
        return hideProducer.findAll();
    }

    public List<LeatherProducer> getAllLeather() {
        return leatherProducer.findAll();
    }

    public List<Factory> getAllFactory() {
        return factory.findAll();
    }

    public List<Retailer> getAllRetailer() {
        return retailer.findAll();
    }

    public void saveHideProducer(HideProducer hp) {
        hideProducer.save(hp);
    }

    public void saveLeatherProducer(LeatherProducer lp) {
        leatherProducer.save(lp);
    }

    public void saveFactory(Factory ft) {
        factory.save(ft);
    }

    public void saveRetailer(Retailer rt) {
        retailer.save(rt);
    }

    public int nextHideProducerId() {
        int count = (int) hideProducer.count();
        return ++count;
    }

    public int nextLeatherProducerId() {
        int count = (int) hideProducer.count();
        return ++count;
    }

    public int nextFactoryId() {
        int count = (int) factory.count();
        return ++count;
    }

    public int nextRetailerId() {
        int count = (int) retailer.count();
        return ++count;
    }

    public int getMessageCount() {
        return message.countAllByNewMSGTrue();
    }

    public List<Message> getMessages() {
        return message.getAllByNewMSGTrue();
    }

    public Message getMessage(Integer id) {
        return message.getOne(id);
    }

    public void saveMessage(Message mg) {
        message.save(mg);
    }
}
