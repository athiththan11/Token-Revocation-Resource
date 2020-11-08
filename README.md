# Token Revocation Resource

[:construction: Dev In Progress]

This repo contains a sample Keycloak `ResourceProvider` implementation to revoke the Token Caches in WSO2 API Manager (Gateway) servers when using Keycloak as the Key Manager with the WSO2 API Manager `v3.2.0`.

> Instructions on configuring Keycloak as a Key Manager with the WSO2 API Manager v3.2.0 can be found under [Configure Keycloak Connector](https://apim.docs.wso2.com/en/latest/administer/key-managers/configure-keycloak-connector/)

The implementation introduces a new endpoint named `revoke-token` in each configured realm of the Keycloak server to verify the JWT token's validity and to send `Token Revocation Notifications` to the WSO2 API Manager server.

## Build & Deploy

### Build

Execute the following command from the root directory of the project to build the JAR

```sh
mvn clean package
```

### Deploy

Copy the built JAR artifact from the `<project>/target` directory and place it inside the `<keycloak>/standalone/deployments` directory.

After successful deployment, start the Keycloak server with the following System Properties to configure the implemented `EventSender` to send Token Revocation Events to the WSO2 API Manager server

```properties
wso2.apim.notification.ep="https://tm-hostname:9443/internal/data/v1/notify"
wso2.apim.notification.username="admin-username"
wso2.apim.notification.password="admin-password"
```

```sh
# a sample startup command will look like below
sh standalone.sh -Dwso2.apim.notification.ep="https://localhost:9443/internal/data/v1/notify" -Dwso2.apim.notification.username="admin" -Dwso2.apim.notification.password="admin"
```

## Usage

### Use-Case

The WSO2 API Manager v3.2.0 provides a platform to configure third-party Key Managers. Out-of-the-box, the API Manager supports Token cache revocation feature when configuring the WSO2 Identity Server as Key Manager.

This sample implementation, introduces an endpoint named `revoke-token` in the Keycloak server to validate the Token's validity and signature and then to send a Token Revocation notification to the API Manager servers to mark the Bearer JWT token as revoked in the Gateway servers. This ensures that the JWT token cannot be used again to invoke the APIs through the API Manager servers.

Given below is the `revoke-token` endpoint definition and the usage.

### Revoke Token Endpoint

The usage of the introduced `revoke-token` endpoint will be as following

```http
POST /auth/realms/{realm-name}/revoke-token
Authorization: Basic Base64(clientId:clientSecret)
Content-Type: application/x-www-form-urlencoded

token=<JWT Bearer Token>
```

## License

[Apache-2.0](LICENSE)
