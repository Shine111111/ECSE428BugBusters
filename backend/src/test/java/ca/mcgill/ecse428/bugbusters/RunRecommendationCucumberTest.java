
package ca.mcgill.ecse428.bugbusters;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource(
    "gherkinFeatures/ID_021_Get_Personalized_Movie_Recommendations.feature"
)
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "ca.mcgill.ecse428.bugbusters.stepdefinitions"
)
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty,html:build/reports/cucumber/recommendations.html"
)
public class RunRecommendationCucumberTest {
}
