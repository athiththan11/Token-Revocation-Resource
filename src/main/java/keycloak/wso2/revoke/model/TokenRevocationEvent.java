package keycloak.wso2.revoke.model;

import java.io.Serializable;
import java.util.UUID;

public class TokenRevocationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private long timestamp;
    private String type;
    private int tenantId;
    private String tenantDomain;

    private String accessToken;
    private long expiryTime;
    private String user;
    private String consumerKey;
    private String tokenType;

    public TokenRevocationEvent(String accessToken, long expiryTime, String user, String consumerKey,
            String tokenType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.type = "token_revocation";

        this.accessToken = accessToken;
        this.expiryTime = expiryTime;
        this.user = user;
        this.consumerKey = consumerKey;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
