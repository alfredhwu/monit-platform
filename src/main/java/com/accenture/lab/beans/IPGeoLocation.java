package com.accenture.lab.beans;

import com.github.thorqin.toolkit.db.DBService;

/**
 * Created by dingwen.wu on 6/26/2015.
 */
public class IPGeoLocation {
    @DBService.DBField
    private long ip;

    @DBService.DBField
    private String country;

    @DBService.DBField
    private String province;

    @DBService.DBField
    private String city;

    @DBService.DBField
    private String district;

    @DBService.DBField
    private String carrier;

    public IPGeoLocation() {

    }

    public IPGeoLocation(long ip, String country, String province, String city, String district, String carrier) {
        this.setIp(ip);
        this.setCountry(country);
        this.setProvince(province);
        this.setCity(city);
        this.setDistrict(district);
        this.setCarrier(carrier);
    }

    public long getIp() {
        return ip;
    }

    public void setIp(long ip) {
        this.ip = ip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }
}
