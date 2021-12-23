package com.comact.iep.api.kubernetes.service;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Config maps service.
 */
@Slf4j
@ApplicationScoped
public class ConfigMapsService {
    private final KubernetesClient kubernetesClient;

    /**
     * Instantiates a new Config maps service.
     *
     * @param kubernetesClient the kubernetes client
     */
    public ConfigMapsService(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    /**
     * Config maps list.
     *
     * @return the list
     */
    public List<ConfigMap> configMaps() {
        return kubernetesClient.configMaps().list().getItems();
    }

    /**
     * Config maps within namespace.
     *
     * @param namespace the namespace
     * @return the list
     */
    public List<ConfigMap> configMapsWithNamespace(String namespace) {
        return kubernetesClient.configMaps().inNamespace(namespace).list().getItems();
    }

    /**
     * Config maps within namespace with label.
     *
     * @param namespace the namespace
     * @param label     the label
     * @return the list
     */
    public List<ConfigMap> configMapsWithNamespaceWithLabel(String namespace, String label) {
        return kubernetesClient.configMaps().inNamespace(namespace).withLabel(label).list().getItems();
    }

    /**
     * Config maps within namespace with label value.
     *
     * @param namespace  the namespace
     * @param label      the label
     * @param labelValue the label value
     * @return the list
     */
    public List<ConfigMap> configMapsWithNamespaceWithLabelValue(String namespace, String label, String labelValue) {
        return kubernetesClient.configMaps().inNamespace(namespace).withLabel(label, labelValue).list().getItems();
    }

    /**
     * Create config map.
     *
     * @param namespace the namespace
     * @param configMap the config map
     * @return the config map
     */
    public ConfigMap create(String namespace, ConfigMap configMap) {
        return kubernetesClient.configMaps().inNamespace(namespace).create(configMap);
    }

    /**
     * Update config map.
     *
     * @param namespace the namespace
     * @param configMap the config map
     * @return the config map
     */
    public ConfigMap update(String namespace, ConfigMap configMap) {
        return kubernetesClient.configMaps().inNamespace(namespace).createOrReplace(configMap);
    }

    /**
     * Delete resource.
     *
     * @param namespace the namespace
     * @param configMap the config map
     * @return the boolean
     */
    public boolean delete(String namespace, ConfigMap configMap) {
        return kubernetesClient.configMaps().inNamespace(namespace).delete(configMap);
    }

    /**
     * Delete resource.
     *
     * @param namespace the namespace
     * @param name      the name
     * @return the boolean
     */
    public boolean delete(String namespace, String name) {
        var configMap = configMapsWithNamespaceWithName(namespace, name);
        if (configMap == null) {
            log.error("ConfigMap [{}] in namespace [{}] is not found", name, namespace);
            return false;
        }
        return kubernetesClient.configMaps().inNamespace(namespace).delete(configMap);
    }

    /**
     * Config maps within namespace with name config map.
     *
     * @param namespace the namespace
     * @param name      the name
     * @return the config map
     */
    public ConfigMap configMapsWithNamespaceWithName(String namespace, String name) {
        return kubernetesClient.configMaps().inNamespace(namespace).withName(name).get();
    }

    /**
     * Add annotation.
     *
     * @param namespace       the namespace
     * @param name            the name
     * @param annotationKey   the annotation key
     * @param annotationValue the annotation value
     */
    public void addAnnotation(String namespace, String name, String annotationKey, String annotationValue) {
        var scalableResource = kubernetesClient.configMaps().inNamespace(namespace).withName(name);

        // get resources
        var resource = scalableResource.get();

        if (resource != null) {
            // add annotation
            if (resource.getMetadata().getAnnotations() == null) {
                resource.getMetadata().setAnnotations(new HashMap<>());
            }
            resource.getMetadata().getAnnotations().put(annotationKey, annotationValue);

            // save resource
            scalableResource.replace(resource);
        }
    }

    /**
     * Add label.
     *
     * @param namespace  the namespace
     * @param name       the name
     * @param labelKey   the label key
     * @param labelValue the label value
     */
    public void addLabel(String namespace, String name, String labelKey, String labelValue) {
        var scalableResource = kubernetesClient.configMaps().inNamespace(namespace).withName(name);

        // get resources
        var resource = scalableResource.get();

        if (resource != null) {
            // add label
            if (resource.getMetadata().getLabels() == null) {
                resource.getMetadata().setLabels(new HashMap<>());
            }

            resource.getMetadata().getLabels().put(labelKey, labelValue);

            // save resource
            scalableResource.replace(resource);
        }
    }

    /**
     * Find resources by annotation.
     *
     * @param namespace     the namespace
     * @param annotationKey the annotation key
     * @return the list
     */
    public List<ConfigMap> findByAnnotation(String namespace, String annotationKey) {
        // get resources
        var resources = kubernetesClient.configMaps().inNamespace(namespace).list();

        return resources.getItems().stream()
                .filter(configMap -> configMap.getMetadata().getAnnotations() != null &&
                        configMap.getMetadata().getAnnotations().containsKey(annotationKey))
                .collect(Collectors.toList());
    }

    /**
     * Find resources by annotation.
     *
     * @param namespace       the namespace
     * @param annotationKey   the annotation key
     * @param annotationValue the annotation value
     * @return the list
     */
    public List<ConfigMap> findByAnnotation(String namespace, String annotationKey, String annotationValue) {
        // get resources
        var resources = kubernetesClient.configMaps().inNamespace(namespace).list();

        return resources.getItems().stream()
                .filter(configMap -> configMap.getMetadata().getAnnotations() != null &&
                        configMap.getMetadata().getAnnotations().containsKey(annotationKey) &&
                        configMap.getMetadata().getAnnotations().get(annotationKey).equals(annotationValue))
                .collect(Collectors.toList());
    }

    /**
     * Find resources by annotations
     *
     * @param namespace   the namespace
     * @param annotations the annotations
     * @return the list
     */
    public List<ConfigMap> findByAnnotations(String namespace, Map<String, String> annotations) {
        // get resources
        var resources = kubernetesClient.configMaps().inNamespace(namespace).list();

        return resources.getItems().stream()
                .filter(configMap -> configMap.getMetadata().getAnnotations() != null &&
                        configMap.getMetadata().getAnnotations().entrySet().containsAll(annotations.entrySet()))
                .collect(Collectors.toList());
    }

    /**
     * Find resources by labels.
     *
     * @param namespace the namespace
     * @param labels    the labels
     * @return the list
     */
    public List<ConfigMap> findByLabels(String namespace, Map<String, String> labels) {
        // get resources
        var resources = kubernetesClient.configMaps().inNamespace(namespace).withLabels(labels).list();

        return new ArrayList<>(resources.getItems());
    }

    /**
     * Find resources by label.
     *
     * @param namespace the namespace
     * @param labelKey  the label key
     * @return the list
     */
    public List<ConfigMap> findByLabel(String namespace, String labelKey) {
        // get resources
        var resources = kubernetesClient.configMaps().inNamespace(namespace).list();

        return resources.getItems().stream()
                .filter(configMap -> configMap.getMetadata().getLabels() != null &&
                        configMap.getMetadata().getLabels().containsKey(labelKey))
                .collect(Collectors.toList());
    }

    /**
     * Find resources by label.
     *
     * @param namespace  the namespace
     * @param labelKey   the label key
     * @param labelValue the label value
     * @return the list
     */
    public List<ConfigMap> findByLabel(String namespace, String labelKey, String labelValue) {
        // get resources
        var resources = kubernetesClient.configMaps().inNamespace(namespace).list();

        return resources.getItems().stream()
                .filter(configMap -> configMap.getMetadata().getLabels() != null &&
                        configMap.getMetadata().getLabels().containsKey(labelKey) &&
                        configMap.getMetadata().getLabels().get(labelKey).equals(labelValue))
                .collect(Collectors.toList());
    }
}