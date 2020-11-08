package keycloak.wso2.revoke;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jboss.logging.Logger;

import keycloak.wso2.revoke.model.TokenRevocationEvent;

public class EventSender {

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(200, 500, 100L,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    private static final Logger LOG = Logger.getLogger(EventSender.class);

    public void publishEvent(TokenRevocationEvent tRevocationEvent) {
        String notificationEndpoint = System.getProperty("wso2.apim.notification.ep");
        String username = System.getProperty("wso2.apim.notification.username");
        String password = System.getProperty("wso2.apim.notification.password");

        // the default configurations
        // notificationEndpoint = https://tm-hostname:9443/internal/data/v1/notify
        // username = admin
        // password = admin

        EventRunner event = new EventRunner(notificationEndpoint, username, password, tRevocationEvent);
        THREAD_POOL_EXECUTOR.execute(event);
    }

    public static class EventRunner implements Runnable {

        private String notificationEndpoint;
        private String username;
        private String password;
        private TokenRevocationEvent event;

        public EventRunner(String notificationEndpoint, String username, String password, TokenRevocationEvent event) {
            this.notificationEndpoint = notificationEndpoint;
            this.username = username;
            this.password = password;
            this.event = event;
        }

        @Override
        public void run() {
            try (CloseableHttpClient closeableHttpClient = new org.keycloak.connections.httpclient.HttpClientBuilder()
                    .disableTrustManager().build()) {

                HttpPost httpPost = new HttpPost(notificationEndpoint);
                if (!username.isEmpty() && !password.isEmpty()) {
                    String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
                    httpPost.addHeader("Authorization", "Basic " + credentials);
                }

                String content = new ObjectMapper().writeValueAsString(event);
                StringEntity requestEntity = new StringEntity(content);
                requestEntity.setContentType(MediaType.APPLICATION_JSON);
                httpPost.setEntity(requestEntity);

                try (CloseableHttpResponse execute = closeableHttpClient.execute(httpPost)) {
                    // closable
                }
            } catch (IOException e) {
                LOG.error("Error while sending Revocation Event to " + notificationEndpoint, e);
            }
        }
    }
}
