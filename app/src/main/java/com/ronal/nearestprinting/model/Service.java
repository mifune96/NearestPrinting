package com.ronal.nearestprinting.model;

public class Service {

    private String serviceUid;
    private String storeUid;
    private String serviceName;
    private String serviceSellUnitType;
    private String minimalOrder;
    private String servicePrice;

    public Service() {
    }

    public Service(String serviceUid, String storeUid, String serviceName, String serviceSellUnitType, String minimalOrder, String servicePrice) {
        this.serviceUid = serviceUid;
        this.storeUid = storeUid;
        this.serviceName = serviceName;
        this.serviceSellUnitType = serviceSellUnitType;
        this.minimalOrder = minimalOrder;
        this.servicePrice = servicePrice;
    }

    public String getServiceUid() {
        return serviceUid;
    }

    public void setServiceUid(String serviceUid) {
        this.serviceUid = serviceUid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceSellUnitType() {
        return serviceSellUnitType;
    }

    public void setServiceSellUnitType(String serviceSellUnitType) {
        this.serviceSellUnitType = serviceSellUnitType;
    }

    public String getMinimalOrder() {
        return minimalOrder;
    }

    public void setMinimalOrder(String minimalOrder) {
        this.minimalOrder = minimalOrder;
    }

    public String getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(String servicePrice) {
        this.servicePrice = servicePrice;
    }

    public String getStoreUid() {
        return storeUid;
    }

    public void setStoreUid(String storeUid) {
        this.storeUid = storeUid;
    }
}
