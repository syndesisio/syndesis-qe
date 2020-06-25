package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.FHIR;
import io.syndesis.qe.util.fhir.FhirUtils;
import io.syndesis.qe.util.fhir.MyBasicSpecification;
import io.syndesis.qe.util.fhir.MyPatientSpecification;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FhirValidationSteps {
    @Autowired
    private FhirUtils fhirUtils;
    private final DbValidationSteps dbValidationSteps = new DbValidationSteps();

    //    entity PATIENT:
    @Then("validate that patient with name {string} is not in FHIR")
    public void validateFhirDelete(String fullName) {
        MyPatientSpecification ps = splitName(fullName);
        assertThat(fhirUtils.isPatientInFhir(ps)).isFalse();
    }

    @Then("validate that patient with name {string} is in FHIR")
    public void validateThatPatientIsInFHIR(String name) {
        MyPatientSpecification ps = splitName(name);
        assertThat(fhirUtils.isPatientInFhir(ps)).isTrue();
    }

    @Then("delete all relevant entities on FHIR server")
    public void cleanAllRelevantEntitiesOnFHIRServer() {
        fhirUtils.deleteAllPatients();
        fhirUtils.deleteAllBasics();
    }

    @Then("create patient with name {string} on FHIR and put it into DB")
    public void createPatientWithNameOnFHIRServerAndDb(String name) {
        MyPatientSpecification ps = splitName(name);
        ps.setId(fhirUtils.insertPatientToFhir(ps));
        DataTable dt = prepareDataTable(ps);
        dbValidationSteps.insertsIntoTable("CONTACT", dt);
    }

    @Then("validate that last inserted patients name has been changed to {string} in FHIR")
    public void validateThatLastInsertPatientNameHasBeenChangedToInFHIR(String newName) {
        MyPatientSpecification newPs = splitName(newName);
        newPs.setId(fhirUtils.getLastPatientId());
        assertThat(fhirUtils.isPatientInFhir(newPs)).isTrue();
    }

    private MyPatientSpecification splitName(String name) {
        String[] meno = name.split(" ");
        return new MyPatientSpecification(meno[1], meno[0]);
    }

    private DataTable prepareDataTable(MyPatientSpecification ps) {
        List<List<String>> raw = new ArrayList<>();
        List<String> row = new ArrayList<>();
        raw.add(row);
        row.add(ps.getGivenName());
        row.add(ps.getFamilyName());
        row.add("Red Hat");
        row.add(ps.getId());
        return DataTable.create(raw);
    }

    //    entity - BASIC:
    @When("create basic with language {string} on FHIR and put it into DB")
    public void createBasicWithLanguageOnFHIRAndDB(String lang) {
        String basicId = fhirUtils.insertBasicToFhir(lang);
        MyBasicSpecification bs = new MyBasicSpecification(lang, basicId);
        DataTable dt = prepareDataTable(bs);
        dbValidationSteps.insertsIntoTable("TODO WITH ID", dt);
    }

    @Then("validate that last inserted basics language has been changed to {string} in FHIR")
    public void validateThatLastBasicsLanguageHasBeenChangedInFHIR(String lang) {
        MyBasicSpecification newBs = new MyBasicSpecification(lang);
        newBs.setId(fhirUtils.getLastBasicId());
        assertThat(fhirUtils.isBasicInFhir(newBs)).isTrue();
    }

    private DataTable prepareDataTable(MyBasicSpecification bs) {
        List<List<String>> raw = new ArrayList<>();
        List<String> row = new ArrayList<>();
        raw.add(row);
        row.add(bs.getId());
        row.add(bs.getLanguage());
        return DataTable.create(raw);
    }

    @Then("add FHIR account")
    public void addFhirAccount() {
        ResourceFactory.get(FHIR.class).addAccount();
    }
}
