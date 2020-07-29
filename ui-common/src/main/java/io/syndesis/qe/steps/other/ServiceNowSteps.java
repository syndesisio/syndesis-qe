package io.syndesis.qe.steps.other;

import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.utils.ServiceNowUtils;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;

public class ServiceNowSteps {
    @Autowired
    ServiceNowUtils snUtils;

    @When("fill in and modify values by element ID")
    public void fillInAndModifyValuesByElementID(DataTable data) {
        Map<String, String> map = new HashMap<>();
        data.asMap(String.class, String.class).forEach((k, v) -> map.put(k.toString(), snUtils.modifySNNumber(v.toString())));
        new Form(new SyndesisRootPage().getRootElement()).fillByTestId(map);
    }
}
