package io.fabric8.kubernetes.api.model.apiextensions;

/**
 * Workaround for Syndesis dependency.
 * <p>
 * Test suite uses kubernetes client 4.13 which has this class in different package `io.fabric8.kubernetes.api.model.apiextensions.v1beta1` as the
 * previous one `io.fabric8.kubernetes.api.model.apiextensions`.
 * Syndesis dependency uses the old one which results in `NoClassDefFoundError` since the class is not longer there.
 */
public class CustomResourceDefinition extends io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition {
}
