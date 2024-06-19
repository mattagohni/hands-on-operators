package de.mattagohni;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusHandler;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusUpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.MaxReconciliationInterval;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.processing.event.rate.RateLimited;
import io.quarkus.logging.Log;

import java.util.concurrent.TimeUnit;


@ControllerConfiguration(maxReconciliationInterval = @MaxReconciliationInterval(interval = 3600L, timeUnit = TimeUnit.SECONDS))
@RateLimited(maxReconciliations = 3, within = 30)
public class IngressReconciler implements Reconciler<Ingress>, ErrorStatusHandler<Ingress> {

    @Override
    public UpdateControl<Ingress> reconcile(Ingress resource, Context<Ingress> context) {

        if (resource.getMetadata().getName().startsWith("cm-acme")) {
            return UpdateControl.noUpdate();
        }

        String hostname;

        hostname = resource.getMetadata().getName();
        Log.infof("in ingress reconciler for %s", hostname);
        return UpdateControl.noUpdate();
    }


    @Override
    public ErrorStatusUpdateControl<Ingress> updateErrorStatus(Ingress resource, Context<Ingress> context, Exception e) {
        return ErrorStatusUpdateControl.updateStatus(resource);
    }
}
