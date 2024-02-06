package gov.di_ipv_fraud.step_definitions;

import gov.di_ipv_fraud.pages.UniversalSteps;
import io.cucumber.java.en.And;

import static gov.di_ipv_fraud.utilities.BrowserUtils.changeLanguageTo;
import static gov.di_ipv_fraud.utilities.BrowserUtils.setFeatureSet;
import static gov.di_ipv_fraud.utilities.TestUtils.getProperty;

public class UniversalStepDefs extends UniversalSteps {

    @And("The test is complete and I close the driver")
    public void closeDriver() {
        driverClose();
    }

    @And("^I add a cookie to change the language to (.*)$")
    public void iAddACookieToChangeTheLanguageToWelsh(String language) {
        changeLanguageTo(language);
    }

    @And("^I set the crosscore version$")
    public void iSetTheCrosscoreVersion() {
        if (getProperty("cucumber.tags").equals("@V2")) {
            setFeatureSet("crosscoreV2");
        }
    }
}
