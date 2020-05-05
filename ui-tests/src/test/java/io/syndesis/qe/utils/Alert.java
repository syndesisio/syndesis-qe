package io.syndesis.qe.utils;

import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public enum Alert {

    ALL("all", By.className("pf-c-alert")),
    INFO("info", By.className("pf-m-info")),
    SUCCESS("success", By.className("pf-m-success")),
    WARNING("warning", By.className("pf-m-warning")),
    DANGER("danger", By.className("pf-m-danger"));

    public static final class Element {
        public static final By CLOSE_BUTTON = By.cssSelector("button.pf-c-button");
        public static final By ALERT_TITLE = By.className("pf-c-alert__title");
    }

    private final String name;
    private final By by;
    private static final Map<String, Alert> ALERTS = new HashMap<String, Alert>();

    Alert(String name, By by) {
        this.name = name;
        this.by = by;
    }

    static {
        for (Alert alert : values()) {
            ALERTS.put(alert.getName(), alert);
        }
    }

    public static Map<String, Alert> getALERTS() {
        return ALERTS;
    }
}
