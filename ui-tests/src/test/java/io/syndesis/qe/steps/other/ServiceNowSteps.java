package io.syndesis.qe.steps.other;

import io.syndesis.qe.bdd.validation.ServiceNowValidationSteps;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.SyndesisRootPage;

import java.util.HashMap;
import java.util.Map;

import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;

public class ServiceNowSteps {
    @When("fill in and modify values by element ID")
    public void fillInAndModifyValuesByElementID(DataTable data) {
        Map<String, String> map = new HashMap<>();
        data.asMap(String.class, String.class).forEach((k, v) ->
            map.put(k.toString(), ServiceNowValidationSteps.modifySNNumber(v.toString())));

        System.out.println(map.toString());
        new Form(new SyndesisRootPage().getRootElement()).fillByTestId(map);
    }
}
