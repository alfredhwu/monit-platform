package com.accenture.lab.services;

import com.accenture.lab.beans.IPGeoLocation;
import com.accenture.lab.utils.BaiduGeoIPAPIResponse;
import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.web.WebApplication;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by dingwen.wu on 6/26/2015.
 */
public class IPGeoService {

    final Logger logger = WebApplication.get().getLogger();

    /**
     * In order to support registration of WebApplication service.
     * @param configManager
     * @param configName
     * @param tracer
     */
    public IPGeoService(ConfigManager configManager, String configName, Tracer tracer) throws ValidateException {
    }

    public IPGeoLocation getIPGeoLocation(String ip, boolean cached) {
        IPGeoLocation ipGeoLocation = this.getIPGeoLocationFromDB(ip);
        if (null == ipGeoLocation) {
            logger.info("IP [" + ip + "] not found in DB cache, trying to retrieve IP geo info from remote API");
            ipGeoLocation = this.getIPGeoLocationFromAPI(ip, cached);
        }
        return ipGeoLocation;
    }

    public IPGeoLocation getIPGeoLocation(String ip) {
        return this.getIPGeoLocation(ip, true);
    }

    public IPGeoLocation getIPGeoLocationFromDB(String ip) {
        long ipHash = IPGeoService.getIPHash(ip);
        IPGeoLocation ipGeoLocation = null;
        String sql = "select * from ip_geo_location where ip = ?";
        try {
            DBService db = WebApplication.get().getService("db");
            ipGeoLocation = db.queryFirst(sql, IPGeoLocation.class, ipHash);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ipGeoLocation;
    }

    public IPGeoLocation getIPGeoLocationFromAPI(String ip, boolean cached) {
        IPGeoLocation ipGeoLocation = null;
        try {
            HttpClientService httpClientService = WebApplication.get().getService("http-client");
            HttpClient httpClient = httpClientService.getHttpClientInstance("baiduIPGeo");
            HashMap<String, String> params = new HashMap<>();
            params.put("ip", ip);
            String result = httpClient.doGet(params);
            logger.info(result);
            JsonObject response = new JsonParser().parse(result).getAsJsonObject();
            if (response.get("errNum").getAsInt() == 0 &&
                    response.get("errMsg").getAsString().trim().equals("success")) {
                JsonObject retData = response.get("retData").getAsJsonObject();
                ipGeoLocation = new IPGeoLocation(
                        IPGeoService.getIPHash(retData.get("ip").getAsString()),
                        this.getRetData(retData, "country"),
                        this.getRetData(retData, "province"),
                        this.getRetData(retData, "city"),
                        this.getRetData(retData, "district"),
                        this.getRetData(retData, "carrier")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != ipGeoLocation && cached) {
            this.cacheIPGeoLocationIntoDB(ipGeoLocation);
        }
        return ipGeoLocation;
    }

    private String getRetData(JsonObject retData, String retDataKey) {
        String data = retData.get(retDataKey).getAsString().trim();
        return data.equals("None") || data.equals("未知") ? null : data;
    }

    private void cacheIPGeoLocationIntoDB(IPGeoLocation ipGeoLocation) {
        String sql = "INSERT INTO ip_geo_location (ip, country, province, city, district, carrier) VALUES (?, ?, ?, ?, ?, ?)";
        DBService db = WebApplication.get().getService("db");
        try {
            db.execute(sql,
                    ipGeoLocation.getIp(),
                    ipGeoLocation.getCountry(),
                    ipGeoLocation.getProvince(),
                    ipGeoLocation.getCity(),
                    ipGeoLocation.getDistrict(),
                    ipGeoLocation.getCarrier());
            logger.info("Geo info of IP [" + IPGeoService.getIPString(ipGeoLocation.getIp()) + "] cached into DB");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static long getIPHash(String ip) {
        String ipString = ip.trim();
//        String ipPattern = "([1-9]|[1-9]//d|1//d{2}|2[0-4]//d|25[0-5])(//.(//d|[1-9]//d|1//d{2}|2[0-4]//d|25[0-5])){3}";
//        Pattern pattern = Pattern.compile(ipPattern);
//        Matcher matcher = pattern.matcher(ipString);
//        if (!matcher.matches()) {
//            return -1;
//        }
        long ipHash = 0;
        for (String segment : ipString.split("\\.")) {
            ipHash = ipHash * 256 + Integer.parseInt(segment);
        }
        return ipHash;
    }

    public static final long IP_MAX = 4294967295L;

    public static String getIPString(long iphash) {
        if (iphash < 0 || iphash > IP_MAX) {
            return null;
        }
        Long residue = iphash / 256;
        String ip = String.valueOf(iphash % 256);
        for (int i=0; i<3; i++) {
            ip = String.valueOf(residue % 256) + "." + ip;
            residue /= 256;
        }
        return ip;
    }
}