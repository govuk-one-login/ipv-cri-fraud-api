package gov.di_ipv_fraud.pages;

import org.openqa.selenium.By;

public class DeviceSelectionPage extends GlobalPage {

    private static final By USER_NOT_ON_MOBILE_DEVICE = By.cssSelector("#select-device-choice-2");
    private static final By CONTINUE_BUTTON = By.xpath("//button[@type='submit']");
    private static final By USER_NOT_ON_IPHONE_OR_ANDROID = By.cssSelector("#smartphone-choice-3");
    private static final By PAGE_TITLE = By.cssSelector("#main-content h1");

    public boolean isDeviceSelectionScreenPresent() {
        return getText(PAGE_TITLE).equals("Are you on a computer or a tablet right now?");
    }

    public void selectNoMobileDeviceAndContinue() {
        clickElement(USER_NOT_ON_MOBILE_DEVICE);
        clickElement(CONTINUE_BUTTON);
    }

    public void selectNoIphoneOrAndroidAndContinue() {
        clickElement(USER_NOT_ON_IPHONE_OR_ANDROID);
        clickElement(CONTINUE_BUTTON);
    }
}
