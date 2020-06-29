package io.syndesis.qe.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Util {
    public static void sleep(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            log.error("Sleep was interrupted!");
            e.printStackTrace();
        }
    }
}
