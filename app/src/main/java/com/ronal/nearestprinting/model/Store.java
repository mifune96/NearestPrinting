package com.ronal.nearestprinting.model;

public class Store {

    private String storeUid;
    private String storeName;
    private String storeAddress;
    private String storePhoneNumber;
    private String openTime;
    private String closeTime;
    private String storeImage;
    private double storeLatitude;
    private double storeLongitude;

    public Store() {
    }

    public Store(String storeUid, String storeName, String storeAddress, String storePhoneNumber, String openTime, String closeTime, String storeImage, double storeLatitude, double storeLongitude) {
        this.storeUid = storeUid;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.storePhoneNumber = storePhoneNumber;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.storeImage = storeImage;
        this.storeLatitude = storeLatitude;
        this.storeLongitude = storeLongitude;
    }

    public String getStoreUid() {
        return storeUid;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public String getOpenTime() {
        return openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public String getStoreImage() {
        return storeImage;
    }

    public Double getStoreLatitude() {
        return storeLatitude;
    }

    public Double getStoreLongitude() {
        return storeLongitude;
    }

    public String getStorePhoneNumber() {
        return storePhoneNumber;
    }

    public void setStoreUid(String storeUid) {
        this.storeUid = storeUid;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public void setStorePhoneNumber(String storePhoneNumber) {
        this.storePhoneNumber = storePhoneNumber;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public void setStoreImage(String storeImage) {
        this.storeImage = storeImage;
    }

    public void setStoreLatitude(double storeLatitude) {
        this.storeLatitude = storeLatitude;
    }

    public void setStoreLongitude(double storeLongitude) {
        this.storeLongitude = storeLongitude;
    }
}
