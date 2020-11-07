package keycloak.wso2.revoke;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.OAuthErrorException;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.Urls;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.Cors;

import keycloak.wso2.revoke.model.TokenRevocationEvent;

public class RevokeTokenResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;
    private final TokenManager tokenManager;
    private EventSender eventSender;

    static final String ID = "revoke-token";
    private static final Logger LOG = Logger.getLogger(RevokeTokenResourceProvider.class);

    RevokeTokenResourceProvider(KeycloakSession session) {
        this.session = session;
        this.tokenManager = new TokenManager();

        eventSender = new EventSender();
    }

    @Override
    public void close() {
        // implement if required
    }

    @Override
    public Object getResource() {
        return this;
    }

    @OPTIONS
    public Response preflight(@Context HttpRequest request) {
        return Cors.add(request, Response.ok()).auth().preflight().allowedMethods("POST", "OPTIONS").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeToken(@FormParam("token") String token, @Context HttpRequest request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Revoke request received with Access Token : " + token);
        }

        if (token != null && !token.isEmpty()) {
            AccessToken accessToken = validateToken(token);
            if (accessToken != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Access token is still valid");
                }

                publishEvent(toTokenRevocationEvent(accessToken));
                return this.buildCorsResponse(request);
            }
        }
        return ErrorResponse.error("unsupported_token_type", Status.BAD_REQUEST);
    }

    private TokenRevocationEvent toTokenRevocationEvent(AccessToken accessToken) {
        long expiryTime = accessToken.getExp() - accessToken.getIat();
        TokenRevocationEvent tokenRevocationEvent = new TokenRevocationEvent(accessToken.getId(), expiryTime,
                accessToken.getPreferredUsername(), accessToken.getIssuedFor(), "JWT");
        tokenRevocationEvent.setTenantId(-1234);
        tokenRevocationEvent.setTenantDomain("carbon.super");
        return tokenRevocationEvent;
    }

    private void publishEvent(TokenRevocationEvent tRevocationEvent) {
        eventSender.publishEvent(tRevocationEvent);
    }

    private Response buildCorsResponse(@Context HttpRequest request) {
        Cors cors = Cors.add(request).auth().allowedMethods("POST").auth()
                .exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS, Cors.ACCESS_CONTROL_ALLOW_ORIGIN).allowAllOrigins();
        return cors.builder(Response.ok().type(MediaType.APPLICATION_JSON)).build();
    }

    @SuppressWarnings("unchecked")
    private AccessToken validateToken(String jwtToken) {
        try {
            RealmModel realm = session.getContext().getRealm();
            TokenVerifier<AccessToken> verifier = TokenVerifier.create(jwtToken, AccessToken.class)
                    .withChecks(TokenVerifier.IS_ACTIVE, new TokenVerifier.RealmUrlCheck(
                            Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName())));
            SignatureVerifierContext verifierContext = session
                    .getProvider(SignatureProvider.class, verifier.getHeader().getAlgorithm().name())
                    .verifier(verifier.getHeader().getKeyId());

            verifier.verifierContext(verifierContext);
            AccessToken accessToken = verifier.verify().getToken();

            if (!tokenManager.checkTokenValidForIntrospection(session, realm, accessToken)) {
                throw new VerificationException("introspection_failed");
            }

            return accessToken;
        } catch (VerificationException | OAuthErrorException e) {
            LOG.warn("Introspection of token failed", e);
        }

        return null;
    }
}
