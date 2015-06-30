package com.accenture.lab.services;

import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.web.WebApplication;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by dingwen.wu on 6/26/2015.
 */
public class HttpClientService {

    private final Logger logger = WebApplication.get().getLogger();
    private final ConfigManager configManager = WebApplication.get().getConfigManager();

    /**
     * In order to support registration of WebApplication service.
     * @param configManager
     * @param configName
     * @param tracer
     */
    public HttpClientService(ConfigManager configManager, String configName, Tracer tracer) throws ValidateException {
    }

    public HttpClient getHttpClientInstance(String clientType) throws Exception {
        clientType = clientType.trim();
        String apiURL = this.configManager.getString("httpClients/" + clientType + "/apiURL");
        if (null == apiURL || apiURL.trim().length() == 0) {
            throw new Exception("HttpClient initialization failed, as the corresponding API URL not configured");
        }
        // retrieve http header settings
        HashMap<String, String> httpHeaders = new HashMap<>();
        for (Map.Entry<String, Object> header :
                this.configManager.getMap("httpClients/" + clientType + "/apiHeaders").entrySet()) {
            httpHeaders.put(header.getKey(), header.getValue().toString());
        }
        // if using proxy, retrieve proxy settings
        if (this.configManager.getBoolean("httpClients/" + clientType + "/proxy")) {
            logger.info("Using proxy to access remote API with HTTP client [" + clientType + "]");
            HashMap<String, String> proxy = new HashMap<>();
            proxy.put("server",this.configManager.getString("httpClients/" + clientType + "/proxySettings/server"));
            proxy.put("port",this.configManager.getString("httpClients/" + clientType + "/proxySettings/port"));
            proxy.put("user",this.configManager.getString("httpClients/" + clientType + "/proxySettings/user"));
            proxy.put("passwd", this.configManager.getString("httpClients/" + clientType + "/proxySettings/passwd"));
            return new HttpClient(apiURL, httpHeaders, proxy);
        }
        return new HttpClient(apiURL, httpHeaders);
    }
}
