package com.accenture.lab.services;

import com.accenture.lab.beans.IPGeoLocation;
import com.accenture.lab.utils.BaiduGeoIPAPIResponse;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.web.WebApplication;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by dingwen.wu on 6/29/2015.
 */
public class VisitDistributionService {
    /**
     * In order to support registration of WebApplication service.
     * @param configManager
     * @param configName
     * @param tracer
     */
    public VisitDistributionService(ConfigManager configManager, String configName, Tracer tracer) throws ValidateException {
    }

    public Map<String, Long> getVisitDistributionByIP() {
        HashMap<String, Long> distr = new HashMap<>();
        try {
            HttpClientService httpClientService = WebApplication.get().getService("http-client");
            HttpClient httpClient = httpClientService.getHttpClientInstance("elasticSearch");
            String result = httpClient.doPost(
                    "{" +
                            "  \"query\" : {" +
                            "    \"filtered\" : {" +
                            "      \"filter\": {" +
                            "        \"bool\": {" +
                            "          \"must\": [" +
                            "            {" +
                            "              \"range\" : {" +
                            "                \"timestamp\" : {" +
                            "                  \"from\" : \"2015-06-28 00:00:00.000\"," +
                            "                  \"to\" : \"2015-06-29 00:00:00.000\"" +
                            "                }" +
                            "              }" +
                            "            }" +
                            "          ]," +
                            "          \"must_not\": [" +
                            "            {" +
                            "              \"prefix\" : {" +
                            "                \"remote_addr\" : \"10.\"" +
                            "              }" +
                            "            }," +
                            "            {" +
                            "              \"prefix\" : {" +
                            "                \"remote_addr\" : \"192.168.\"" +
                            "              }" +
                            "            }" +
                            "          ]" +
                            "        }" +
                            "      }" +
                            "    }" +
                            "  }," +
                            "  \"size\" : 0," +
                            "  \"aggs\" : {" +
                            "    \"visit\": {" +
                            "      \"terms\": {" +
                            "        \"field\" : \"remote_addr\"," +
                            "        \"size\" : 0" +
                            "      }" +
                            "    }" +
                            "  }" +
                            "}");
            // parsing result
            JsonArray buckets = new JsonParser().parse(result).getAsJsonObject()
                    .get("aggregations").getAsJsonObject()
                    .get("visit").getAsJsonObject()
                    .get("buckets").getAsJsonArray();
            for (Iterator iter = buckets.iterator(); iter.hasNext();) {
                JsonObject obj = (JsonObject) iter.next();
                distr.put(obj.get("key").getAsString(), obj.get("doc_count").getAsLong());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return distr;
    }

    public Map<String, Long> getVisitDistributionByCity() {
        return this.getVisitDistribution("city");
    }

    public Map<String, Long> getVisitDistributionByProvince() {
        return this.getVisitDistribution("province");
    }

    private Map<String, Long> getVisitDistribution(String dimension) {
        IPGeoService ipGeoService = WebApplication.get().getService("ip-geo");
        dimension = dimension.trim();
        if ("country" != dimension &&
                "province" != dimension &&
                "city" != dimension &&
                "district" != dimension &&
                "carrier" != dimension) {
            return this.getVisitDistributionByIP();
        }
        Map<String, Long> distr = new HashMap<>();
        for (Map.Entry<String, Long> ipcount : this.getVisitDistributionByIP().entrySet()) {
            IPGeoLocation ipGeoLocation = ipGeoService.getIPGeoLocation(ipcount.getKey());
            if (null != ipGeoLocation) {
                String token = null;
                switch (dimension) {
                    case "country": token = ipGeoLocation.getCountry(); break;
                    case "province":
                        token = ipGeoLocation.getProvince();
                        break;
                    case "district":
                        token = ipGeoLocation.getCountry() + "/" +
                                ipGeoLocation.getProvince() + "/" +
                                ipGeoLocation.getCity() + "/" +
                                ipGeoLocation.getDistrict();
                        break;
                    case "carrier": token = ipGeoLocation.getCarrier(); break;
                    default:
                        token = ipGeoLocation.getCountry() + "/" +
                                ipGeoLocation.getProvince() + "/" +
                                ipGeoLocation.getCity(); // by default, aggr by city
                }
                if (distr.containsKey(token)) {
                    distr.put(token, distr.get(token) + ipcount.getValue());
                } else {
                    distr.put(token, ipcount.getValue());
                }
            }
        }
        return distr;
    }
}
