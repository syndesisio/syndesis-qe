package io.syndesis.qe.utils.fhir;

import org.hl7.fhir.dstu3.model.Basic;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FhirUtils {
    private MyPatientClient fhirClient = FhirClientManager.getInstance().getClient();
    @Getter
    private String lastPatientId;
    @Getter
    private String lastBasicId;

    //   entity PATIENT:
    public String insertPatientToFhir(MyPatientSpecification ps) {

        Patient patient = new Patient();
        patient.addName().setFamily(ps.getFamilyName()).addGiven(ps.getGivenName());
        checkConnection();
        MethodOutcome outcome = fhirClient.createPatient(patient);
        lastPatientId = this.extractSimpleId(outcome.getId().getValue(), FhirEntity.PATIENT.getName());
        log.info("Patient *{}* has been inserted with id:*{}*", ps.toString(), lastPatientId);
        return lastPatientId;
    }

    public boolean isPatientInFhir(MyPatientSpecification ps) {
        List<Patient> patients;
        if (ps.getId() != null && getPatientById(ps.getId()) == null) {
            return false;
        }
        patients = getPatientsByFamilyName(ps.getFamilyName());
        if (patients != null && !patients.isEmpty()) {
            return patients.stream().filter(p -> ps.getGivenName().equals(p.getName().get(0).getGiven().get(0).getValue())).findAny().isPresent();
        }
        return false;
    }

    private Patient getPatientById(String id) {
        IdType idtp = new IdType(id);
        try {
            checkConnection();
            return fhirClient.getPatientById(idtp);
        } catch (ResourceNotFoundException | ResourceGoneException e) {
            return null;
        }
    }

    private List<Patient> getPatientsByFamilyName(String fn) {
        checkConnection();
        return fhirClient.getPatientsByFamilyName(fn);
    }

    public void deleteAllPatients() {
        checkConnection();
        List<Patient> patients = fhirClient.getAllPatients();
        patients.forEach(patient -> deleteOnePatient(patient));
    }

    private void deleteOnePatient(Patient patient) {
        IdType idtp = new IdType(extractSimpleId(patient.getId(), FhirEntity.PATIENT.getName()));
        checkConnection();
        fhirClient.deletePatientById(idtp);
    }

    //    entity: BASIC
    public String insertBasicToFhir(String language) {

        Basic basic = new Basic();
        basic.setLanguage(language);
        checkConnection();
        MethodOutcome outcome = fhirClient.createBasic(basic);
        lastBasicId = this.extractSimpleId(outcome.getId().getValue(), FhirEntity.BASIC.getName());
        log.info("Basic with language *{}* has been inserted with id:*{}*", language, lastBasicId);
        return lastBasicId;
    }

    public boolean isBasicInFhir(MyBasicSpecification bs) {
        List<Basic> basics;
        if (bs.getId() != null && getBasicById(bs.getId()) == null) {
            return false;
        }
        basics = getBasicsByLanguage(bs.getLanguage());
        if (basics != null && !basics.isEmpty()) {
            return basics.stream().filter(b -> bs.getLanguage().equals(b.getLanguage())).findAny().isPresent();
        }
        return false;
    }

    private Basic getBasicById(String id) {
        IdType idtp = new IdType(id);
        try {
            checkConnection();
            return fhirClient.getBasicById(idtp);
        } catch (ResourceNotFoundException | ResourceGoneException e) {
            return null;
        }
    }

    private List<Basic> getBasicsByLanguage(String lang) {
        return fhirClient.getBasicByLanguage(lang);
    }

    public void deleteAllBasics() {
        checkConnection();
        List<Basic> basics = fhirClient.getAllBasics();
        basics.forEach(basic -> deleteOneBasic(basic));
    }

    public void deleteOneBasic(Basic basic) {
        IdType idtp = new IdType(extractSimpleId(basic.getId(), FhirEntity.BASIC.getName()));
        checkConnection();
        fhirClient.deleteBasicById(idtp);
    }

    //    COMMON:
    private String extractSimpleId(String fullId, String entity) {

        final String pattern = String.format("%s/(\\d{1,10})", entity);
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(fullId);

        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            sb.append(m.group(1));
        }
        return sb.toString();
    }

    /**
     * Very often connections ends with "FhirClientConnectionException: Failed to parse response from server when performing GET to URL...".
     * Todo - fo be fixed
     */
    private void checkConnection() {
        try {
            fhirClient.getPatientsByFamilyName("Mrdar");
            log.info("**************** client renewed! ****************");
        } catch (FhirClientConnectionException ex) {
            fhirClient = FhirClientManager.getInstance().getClient();
        }
    }
}
