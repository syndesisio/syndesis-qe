package io.syndesis.qe.resource;

import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceFactory {
    private static List<Resource> createdResources = new ArrayList<>();

    /**
     * Gets (or creates and gets) the instance of given class and calls its the deploy method.
     * @param clazz class to create
     * @param <T> type
     */
    public static <T extends Resource> void create(Class<T> clazz) {
        get(clazz).deploy();
    }

    public static <T extends Resource> void destroy(Class<T> clazz) {
        get(clazz).undeploy();
    }

    /**
     * Gets (or creates and gets) the instance of given class and returns it.
     * @param clazz class to create
     * @param <T> type
     * @return instance of given class
     */
    public static <T extends Resource> T get(Class<T> clazz) {
        Optional<Resource> oExtRes = createdResources.stream().filter(clazz::isInstance).findAny();
        if (oExtRes.isPresent()) {
            log.debug("Returning previously created instance of " + clazz.getName());
            return (T) oExtRes.get();
        } else {
            log.info("Creating a new instance of " + clazz.getName());
            T instance = null;
            try {
                instance = clazz.newInstance();
                createdResources.add(instance);
                return instance;
            } catch (Exception e) {
                fail("Unable to create instance of " + clazz.getName());
            }
            return instance;
        }
    }

    /**
     * Calls the undeploy method on all instances created by the factory.
     */
    public static void cleanup() {
        createdResources.forEach(created -> {
            log.info("Undeploying resource " + created.getClass().getName());
            created.undeploy();
        });
    }
}
