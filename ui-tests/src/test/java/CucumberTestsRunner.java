import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import org.openqa.selenium.Dimension;

import com.codeborne.selenide.Configuration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import properties.PropertiesLoader;

/**
 * Created by mastepan on 10/18/17.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		features="src/test/java/features",
		glue={"steps"},
		monochrome=true,
		format={"pretty"}
)
public class CucumberTestsRunner {
	// setup
	@BeforeClass
	public static void setup() {
		//load properties
		PropertiesLoader.load(System.getProperty("propertiesFileAbsolutePath"));
		//set up Selenide
		Configuration.timeout = 60000;
		getWebDriver().manage().window().setSize(new Dimension(1920,1024));
	}
}
