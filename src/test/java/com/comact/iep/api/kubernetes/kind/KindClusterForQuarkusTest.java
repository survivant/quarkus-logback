package com.comact.iep.api.kubernetes.kind;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Used only for LOCAL testing for Integration testing
 *
 * Will launch a Kubernetes cluster for the IT instead of using a real cluster.
 *
 * Need to have "Kind" installed on your computer.
 *
 * Work in progress !!! (We could add the code to retreive Kind and install it if it's not present)
 *
 */
public class KindClusterForQuarkusTest implements QuarkusTestResourceLifecycleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(KindClusterForQuarkusTest.class);

    @SuppressWarnings("StaticVariableOfConcreteClass")
    public static final KindClusterLauncher CLUSTER = new KindClusterLauncher();

    // change the value to true to debug junits, but don't commit it with a value true
    // keeping the cluster after you are done testing with take lot of resources for nothing.
    public static final boolean KEEP_CLUSTER = true; // keep the cluster or delete it after the tests

    @Override
    public Map<String, String> start() {
        LOGGER.info("Creating Kind Cluster");
        try {
            CLUSTER.initKindClusterWithDefaults();
        } catch (Exception e) {
            LOGGER.error("Unable to create the cluster", e);
            if (!KEEP_CLUSTER) {
                throw new RuntimeException("Unable to create the cluster", e);
            }
        }

        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        if (KEEP_CLUSTER) {
            return;
        }

        try {
            CLUSTER.deleteKindCluster(KindClusterLauncher.DEFAULT_CLUSTER_NAME);
        } catch (Exception e) {
            LOGGER.error("Unable to delete the cluster", e);
        }
    }

    @Override
    public int order() {
        return 0;
    }
}
