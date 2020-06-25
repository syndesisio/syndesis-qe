package io.syndesis.qe;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = CucumberSpring.class)
@ComponentScan(basePackages = {"io.syndesis.qe"})
public class CucumberSpring {
}
