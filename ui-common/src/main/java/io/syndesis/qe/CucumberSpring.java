package io.syndesis.qe;

import org.springframework.test.context.ContextConfiguration;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
//@ComponentScan("io.syndesis.qe")
@ContextConfiguration("classpath:cucumber.xml")
public class CucumberSpring {
}
