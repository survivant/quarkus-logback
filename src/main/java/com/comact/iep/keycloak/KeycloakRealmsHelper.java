package com.comact.iep.keycloak;


import org.apache.commons.lang.StringUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.PartialImportRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.util.JsonSerialization;

import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Keycloak realms helper.
 */
@Slf4j
public class KeycloakRealmsHelper extends KeycloakTestClient {

    /**
     * Load file partial import representation.
     *
     * @param filename               the filename
     * @param policyIfResourceExists the policy if resource exists
     * @return the partial import representation
     * @throws Exception the exception
     */
    public static PartialImportRepresentation loadFile(String filename, String policyIfResourceExists) throws Exception {
        var realmRepresentation = JsonSerialization.readValue(KeycloakRealmsHelper.class.getResourceAsStream(filename), RealmRepresentation.class);

        var partialImport = new PartialImportRepresentation();
        partialImport.setIfResourceExists(policyIfResourceExists);
        partialImport.setClients(realmRepresentation.getClients());
        partialImport.setGroups(realmRepresentation.getGroups());
        partialImport.setIdentityProviders(realmRepresentation.getIdentityProviders());
        partialImport.setRoles(realmRepresentation.getRoles());
        partialImport.setUsers(realmRepresentation.getUsers());

        return partialImport;
    }

    /**
     * Get a realm
     * @param adminUsername
     * @param adminPassword
     */
    public RealmRepresentation getRealm(String realm, String adminUsername, String adminPassword) {
        return RestAssured
                .given()
                .auth().oauth2(getAccessToken(adminUsername, adminPassword))
                .when()
                .get(getAuthServerBaseUrl() + "/admin/realms/" + realm)
                .then()
                .extract()
                .body()
                .as(RealmRepresentation.class);
    }

    public String getAccessToken(String adminUsername, String adminPassword) {
        String token = null;

        if (StringUtils.isEmpty(adminUsername)) {
            token = getAdminAccessToken();
        } else {
            token = getAccessTokenInternal(adminUsername, adminPassword, "admin-cli", (String)null, this.getAuthServerBaseUrl() + "/realms/master");
        }

        return token;
    }

    private String getAccessTokenInternal(String userName, String userSecret, String clientId, String clientSecret, String authServerUrl) {
        var requestSpec = RestAssured.given().param("grant_type", new Object[]{"password"}).param("username", new Object[]{userName}).param("password", new Object[]{userSecret}).param("client_id", new Object[]{clientId});
        if (clientSecret != null && !clientSecret.isBlank()) {
            requestSpec = requestSpec.param("client_secret", new Object[]{clientSecret});
        }

        return ((AccessTokenResponse)((Response)requestSpec.when().post(authServerUrl + "/protocol/openid-connect/token", new Object[0])).as(AccessTokenResponse.class)).getToken();
    }

    /**
     * Partial import.
     *
     * @param realm                       the realm
     * @param partialImportRepresentation the partial import representation
     */
    public void partialImport(String realm, PartialImportRepresentation partialImportRepresentation, String adminUsername, String adminPassword) {
        RestAssured
                .given()
                .auth().oauth2(getAccessToken(adminUsername, adminPassword))
                .when()
                .accept("application/json, text/plain, */*")
                .contentType("application/json; charset=UTF-8")
                .body(partialImportRepresentation)
                .post(getAuthServerBaseUrl() + "/admin/realms/" + realm + "/partialImport")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();
    }

}
