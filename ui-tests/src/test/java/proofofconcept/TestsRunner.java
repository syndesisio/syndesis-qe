package proofofconcept;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * Created by mastepan on 10/18/17.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		features="src/test/java/proofofconcept/features",
		glue={"proofofconcept/steps"},
		monochrome=true,
		format={"pretty"}
)
public class TestsRunner {
	//selenide setup
	@BeforeClass
	public static void setup() {
		Configuration.timeout = 60000;
	}
}
