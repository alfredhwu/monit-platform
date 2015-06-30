package com.accenture.lab.services;

import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.WebAppService;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Created by dingwen.wu on 6/26/2015.
 */
public class HttpClient {

    final private Logger logger = WebApplication.get().getLogger();
    private String apiURL = null;
    private Map<String, String> proxySettings = null;
    private Map<String, String> httpHeaders = null;

    public HttpClient(String apiURL, Map<String, String> httpHeaders) {
        this.apiURL = apiURL;
        this.httpHeaders = httpHeaders;
    }

    public HttpClient(String apiURL, Map<String, String> httpHeaders, Map<String, String> proxySettings) {
        this.apiURL = apiURL;
        this.proxySettings = proxySettings;
        this.httpHeaders = httpHeaders;
    }

    public String doGet(Map<String, String> params) {

        String result = null;

        String queryParams = "";
        for (Map.Entry<String, String> param : params.entrySet()) {
            queryParams += param.getKey() + "=" + param.getValue() + "&";
        }

        try {
            // prepare Http URL Connection
            HttpURLConnection httpURLConnection = null;

            URL url = new URL(apiURL + "?" + queryParams);
            if (null == this.proxySettings) { // if connect without proxy
                httpURLConnection = (HttpURLConnection) url.openConnection();
            } else { // if connect with proxy
                InetSocketAddress proxyServerAddr = new InetSocketAddress(this.proxySettings.get("server"),
                        Integer.parseInt(this.proxySettings.get("port")));
                Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyServerAddr);
                String encoded = new String(Base64.getEncoder().encodeToString(
                        new String(this.proxySettings.get("user")+":"+
                                this.proxySettings.get("passwd")).getBytes("utf-8")));

                httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
                httpURLConnection.setRequestProperty("Proxy-Authorization", "Basic "+encoded);
            }
            httpURLConnection.setReadTimeout(5 * 1000);
            httpURLConnection.setRequestMethod("GET");
            // add header to request
            if (null != this.httpHeaders && !this.httpHeaders.isEmpty()) {
                for (Map.Entry<String, String> header : this.httpHeaders.entrySet()) {
                    httpURLConnection.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            // do request
            httpURLConnection.connect();

            InputStreamReader inputStreamReader =
                    new InputStreamReader(httpURLConnection.getInputStream(), "utf-8");
            Scanner inputStream = new Scanner(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();

            while (inputStream.hasNext()) {
                stringBuffer.append(inputStream.nextLine());
            }

//            this.logger.info("Response from API call: " + stringBuffer.toString());

            if (httpURLConnection.getResponseCode() == 200)
                result =  stringBuffer.toString();
//            if (httpURLConnection.getResponseCode() == 200 &&
//                    httpURLConnection.getContentType().contains("application/json")) {
//                return stringBuffer.toString();
//            }
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String doPost(String data) {
        String result = null;
        try {
            // prepare Http URL Connection
            HttpURLConnection httpURLConnection = null;
            URL url = new URL(apiURL);
            if (null == this.proxySettings) { // if connect without proxy
                httpURLConnection = (HttpURLConnection) url.openConnection();
            } else { // if connect with proxy
                InetSocketAddress proxyServerAddr = new InetSocketAddress(this.proxySettings.get("server"),
                        Integer.parseInt(this.proxySettings.get("port")));
                Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyServerAddr);
                String encoded = new String(Base64.getEncoder().encodeToString(
                        new String(this.proxySettings.get("user")+":"+
                                this.proxySettings.get("passwd")).getBytes("utf-8")));

                httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
                httpURLConnection.setRequestProperty("Proxy-Authorization", "Basic "+encoded);
            }
            httpURLConnection.setReadTimeout(5 * 1000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            // add header to request
            if (null != this.httpHeaders && !this.httpHeaders.isEmpty()) {
                for (Map.Entry<String, String> header : this.httpHeaders.entrySet()) {
                    httpURLConnection.setRequestProperty(header.getKey(), header.getValue());
                }
            }


            // do POST request
            httpURLConnection.connect();
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.writeBytes(data);
            dataOutputStream.flush();
            dataOutputStream.close();

            InputStreamReader inputStreamReader =
                    new InputStreamReader(httpURLConnection.getInputStream(), "utf-8");
            Scanner inputStream = new Scanner(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();

            while (inputStream.hasNext()) {
                stringBuffer.append(inputStream.nextLine());
            }

//            this.logger.info("Response from API call: " + stringBuffer.toString());

            if (httpURLConnection.getResponseCode() == 200)
                result = stringBuffer.toString();
//            if (httpURLConnection.getResponseCode() == 200 &&
//                    httpURLConnection.getContentType().contains("application/json")) {
//                return stringBuffer.toString();
//            }
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
