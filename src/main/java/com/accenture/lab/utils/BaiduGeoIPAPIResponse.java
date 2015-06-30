package com.accenture.lab.utils;

import com.accenture.lab.beans.IPGeoLocation;
import com.accenture.lab.services.IPGeoService;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.web.WebApplication;
import com.google.gson.Gson;

/**
 * Created by dingwen.wu on 6/29/2015.
 */
public class BaiduGeoIPAPIResponse implements GeoIPAPIResponse{

    public int errNum;
    public String errMsg;
    public Data retData;

    class Data {
        public String ip;
        public String country;
        public String province;
        public String city;
        public String district;
        public String carrier;
    }

    public static BaiduGeoIPAPIResponse parseResult(String result) {
        BaiduGeoIPAPIResponse response = null;
        try {
            response = Serializer.fromJson(result, BaiduGeoIPAPIResponse.class);
        } catch (Exception e) {
            response = null;
            WebApplication.get().getLogger().info("Cannot retrieve IP Geo info from result [" + result + "].");
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public IPGeoLocation retrieveIPGeoLocation() {
        if (null == this.retData) return null;
        return new IPGeoLocation(
                IPGeoService.getIPHash(this.retData.ip),
                this.retData.country,
                this.retData.province,
                this.retData.city,
                this.retData.district,
                this.retData.carrier);
    }
}
