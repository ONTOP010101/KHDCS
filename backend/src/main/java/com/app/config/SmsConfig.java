package com.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sms.lianlu")
public class SmsConfig {

    private String apiBase;
    private String mchId;
    private String version;
    private String signType;
    private AppKeyConfig notify;
    private AppKeyConfig market;

    public static class AppKeyConfig {
        private String appId;
        private String appKey;

        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getAppKey() { return appKey; }
        public void setAppKey(String appKey) { this.appKey = appKey; }
    }

    public String getApiBase() { return apiBase; }
    public void setApiBase(String apiBase) { this.apiBase = apiBase; }
    public String getMchId() { return mchId; }
    public void setMchId(String mchId) { this.mchId = mchId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }
    public AppKeyConfig getNotify() { return notify; }
    public void setNotify(AppKeyConfig notify) { this.notify = notify; }
    public AppKeyConfig getMarket() { return market; }
    public void setMarket(AppKeyConfig market) { this.market = market; }

    /**
     * 根据短信类型获取对应的 AppId/AppKey
     */
    public AppKeyConfig getAppConfig(String smsType) {
        if ("营销".equals(smsType)) {
            return market;
        }
        return notify;
    }
}
