package io.syndesis.qe.utils.dballoc;


import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class DBAllocatorClient {

    private DBAllocation dbAllocation;

    public void allocate(String label) {
        this.setDbAllocation(DBAllocationManager.allocate(label));
    }


    public void free() {
        DBAllocationManager.free(dbAllocation.getUuid());
    }
}
