package de.mattagohni;

import de.mattagohni.metrics.MeterRegistry;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.monitoring.micrometer.MicrometerMetrics;
import io.quarkus.logging.Log;

import java.util.Collections;

@ControllerConfiguration()
public class MessageServerReconciler implements Reconciler<MessageServer>, Cleaner<MessageServer> {
    private final KubernetesClient kubernetesClient;

    public MessageServerReconciler(KubernetesClient kubernetesClient, MeterRegistry meterRegistry) {
        this.kubernetesClient = kubernetesClient;
        MicrometerMetrics.newPerResourceCollectingMicrometerMetricsBuilder(meterRegistry.meterRegistry())
                         .withCleanUpDelayInSeconds(5)
                         .withCleaningThreadNumber(2)
                         .build();
    }

    @Override
    public UpdateControl<MessageServer> reconcile(MessageServer messageServer, Context<MessageServer> context) {
        String name = messageServer.getMetadata().getName();
        String namespace = messageServer.getMetadata().getNamespace();
        String message = messageServer.getSpec().getMessage();
        String path = messageServer.getSpec().getPath();

        Log.info("Reconciling MessageServer " + name + " in namespace " + namespace);

        // Get the existing ConfigMap
        Log.info("Checking if ConfigMap exists...");
        ConfigMap existingConfigMap = kubernetesClient.configMaps().inNamespace(namespace).withName(name).get();

        // Check if the message has changed
        if (existingConfigMap != null && !existingConfigMap.getData().get("index.html").contains(message)) {
            Log.info("Updating ConfigMap...");
            // Update the ConfigMap with the new message
            existingConfigMap.getData().put("index.html", "<html><body><h1>" + message + "</h1></body></html>");
            kubernetesClient.configMaps().inNamespace(namespace).createOrReplace(existingConfigMap);
        } else if (existingConfigMap == null) {
            Log.info("Creating ConfigMap...");
            // Create a new ConfigMap with the message
            ConfigMap configMap = new ConfigMapBuilder()
                    .withNewMetadata().withName(name).endMetadata()
                    .addToData("index.html", "<html><body><h1>" + message + "</h1></body></html>")
                    .build();
            kubernetesClient.configMaps().inNamespace(namespace).createOrReplace(configMap);
        }

        // Check if the Deployment already exists
        Log.info("Checking if Deployment exists...");
        Resource<Deployment> deploymentResource = kubernetesClient.apps().deployments().inNamespace(namespace).withName(name);
        if (deploymentResource.get() == null) {
            Log.info("Creating Deployment...");
            // Create a new Deployment for the Nginx server
            Deployment deployment = new DeploymentBuilder()
                    .withNewMetadata().withName(name).endMetadata()
                    .withNewSpec()
                        .withNewSelector()
                        .addToMatchLabels("app", name)
                        .endSelector()
                        .withNewTemplate()
                            .withNewMetadata()
                                .addToLabels("app", name)
                            .endMetadata()
                            .withNewSpec()
                                .addNewContainer()
                                .withName("nginx")
                                .withImage("nginx:1.14.2")
                                    .addNewVolumeMount()
                                        .withName("html")
                                        .withMountPath("/usr/share/nginx/html")
                                    .endVolumeMount()
                                .endContainer()
                                .addNewVolume()
                                    .withName("html")
                                        .withNewConfigMap()
                                            .withName(name)
                                        .endConfigMap()
                                .endVolume()
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();
            deploymentResource.createOrReplace(deployment);
        }

        // Check if the Service already exists
        Log.info("Checking if Service exists...");
        Resource<Service> serviceResource = kubernetesClient.services().inNamespace(namespace).withName(name);
        if (serviceResource.get() == null) {
            Log.info("Creating Service...");
            // Create a new Service for the Nginx server
            Service service = new ServiceBuilder()
                    .withNewMetadata().withName(name).endMetadata()
                    .withNewSpec()
                        .withSelector(Collections.singletonMap("app", name))
                        .addNewPort()
                            .withProtocol("TCP")
                            .withPort(80)
                            .withTargetPort(new IntOrString(80))
                        .endPort()
                    .endSpec()
                    .build();
            serviceResource.createOrReplace(service);
        }

        // Check if the Ingress already exists
        Log.info("Checking if Ingress exists...");
        Resource<Ingress> ingressResource = kubernetesClient.network().v1().ingresses().inNamespace(namespace).withName(name);
        if (ingressResource.get() == null) {
            Log.info("Creating Ingress...");
            // Create a new Ingress for the Nginx server
            Ingress ingress = new IngressBuilder()
                    .withNewMetadata().withName(name).endMetadata()
                    .withNewSpec()
                        .addNewRule()
                            .withNewHttp()
                            .addNewPath()
                                .withPathType("Prefix")
                                .withPath(path)
                                    .withNewBackend()
                                        .withNewService()
                                            .withName(name)
                                            .withNewPort()
                                                .withNumber(80)
                                            .endPort()
                                        .endService()
                                    .endBackend()
                                .endPath()
                            .endHttp()
                        .endRule()
                    .endSpec()
                    .build();
            ingressResource.createOrReplace(ingress);
        }

        // After creating or updating the resources
        Log.info("Updating MessageServer status...");
        MessageServerStatus status = new MessageServerStatus();
        status.setRunning(true);
        messageServer.setStatus(status);

        return UpdateControl.updateResourceAndPatchStatus(messageServer);
    }

    @Override
    public DeleteControl cleanup(MessageServer messageServer, Context<MessageServer> context) {
        String name = messageServer.getMetadata().getName();
        String namespace = messageServer.getMetadata().getNamespace();

        try {
            Log.info("Deleting resources for MessageServer " + name + " in namespace " + namespace);
            // Delete the ConfigMap
            kubernetesClient.configMaps().inNamespace(namespace).withName(name).delete();
            // delete the Deployment
            kubernetesClient.apps().deployments().inNamespace(namespace).withName(name).delete();
            // delete the Service
            kubernetesClient.services().inNamespace(namespace).withName(name).delete();
            // delete the Ingress
            kubernetesClient.network().v1().ingresses().inNamespace(namespace).withName(name).delete();
        } catch (Exception e) {
            // Log the exception
            Log.error("Failed to delete resources: " + e.getMessage());
        }

        return DeleteControl.defaultDelete();
    }
}