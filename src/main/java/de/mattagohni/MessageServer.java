package de.mattagohni;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("k8s.mattagohni.de")
public class MessageServer extends CustomResource<MessageServerSpec, MessageServerStatus> implements Namespaced {}

