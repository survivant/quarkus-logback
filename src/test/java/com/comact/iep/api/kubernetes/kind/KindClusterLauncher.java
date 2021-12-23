package com.comact.iep.api.kubernetes.kind;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used only for LOCAL testing for Integration testing
 * <p>
 * Will launch a Kubernetes cluster for the IT instead of using a real cluster.
 * <p>
 * Need to have "Kind" installed on your computer.
 * <p>
 * Work in progress !!! (We could add the code to retrieve Kind and install it if it's not present)
 */
@SuppressWarnings({"CallToRuntimeExecWithNonConstantString", "CallToRuntimeExec"})
@ApplicationScoped
public class KindClusterLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(KindClusterLauncher.class);

    public static final String DEFAULT_CLUSTER_NAME = "kind";
    private static final int DEFAULT_TIMEOUT = 180;
    private static final String DEFAULT_IMAGE = "kindest/node:v1.22.2";
    private static final String DEFAULT_CONFIG_FILE = "kind-config.yaml";

    private String clusterName = "kind-kind";

    /**
     * Find the version of Kind installed
     *
     * @return           the version of Kind installed
     * @throws Exception if there are errors calling Kind
     */
    private String getKindVersion() throws Exception {
        var command = "kind version";

        LOGGER.debug("request : command [{}]", command);

        var pr = Runtime.getRuntime().exec(command);
        var version = IOUtils.toString(pr.getInputStream(), Charset.defaultCharset()).trim();
        waitForNormalTermination(pr, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        return version;
    }

    /**
     * Retrieve kubeconfig from Kind
     *
     * @return           kubeconfig from Kind
     * @throws Exception if there are errors calling Kind
     */
    public String getKindKubeConfig() throws Exception {
        var command = "kind get kubeconfig";

        LOGGER.debug("request : command [{}]", command);

        var pr = Runtime.getRuntime().exec(command);
        var kubeconfig = IOUtils.toString(pr.getInputStream(), Charset.defaultCharset()).trim();
        waitForNormalTermination(pr, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        return kubeconfig;
    }

    /**
     * Create a cluster
     *
     * @param  imageName          Kind image that will be used
     * @param  clusterName        name of the cluster
     * @param  config             the config to use to create the cluster
     * @param  keepCurrentContext if true, it will set back the context the the current context in kubeconfig
     * @throws Exception          if there are errors calling Kind
     */
    private void createKindCluster(String imageName, String clusterName, String config, boolean keepCurrentContext) throws Exception {
        var command = "kind create cluster ";

        if (!StringUtils.isEmpty(imageName)) {
            command += " --image " + imageName;
        }

        if (!StringUtils.isEmpty(clusterName)) {
            command += " --name " + clusterName;
            this.clusterName = "kind-" + clusterName;
        }

        if (!StringUtils.isEmpty(config)) {
            command += " --config " + config;
        }

        String currentContext = null;
        if (keepCurrentContext) {
            currentContext = getCurrentKubeContext();
        }

        LOGGER.debug("createKindCluster request : command [{}]", command);

        var pr = Runtime.getRuntime().exec(command);

        var output = IOUtils.toString(pr.getInputStream(), Charset.defaultCharset()).trim();
        waitForNormalTermination(pr, DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        if (!StringUtils.isEmpty(currentContext)) {
            setCurrentKubeContext(currentContext);
        }
    }

    /**
     * Delete the cluster
     *
     * @param  clusterName name of the cluster
     * @throws Exception   if there are errors calling Kind
     */
    public void deleteKindCluster(String clusterName) throws Exception {
        var command = "kind delete cluster ";

        if (!StringUtils.isEmpty(clusterName)) {
            command += " --name " + clusterName;
        }

        LOGGER.debug("deleteKindCluster request : command [{}]", command);

        var pr = Runtime.getRuntime().exec(command);

        var output = IOUtils.toString(pr.getInputStream(), Charset.defaultCharset()).trim();
        waitForNormalTermination(pr, DEFAULT_TIMEOUT, TimeUnit.SECONDS);

    }

    /**
     * Find the current context in kubeconfig
     *
     * @return           the current context in kubeconfig
     * @throws Exception if there are errors calling Kind
     */
    private String getCurrentKubeContext() throws Exception {
        var command = "kubectl config current-context";
        LOGGER.debug("getCurrentKubeContext request : command [{}]", command);

        var pr = Runtime.getRuntime().exec(command);
        var context = IOUtils.toString(pr.getInputStream(), Charset.defaultCharset()).trim();
        try {
            waitForNormalTermination(pr, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // context probably not setted
        }

        return context;
    }

    /**
     * Set the current context in kubeconfig
     *
     * @param  context   the context to use in kubeconfig
     * @throws Exception if there are errors calling Kind
     */
    private void setCurrentKubeContext(String context) throws Exception {
        var command = "kubectl config use-context " + context;
        LOGGER.debug("setCurrentKubeContext request : command [{}]", command);

        var pr = Runtime.getRuntime().exec(command);
        waitForNormalTermination(pr, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * Check if Kind is installed on the system
     *
     * @return           true if Kind executable is found
     * @throws Exception if there are errors calling Kind
     */
    private boolean isKindInstalled() throws Exception {
        var version = getKindVersion();

        return version != null;
    }

    /**
     * check if the cluster exist
     *
     * @param  clusterName cluster name
     * @return             true if the cluster exist
     * @throws Exception   if there are errors calling Kind
     */
    private boolean isKindClusterExist(String clusterName) throws Exception {
        var command = "kind get clusters";

        LOGGER.debug("isKindClusterExist request : command [{}]", command);

        var pr = Runtime.getRuntime().exec(command);

        var output = IOUtils.toString(pr.getInputStream(), Charset.defaultCharset()).trim();
        var outputErr = IOUtils.toString(pr.getErrorStream(), Charset.defaultCharset()).trim();

        waitForNormalTermination(pr, DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        // no clusters
        if ("No kind clusters found.".equals(outputErr)) {
            return false;
        }

        if (StringUtils.isEmpty(clusterName)) {
            // there is a cluster that exist
            return true;
        }

        //noinspection RedundantIfStatement
        if (Arrays.stream(output.split("\n")).anyMatch(clusterName::equalsIgnoreCase)) {
            // the output contains cluster name
            return true;
        }

        return false;
    }

    /**
     * Wait for the process to end until a timeout
     *
     * @param  process   the process to run
     * @param  timeout   timeout value
     * @param  unit      time unit to use for timeout
     * @throws Exception if there are errors calling Kind
     */
    private void waitForNormalTermination(Process process, int timeout, TimeUnit unit) throws Exception {

        var stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        var stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // Read the output from the command
        LOGGER.debug("Here is the standard output of the command:\n");
        String value;
        while ((value = stdInput.readLine()) != null) {
            LOGGER.debug(value);
        }

        // Read any errors from the attempted command
        LOGGER.debug("Here is the standard error of the command (if any):\n");
        while ((value = stdError.readLine()) != null) {
            LOGGER.debug(value);
        }

        if (!process.waitFor(timeout, unit)) {
            throw new TimeoutException("Timeout while executing " + process.info().commandLine().orElse(null));
        }

        if (process.exitValue() != 0) {
            throw new Exception("Process termination was abnormal, exit value: [" + process.exitValue() + "], command:[" + process.info().commandLine().orElse(null) + "] error returned:[" + IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8) + ']');
        }
    }

    /**
     * Init Kind cluster with the default configuration
     *
     * @throws Exception if there are errors calling Kind
     */
    public void initKindClusterWithDefaults() throws Exception {
        var resource = KindClusterLauncher.class.getClassLoader().getResource(DEFAULT_CONFIG_FILE);

        if (resource == null) {
            throw new Exception("Configuration in not found in the classpath");
        }

        var configPath = resource.getFile();
        createKindCluster(DEFAULT_IMAGE, DEFAULT_CLUSTER_NAME, new File(configPath).getAbsolutePath(), true);
    }

    /**
     * Create a new namespace in the cluster
     *
     * @param  namespace the namespace to create
     * @throws Exception if there are errors calling Kind
     */
    public void createNamespace(String namespace) throws Exception {
        var command = "kubectl --context " + clusterName + " create namespace " + namespace;

        LOGGER.debug("request : command [{}]", command);

        var pr = Runtime.getRuntime().exec(command);
        waitForNormalTermination(pr, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * Delete a namespace in the cluster
     *
     * @param  namespace the namespace to delete
     * @throws Exception if there are errors calling Kind
     */
    public void deleteNamespace(String namespace) throws Exception {
        var command = "kubectl --context " + clusterName + " delete namespace " + namespace;

        LOGGER.debug("request : command [{}]", command);

        var pr = Runtime.getRuntime().exec(command);
        waitForNormalTermination(pr, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * Recreate a new namespace in the cluster
     *
     * @param  namespace the namespace to create
     * @throws Exception if there are errors calling Kind
     */
    public void recreateNamespace(String namespace) throws Exception {
        // delete namespace if present
        try {
            deleteNamespace(namespace);
        } catch (Exception ignore) {
        }
        // create the namespace
        createNamespace(namespace);
    }

    /**
     * Create a role to see in the namespace in the cluster
     *
     * @param  namespace the namespace to allow
     * @throws Exception if there are errors calling Kind
     */
    public void createRoleInNamespace(String rolename, String namespace) throws Exception {
        var command = "kubectl --context " + clusterName + " create rolebinding " + rolename + " --clusterrole=kubernetes-api-role --serviceaccount=kubeapi:sa-kubernetes-api --namespace=" + namespace;

        LOGGER.debug("request : command [{}]", command);

        var pr = Runtime.getRuntime().exec(command);
        waitForNormalTermination(pr, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }
}
