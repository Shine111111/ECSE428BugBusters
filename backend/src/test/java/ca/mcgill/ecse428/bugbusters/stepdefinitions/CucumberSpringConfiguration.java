package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

// Starts the Spring application once for all Cucumber scenarios so step definitions can
// @Autowired services and repositories. Uses the in-memory H2 database from
// src/test/resources/application.properties, never Supabase.
// There must be exactly one @CucumberContextConfiguration class in this package.
@CucumberContextConfiguration
@SpringBootTest
public class CucumberSpringConfiguration {
}
