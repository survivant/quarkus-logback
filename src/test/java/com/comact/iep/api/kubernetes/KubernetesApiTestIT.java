package com.comact.iep.api.kubernetes;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comact.iep.api.kubernetes.kind.KindClusterForQuarkusTest;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.Tokens;
import io.quarkus.runtime.configuration.ProfileManager;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Use real objects to call CodecService
 * <p>
 * A cluster will be created by this tests and killed at the end
 */
@QuarkusTest
@QuarkusTestResource(value = KindClusterForQuarkusTest.class, restrictToAnnotatedClass = true)
public class KubernetesApiTestIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesApiTestIT.class);

    private static final String DEFAULT_NAMESPACE = "default";
    private static final String NAMESPACE = "namespace-cm";

    @BeforeAll
    static void init() throws Exception {
        LOGGER.info("The application is starting with profile {}", ProfileManager.getActiveProfile());
    }

    @BeforeEach
    public void initBeforeTest() {
        // create namespace if not present
        try {
            KindClusterForQuarkusTest.CLUSTER.createNamespace(NAMESPACE);
            KindClusterForQuarkusTest.CLUSTER.createRoleInNamespace("configmap-role", NAMESPACE); // allow kubernetes-api to query this namespace
        } catch (Exception ignore) {
            // ignore
        }
    }

    @AfterEach
    public void cleanup() {
        // delete namespace if present
        try {
            KindClusterForQuarkusTest.CLUSTER.deleteNamespace(NAMESPACE);
        } catch (Exception ignore) {
            // ignore
        }
    }

    @Test
    public void testListConfigMaps() {
        var configMaps = given()
                .when()
                .get("/configMap/")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList(".", ConfigMap.class);

        assertNotNull(configMaps, "configMaps can't be null");
    }
}
