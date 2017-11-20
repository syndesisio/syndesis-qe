package io.syndesis.qe.salesforce;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * SF lead DTO.
 *
 * Nov 2, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Lead {

	@JsonProperty(value = "Id")
	private String id;
	@JsonProperty(value = "FirstName")
	private String firstName;
	@JsonProperty(value = "LastName")
	private String lastName;
	@JsonProperty(value = "Email")
	private String email;
	@JsonProperty(value = "Company")
	private String company;
}
