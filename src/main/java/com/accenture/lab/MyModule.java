package com.accenture.lab;

import com.accenture.lab.beans.IPGeoLocation;
import com.accenture.lab.services.HttpClient;
import com.accenture.lab.services.HttpClientService;
import com.accenture.lab.services.IPGeoService;
import com.accenture.lab.services.VisitDistributionService;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.web.HttpException;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@WebModule
public class MyModule {
	/*
    @Service("db")
    DBService db;
	*/
    @Service("ip-geo")
    IPGeoService ipGeoService;

    @Service("http-client")
    HttpClientService httpClientService;

    @Service("visit-distribution")
    VisitDistributionService visitDistributionService;

    final Logger logger = WebApplication.get().getLogger();

    @WebEntry(method = HttpMethod.POST)
    public WebContent getServerInfo(HttpServletRequest request) {
        String serverTime = new DateTime().toString(DateTimeFormat.mediumDateTime());
        String serverName = request.getServletContext().getServerInfo();
        return WebContent.json(serverName + "<br>" + serverTime);
    }

    @WebEntry(method = HttpMethod.GET)
    public WebContent getVisitDistribution(@Param("dimension") String dimension) {
        if (null == dimension) {
            dimension = "ip";
        }
        Map<String, Long> distr = null;
        switch (dimension.trim()) {
            case "province":
                distr = this.visitDistributionService.getVisitDistributionByProvince();
                break;
            case "city":
                distr = this.visitDistributionService.getVisitDistributionByCity();
                break;
            case "district":
                break;
            default:
                distr = this.visitDistributionService.getVisitDistributionByIP();
        }
        return WebContent.jsonString(Serializer.toJsonString(distr));
    }

    @WebEntry(method = HttpMethod.GET)
    public WebContent getIPGeoLocation(@Param("ip") String ip) {
        IPGeoLocation ipGeoLocation = this.ipGeoService.getIPGeoLocation(ip, true);
        if (null == ipGeoLocation) {
            throw new HttpException(404, "Geo locaion of IP [" + ip + "] not found !");
        }
        return WebContent.json(ipGeoLocation);
    }


    @WebEntry(method = HttpMethod.GET)
    public WebContent getIPGeoLocationString(@Param("ip") String ip) {
        String result = null;
        try {
            HttpClient httpClient = this.httpClientService.getHttpClientInstance("baiduIPGeo");
            HashMap<String, String> params = new HashMap<>();
            params.put("ip", ip);

            result = httpClient.doGet(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return WebContent.jsonString(result);
    }
}

