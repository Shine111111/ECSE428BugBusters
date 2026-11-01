package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import ca.mcgill.ecse428.bugbusters.BugBustersApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

// Starts the Spring application once for all Cucumber scenarios so step definitions can
// @Autowired services, repositories and MockMvc (to call the REST endpoints). Uses the in-memory
// H2 database from src/test/resources/application.properties, never Supabase.
// There must be exactly one @CucumberContextConfiguration class in this package.
@CucumberContextConfiguration
@SpringBootTest(classes = BugBustersApplication.class, properties = {
    "spring.config.import=",
    "spring.datasource.url=jdbc:h2:mem:acceptance;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false"
})
public class CucumberSpringConfiguration {
}
