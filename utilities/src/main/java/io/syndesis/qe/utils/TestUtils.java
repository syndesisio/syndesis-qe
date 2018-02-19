package io.syndesis.qe.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.syndesis.model.action.Action;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.IntegrationDeploymentState;
import io.syndesis.qe.endpoints.IntegrationOverviewEndpoint;
import io.syndesis.qe.model.IntegrationOverview;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jknetl
 */
@Slf4j
public final class TestUtils {

    private TestUtils() {
    }

    /**
     * Finds an Action of a given connector.
     *
     * TODO(tplevko): Rework this, when all connectors will be unified to follow the new writing style.
     *
     * @param connector
     * @param connectorPrefix
     * @return Action with given prefix or null if no such action can be found.
     */
    public static Action findConnectorAction(Connector connector, String connectorPrefix) {
        Optional<ConnectorAction> action;
        action = connector.getActions()
                .stream()
                .filter(a -> a.getId().get().toString().contains(connectorPrefix))
                .findFirst();

        if (!action.isPresent()) {
            action = connector.getActions()
                    .stream()
                    .filter(a -> a.getDescriptor().getCamelConnectorPrefix().contains(connectorPrefix))
                    .findFirst();
        }

        return action.get();
    }

    /**
     * Waits until a predicate is true or timeout exceeds.
     *
     * @param predicate predicate
     * @param supplier supplier of values to test by predicate
     * @param unit TimeUnit for timeout
     * @param timeout how long to wait for event
     * @param sleepUnit TimeUnit of sleep interval between tests
     * @param sleepTime how long to wait between individual tests (in miliseconds)
     * @param <T> Type of tested value by a predicate
     * @return True if predicate become true within a timeout, otherwise returns false.
     */
    public static <T> boolean waitForEvent(Predicate<T> predicate, Supplier<T> supplier, TimeUnit unit, long timeout, TimeUnit sleepUnit, long sleepTime) {

        final long start = System.currentTimeMillis();
        long elapsed = 0;
        while (!predicate.test(supplier.get()) && unit.toMillis(timeout) >= elapsed) {
            try {
                sleepUnit.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.debug("Interupted while sleeping", e);
            } finally {
                elapsed = System.currentTimeMillis() - start;
            }
        }

        return predicate.test(supplier.get());
    }

    public static boolean waitForPublishing(IntegrationOverviewEndpoint e, IntegrationOverview i, TimeUnit unit, long timeout) {

        return waitForState(e, i, IntegrationDeploymentState.Published, unit, timeout);
    }

    /**
     * Waits until integration reaches a specified state or timeout exceeds.
     *
     * @param e Integration endpoint to obtain current state
     * @param i integration
     * @param state desired integration state
     * @param unit Time unit
     * @param timeout timeout
     * @return True if integration is activated within a timeout. False otherwise.
     */
    public static boolean waitForState(IntegrationOverviewEndpoint e, IntegrationOverview i, IntegrationDeploymentState state, TimeUnit unit, long timeout) {

        return waitForEvent(
                //                integration -> integration.getCurrentStatus().orElse(IntegrationDeploymentState.Pending) == state,
                integration -> integration.getCurrentState() == state,
                () -> getIntegration(e, i).orElse(i),
                unit,
                timeout,
                TimeUnit.SECONDS,
                10
        );
    }

    private static Optional<IntegrationOverview> getIntegration(IntegrationOverviewEndpoint e, IntegrationOverview i) {
        return Optional.of(e.getOverview());
    }

    public static LocalPortForward createLocalPortForward(String podName, int remotePort, int localPort) {
        final Pod podToForward = OpenShiftUtils.getInstance().findComponentPod(podName);
        final LocalPortForward lpf = OpenShiftUtils.getInstance().portForward(podToForward, remotePort, localPort);
        return lpf;
    }

    public static void terminateLocalPortForward(LocalPortForward lpf) {
        if (lpf == null) {
            return;
        }
        if (lpf.isAlive()) {
            try {
                lpf.close();
            } catch (IOException ex) {
                log.error("Error: " + ex);
            }
        } else {
            log.info("Local Port Forward already closed.");
        }
    }

    /**
     * Creates map from objects.
     *
     * @param values key1, value1, key2, value2, ...
     * @return
     */
    public static Map<String, String> map(Object... values) {
        final HashMap<String, String> rc = new HashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            rc.put(values[i].toString(), values[i + 1].toString());
        }
        return rc;
    }
}
