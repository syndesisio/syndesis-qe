package io.syndesis.qe.rest;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.syndesis.qe.rest.tests.ClientTest;
import io.syndesis.qe.templates.SyndesisTemplate;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ClientTest.class
})
public class RestTestSuite {


	@BeforeClass
	public static void prepareEnvironment() {
		SyndesisTemplate.deploy();
	}

}
