package gov.di_ipv_fraud.step_definitions;

import com.google.common.collect.ImmutableMap;
import gov.di_ipv_fraud.utilities.Driver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.ByteArrayInputStream;

import static com.github.automatedowl.tools.AllureEnvironmentWriter.allureEnvironmentWriter;

public class Hooks {

    @Before("@DVLADrivingLicence_test")
    public void setUp() {
        Capabilities capabilities = ((RemoteWebDriver) Driver.get()).getCapabilities();
        allureEnvironmentWriter(
                ImmutableMap.<String, String>builder()
                        .put("Browser", capabilities.getBrowserName())
                        .put("Browser Version", capabilities.getBrowserVersion())
                        //                        .put("Environment", System.getenv("ENV"))
                        .build());
    }

    @After("@DVLADrivingLicence_test")
    public void onFailure(Scenario scenario) {
        if (scenario.isFailed()) {
            Allure.addAttachment(
                    "Screenshot",
                    new ByteArrayInputStream(
                            ((TakesScreenshot) Driver.get()).getScreenshotAs(OutputType.BYTES)));
        }
    }
}
