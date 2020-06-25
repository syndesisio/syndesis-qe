package io.syndesis.qe.util.fhir;

import org.hl7.fhir.dstu3.model.Basic;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;

import java.util.List;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public interface MyPatientClient extends IGenericClient {

    //entity Patient:
    @Create
    public MethodOutcome createPatient(@ResourceParam Patient patient);

    @Search
    public List<Patient> getPatientsByFamilyName(@RequiredParam(name = "family") String familyName);

    @Search
    public List<Patient> getPatientsByGivenName(@RequiredParam(name = "given") String givenName);

    @Search
    public List<Patient> getAllPatients();

    @Read
    public Patient getPatientById(@IdParam IdType id);

    @Delete(type = Patient.class)
    public MethodOutcome deletePatientById(@IdParam IdType theId);

    //entity Basic:
    @Create
    public MethodOutcome createBasic(@ResourceParam Basic basic);

    @Delete(type = Basic.class)
    public MethodOutcome deleteBasicById(@IdParam IdType theId);

    @Read
    public Basic getBasicById(@IdParam IdType id);

    @Search
    public List<Basic> getAllBasics();

    @Search
    public List<Basic> getBasicByLanguage(@RequiredParam(name = "_language") String language);
}
