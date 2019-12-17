package io.syndesis.qe.utils;

import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public enum Alert {

    ALL("all", By.className("alert")),
    SUCCESS("success", By.className("alert-success")),
    WARNING("warning", By.className("alert-warning")),
    DANGER("danger", By.className("alert-danger"));

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
