package io.syndesis.qe.utils.dballoc;

import org.jboss.qa.dballoc.api.allocator.DbAllocatorResource;
import org.jboss.qa.dballoc.api.allocator.entity.JaxbAllocation;
import org.jboss.qa.dballoc.client.RestClientFactory;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class DBAllocatorClient {

    private String dbLabel;

    private JaxbAllocation dbAllocation;

    private final DbAllocatorResource resource;

    private static final String API = "http://dballocator.mw.lab.eng.bos.redhat.com:8080/allocator-rest/api/";
    private static final String REQUESTEE = "Syndesis Transaction Test";
    private static final int expire = 60;

    public DBAllocatorClient() {
        resource = RestClientFactory.getDbAllocatorResourceRestClient(API);
    }

    public void allocate(String label) {
        this.setDbLabel(label);
        this.setDbAllocation(resource.allocate(label, REQUESTEE, expire, true));
    }

    public void free() {
        resource.free(dbAllocation.getUuid().toString());
    }
}
