package com.comact.iep.api.kubernetes;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;


import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comact.iep.api.kubernetes.service.ConfigMapsService;
import io.fabric8.kubernetes.api.model.ConfigMap;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Config maps.
 */
@Slf4j
@Path("/configMap")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"serviceAccount", "user"}) // only a user with this role can have access
public class ConfigMaps {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMaps.class);

    /**
     * The Config maps service.
     */
    @Inject
    ConfigMapsService configMapsService;

    /**
     * Config maps list.
     *
     * @return the list
     */
    @GET
    @Operation(summary = "return configmaps in default namespace", description = "return configmaps in default namespace")
    @Path("/")
    public List<ConfigMap> configMaps() {
        return configMapsService.configMaps();
    }

    /**
     * Config maps within namespace.
     *
     * @param namespace the namespace
     * @return the list
     */
    @GET
    @Operation(summary = "return configmaps in namespace", description = "return configmaps in namespace")
    @Path("/namespace/{namespace}")
    public List<ConfigMap> configMapsWithNamespace(@PathParam("namespace") String namespace) {
        return configMapsService.configMapsWithNamespace(namespace);
    }

    /**
     * Config maps within namespace with label.
     *
     * @param namespace the namespace
     * @param label     the label
     * @return the list
     */
    @GET
    @Operation(summary = "return configmaps with label in namespace", description = "return configmaps with label in namespace")
    @Path("/namespace/{namespace}/label/{label}")
    public List<ConfigMap> configMapsWithNamespaceWithLabel(@PathParam("namespace") String namespace, @PathParam("label") String label) {
        return configMapsService.configMapsWithNamespaceWithLabel(namespace, label);
    }

    /**
     * Config maps within namespace with label value.
     *
     * @param namespace  the namespace
     * @param label      the label
     * @param labelValue the label value
     * @return the list
     */
    @GET
    @Operation(summary = "return configmaps with label value in namespace", description = "return configmaps with label value in namespace")
    @Path("/namespace/{namespace}/label/{label}/{labelValue}")
    public List<ConfigMap> configMapsWithNamespaceWithLabelValue(@PathParam("namespace") String namespace, @PathParam("label") String label, @PathParam("labelValue") String labelValue) {
        return configMapsService.configMapsWithNamespaceWithLabelValue(namespace, label, labelValue);
    }

    /**
     * Create config map.
     *
     * @param namespace the namespace
     * @param configMap the config map
     * @return the config map
     */
    @POST
    @Operation(summary = "create a configMap in the namespace", description = "create a configMap in the namespace")
    @Path("/{namespace}")
    public ConfigMap create(@PathParam("namespace") String namespace, ConfigMap configMap) {
        return configMapsService.create(namespace, configMap);
    }

    /**
     * Update config map.
     *
     * @param namespace the namespace
     * @param configMap the config map
     * @return the config map
     */
    @PUT
    @Operation(summary = "update a configMap in the namespace", description = "update a configMap in the namespace")
    @Path("/{namespace}")
    public ConfigMap update(@PathParam("namespace") String namespace, ConfigMap configMap) {
        return configMapsService.update(namespace, configMap);
    }

    /**
     * Delete resource.
     *
     * @param namespace the namespace
     * @param configMap the config map
     * @return the boolean
     */
    @DELETE
    @Operation(summary = "delete a configMap in the namespace", description = "delete a configMap in the namespace")
    @Path("/{namespace}")
    public boolean delete(@PathParam("namespace") String namespace, ConfigMap configMap) {
        return configMapsService.delete(namespace, configMap);
    }

    /**
     * Delete resource.
     *
     * @param namespace the namespace
     * @param name      the name
     * @return the boolean
     */
    @DELETE
    @Operation(summary = "delete a configMap by name in the namespace", description = "delete a configMap by name in the namespace")
    @Path("/{namespace}/{name}")
    public boolean delete(@PathParam("namespace") String namespace, @PathParam("name") String name) {
        return configMapsService.delete(namespace, name);
    }

    /**
     * Config maps within namespace with name config map.
     *
     * @param namespace the namespace
     * @param name      the name
     * @return the config map
     */
    @GET
    @Operation(summary = "return configmaps with name in namespace", description = "return configmaps with name in namespace")
    @Path("/namespace/{namespace}/name/{name}")
    public ConfigMap configMapsWithNamespaceWithName(@PathParam("namespace") String namespace, @PathParam("name") String name) {
        return configMapsService.configMapsWithNamespaceWithName(namespace, name);
    }

    /**
     * Add annotation.
     *
     * @param namespace       the namespace
     * @param name            the name
     * @param annotationKey   the annotation key
     * @param annotationValue the annotation value
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/namespace/{namespace}/name/{name}/annotation/{key}/{value}")
    public void addAnnotation(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("key") String annotationKey, @PathParam("value") String annotationValue) {
        configMapsService.addAnnotation(namespace, name, annotationKey, annotationValue);
    }

    /**
     * Add label.
     *
     * @param namespace  the namespace
     * @param name       the name
     * @param labelKey   the label key
     * @param labelValue the label value
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/namespace/{namespace}/name/{name}/label/{key}/{value}")
    public void addLabel(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("key") String labelKey, @PathParam("value") String labelValue) {
        configMapsService.addLabel(namespace, name, labelKey, labelValue);
    }

    /**
     * Find resources by annotation.
     *
     * @param namespace     the namespace
     * @param annotationKey the annotation key
     * @return the list
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/namespace/{namespace}/annotation/{key}")
    public List<ConfigMap> findByAnnotation(@PathParam("namespace") String namespace, @PathParam("key") String annotationKey) {
        return configMapsService.findByAnnotation(namespace, annotationKey);
    }

    /**
     * Find resources by annotation.
     *
     * @param namespace       the namespace
     * @param annotationKey   the annotation key
     * @param annotationValue the annotation value
     * @return the list
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/namespace/{namespace}/annotation/{key}/{value}")
    public List<ConfigMap> findByAnnotation(@PathParam("namespace") String namespace, @PathParam("key") String annotationKey, @PathParam("value") String annotationValue) {
        return configMapsService.findByAnnotation(namespace, annotationKey, annotationValue);
    }

    /**
     * Find resources by annotations
     *
     * @param namespace   the namespace
     * @param annotations the annotations
     * @return the list
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/namespace/{namespace}/findByAnnotations")
    public List<ConfigMap> findByAnnotations(@PathParam("namespace") String namespace, Map<String, String> annotations) {
        return configMapsService.findByAnnotations(namespace, annotations);
    }

    /**
     * Find resources by labels.
     *
     * @param namespace the namespace
     * @param labels    the labels
     * @return the list
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/namespace/{namespace}/findByLabels")
    public List<ConfigMap> findByLabels(@PathParam("namespace") String namespace, Map<String, String> labels) {
        return configMapsService.findByLabels(namespace, labels);
    }

    /**
     * Find resources by label.
     *
     * @param namespace the namespace
     * @param labelKey  the label key
     * @return the list
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/namespace/{namespace}/label/{key}")
    public List<ConfigMap> findByLabel(@PathParam("namespace") String namespace, @PathParam("key") String labelKey) {
        return configMapsService.findByLabel(namespace, labelKey);
    }

    /**
     * Find resources by label.
     *
     * @param namespace  the namespace
     * @param labelKey   the label key
     * @param labelValue the label value
     * @return the list
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/namespace/{namespace}/label/{key}/{value}")
    public List<ConfigMap> findByLabel(@PathParam("namespace") String namespace, @PathParam("key") String labelKey, @PathParam("value") String labelValue) {
        return configMapsService.findByLabel(namespace, labelKey, labelValue);
    }
}