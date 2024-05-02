package de.mattagohni;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

public class MessageServerReconciler implements Reconciler<MessageServer> { 
  private final KubernetesClient client;

  public MessageServerReconciler(KubernetesClient client) {
    this.client = client;
  }

  // TODO Fill in the rest of the reconciler

  @Override
  public UpdateControl<MessageServer> reconcile(MessageServer resource, Context context) {
    // TODO: fill in logic

    return UpdateControl.noUpdate();
  }
}

