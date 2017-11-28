package io.syndesis.qe.rest.tests;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
		features = "classpath:features",
		format = {"pretty", "html:target/cucumber-report.html", "junit:target/cucumber-junit.html"})
public class CucumberTestsRunner {

	//we could have some setup here

}
