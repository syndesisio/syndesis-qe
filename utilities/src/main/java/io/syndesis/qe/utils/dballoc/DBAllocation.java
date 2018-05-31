package io.syndesis.qe.utils.dballoc;

import java.util.Map;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class DBAllocation {
    private String dbLabel;
    private String uuid;
    private Map<String, String> allocationMap;

    public DBAllocation(String dbLabel) {
        setDbLabel(dbLabel);
    }
}
