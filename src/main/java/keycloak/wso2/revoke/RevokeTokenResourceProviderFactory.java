package keycloak.wso2.revoke;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class RevokeTokenResourceProviderFactory implements RealmResourceProviderFactory {

    @Override
    public void close() {
        // implement if needed
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new RevokeTokenResourceProvider(session);
    }

    @Override
    public String getId() {
        return RevokeTokenResourceProvider.ID;
    }

    @Override
    public void init(Scope arg0) {
        // implement if needed
    }

    @Override
    public void postInit(KeycloakSessionFactory arg0) {
        // implement if needed
    }
}
