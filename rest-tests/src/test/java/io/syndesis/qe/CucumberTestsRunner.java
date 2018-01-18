package io.syndesis.qe;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
	features = "classpath:features",
	format = {"pretty", "html:target/cucumber/cucumber-html", "junit:target/cucumber/cucumber-junit.xml", "json:target/cucumber/cucumber-report.json"})
public class CucumberTestsRunner extends TestSuiteParent {

	//we could have some setup here

}
