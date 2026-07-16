package com.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wework")
public class WeworkConfig {

    private String corpid;
    private int agentid;
    private String secret;
    private String customerContactSecret;
    private String apiBase;
    private String token;
    private String encodingAesKey;

    public String getCorpid() { return corpid; }
    public void setCorpid(String corpid) { this.corpid = corpid; }
    public int getAgentid() { return agentid; }
    public void setAgentid(int agentid) { this.agentid = agentid; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getCustomerContactSecret() { return customerContactSecret; }
    public void setCustomerContactSecret(String customerContactSecret) { this.customerContactSecret = customerContactSecret; }
    public String getApiBase() { return apiBase; }
    public void setApiBase(String apiBase) { this.apiBase = apiBase; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEncodingAesKey() { return encodingAesKey; }
    public void setEncodingAesKey(String encodingAesKey) { this.encodingAesKey = encodingAesKey; }
}
