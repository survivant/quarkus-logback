package com.comact.iep.keycloak;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.representations.idm.PartialImportRepresentation.Policy;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;

import io.quarkus.runtime.Startup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@Startup(0)
public class RealmInitializerClass {

    @ConfigProperty(name = "keycloak.realm")
    String realm;

    @ConfigProperty(name = "keycloak.realm.init.filename")
    String realmFilename;

    @ConfigProperty(name = "keycloak.admin.user", defaultValue = "")
    Optional<String> adminUsername;

    @ConfigProperty(name = "keycloak.admin.password", defaultValue = "")
    Optional<String> adminPassword;

    @PostConstruct
    public void init() {
        if (StringUtils.isNotEmpty(realmFilename)) {
            log.debug("Importing realm [{}]", realm);
            var client = new KeycloakRealmsHelper();

            RealmRepresentation realmRepresentation = null;
            try {
                realmRepresentation = client.getRealm(realm, adminUsername.orElse(null), adminPassword.orElse(null));
            } catch (Throwable ignore) {
            }

            // create realm is not present
            if (realmRepresentation == null) {
                realmRepresentation = createRealm(realm);
                realmRepresentation.setRevokeRefreshToken(true);
                realmRepresentation.setRefreshTokenMaxReuse(0);
                realmRepresentation.setAccessTokenLifespan(3);

                try {
                    client.createRealm(realmRepresentation);
                } catch (Throwable e) {
                    log.error("Unable to create realm [{}]", realm, e);
                }
            }

            // import realms
            try {
                var partialImportRepresentation = KeycloakRealmsHelper.loadFile(realmFilename, Policy.SKIP.toString());
                client.partialImport(realm, partialImportRepresentation, adminUsername.orElse(null), adminPassword.orElse(null));
            } catch (Throwable e) {
                log.error("Unable to import realm file [{}]", realmFilename, e);
            }
        }

    }

    /**
     * Create realm
     *
     * @param name realm name
     * @return realmReprensentation
     */
    private RealmRepresentation createRealm(String name) {
        var realm = new RealmRepresentation();

        realm.setRealm(name);
        realm.setEnabled(true);
        realm.setUsers(new ArrayList<>());
        realm.setClients(new ArrayList<>());
        realm.setAccessTokenLifespan(300);
        realm.setRevokeRefreshToken(false);

        var roles = new RolesRepresentation();
        List<RoleRepresentation> realmRoles = new ArrayList<>();

        roles.setRealm(realmRoles);
        realm.setRoles(roles);

        // temporary users
        realm.getRoles().getRealm().add(new RoleRepresentation("user", null, false));
        realm.getRoles().getRealm().add(new RoleRepresentation("admin", null, false));

        return realm;
    }
}
