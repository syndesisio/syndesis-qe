package io.syndesis.qe.util.salesforce;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Represent a Salesforce contact.
 *
 * @author jknetl
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Contact {

    @JsonProperty(value = "Id")
    private String id;
    @JsonProperty(value = "FirstName")
    private String firstName;
    @JsonProperty(value = "LastName")
    private String lastname;
    @JsonProperty(value = "Description")
    private String description;
    @JsonProperty(value = "Title")
    private String title;
}
