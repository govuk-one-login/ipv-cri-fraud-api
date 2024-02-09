package gov.di_ipv_fraud.step_definitions;

import gov.di_ipv_fraud.pages.DeviceSelectionPage;
import gov.di_ipv_fraud.pages.ProveYourIdentityGovUkPage;
import io.cucumber.java.en.And;

import java.util.logging.Logger;

public class LoginSteps {

    private final ProveYourIdentityGovUkPage proveYourIdentityGovUkPage =
            new ProveYourIdentityGovUkPage();
    private final DeviceSelectionPage deviceSelectionPage = new DeviceSelectionPage();
    private static final Logger LOGGER = Logger.getLogger(LoginSteps.class.getName());

    @And("clicks continue on the signed into your GOV.UK One Login page")
    public void clicksContinueOnTheSignedIntoYourGOVUKOneLoginPage() {
        proveYourIdentityGovUkPage.waitForPageToLoad();
        try {
            if (deviceSelectionPage.isDeviceSelectionScreenPresent()) {
                deviceSelectionPage.selectNoMobileDeviceAndContinue();
                deviceSelectionPage.selectNoIphoneOrAndroidAndContinue();
            }
        } catch (NullPointerException e) {
            LOGGER.warning(
                    "No environment variable specified, please specify a variable for runs in Integration");
        }
    }
}
