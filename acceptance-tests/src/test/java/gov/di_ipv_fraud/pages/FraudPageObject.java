package gov.di_ipv_fraud.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import gov.di_ipv_fraud.service.ConfigurationService;
import gov.di_ipv_fraud.utilities.Driver;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static gov.di_ipv_fraud.pages.Headers.CHECKING_YOUR_DETAILS;
import static gov.di_ipv_fraud.pages.Headers.IPV_CORE_STUB;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FraudPageObject extends UniversalSteps {

    private final ConfigurationService configurationService;
    private static final Logger LOGGER = Logger.getLogger(FraudPageObject.class.getName());

    @FindBy(xpath = "//*[@id=\"main-content\"]/p/a/button")
    public WebElement visitCredentialIssuers;

    public static final String IPV_CORE_STUB_ENDPOINT = "ipvCoreStubEndpoint";

    @FindBy(xpath = "//*[@value=\"Fraud CRI Build\"]")
    public WebElement fraudCRIBuild;

    @FindBy(xpath = "//*[@value=\"Fraud CRI Staging\"]")
    public WebElement fraudCRIStaging;

    @FindBy(id = "rowNumber")
    public WebElement selectRow;

    @FindBy(xpath = "//*[@value=\"Fraud CRI Integration\"]")
    public WebElement fraudCRIIntegration;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/form/button")
    public WebElement checkYourDetailsContinue;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details/summary/span")
    public WebElement viewResponse;

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[2]/div/button")
    public WebElement searchButton;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/form/details/summary/span")
    public WebElement whoWeCheckLink;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/form/details/div/p[1]/a")
    public WebElement experianLink;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/form/details/div/p[3]/a")
    public WebElement privacyPolicyLink;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details")
    public WebElement errorResponse;

    @FindBy(xpath = "//*[@id=\"continue\"]")
    public WebElement continueButton;

    @FindBy(xpath = "//*[@class=\"govuk-heading-l\"]")
    public WebElement title;

    @FindBy(id = "name")
    public WebElement usersearchField;

    @FindBy(xpath = "//*[@class=\"govuk-button\"]")
    public WebElement usersearchButton;

    @FindBy(xpath = "//*[@id=\"main-content\"]/table/tbody/tr/td[1]/p[1]/a")
    public WebElement fraudCRILink;

    @FindBy(xpath = "//*[@class=\"govuk-details__summary-text\"]")
    public WebElement secondAddressLink;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details/div/pre")
    public WebElement JSONPayload;

    @FindBy(xpath = "//*[@id=\"main-content\"]/table/tbody/tr/td[1]/p[2]/a")
    public WebElement editUserLink;

    @FindBy(id = "buildingName")
    public WebElement housenameField;

    @FindBy(xpath = "//*[@class=\"govuk-button button\"]")
    public WebElement fraudCRIButton;

    @FindBy(id = "buildingNumber")
    public WebElement housenumberField;

    @FindBy(xpath = "//*[@class=\"govuk-heading-xl\"]")
    public WebElement pagetTitle;

    @FindBy(xpath = "//*[@id=\"postCode\"]")
    public WebElement clearPostcode;

    @FindBy(id = "dateOfBirth-day")
    public WebElement dobDay;

    @FindBy(id = "dateOfBirth-month")
    public WebElement dobMonth;

    @FindBy(id = "dateOfBirth-year")
    public WebElement dobYear;

    @FindBy(id = "firstName")
    public WebElement firstName;

    @FindBy(id = "surname")
    public WebElement surname;

    @FindBy(id = "SecondaryUKAddress.buildingNumber")
    public WebElement secondAddressHousenumberField;

    @FindBy(id = "SecondaryUKAddress.street")
    public WebElement secondAddressStreetnameField;

    @FindBy(id = "SecondaryUKAddress.townCity")
    public WebElement secondAddresssTownOrCityField;

    @FindBy(id = "SecondaryUKAddress.postCode")
    public WebElement secondAddresssPostcodeField;

    @FindBy(id = "SecondaryUKAddress.validUntilDay")
    public WebElement secondAddresssvalidToDayField;

    @FindBy(id = "SecondaryUKAddress.validUntilMonth")
    public WebElement secondAddresssvalidToMonthField;

    @FindBy(id = "SecondaryUKAddress.validUntilYear")
    public WebElement secondAddresssvalidToYearField;

    public FraudPageObject() {
        this.configurationService = new ConfigurationService(System.getenv("ENVIRONMENT"));
        PageFactory.initElements(Driver.get(), this);
    }

    public void navigateToIPVCoreStub() {
        Driver.get().get(configurationService.getCoreStubUrl(true));
        waitForTextToAppear(IPV_CORE_STUB);
    }

    public void navigateToFraudCRIOnTestEnv() {
        visitCredentialIssuers.click();
        String fraudCRITestEnvironment = configurationService.getFraudCRITestEnvironment();
        LOGGER.info("fraudCRITestEnvironment = " + fraudCRITestEnvironment);
        if (fraudCRITestEnvironment.equalsIgnoreCase("Build")) {
            fraudCRIBuild.click();
        } else if (fraudCRITestEnvironment.equalsIgnoreCase("Staging")) {
            fraudCRIStaging.click();
        } else if (fraudCRITestEnvironment.equalsIgnoreCase("Integration")) {
            fraudCRIIntegration.click();
        } else {
            LOGGER.info("No test environment is set");
        }
    }

    public void navigateToFraudCRI(String environment) {
        visitCredentialIssuers.click();
        assertURLContains("credential-issuers");

        switch (environment) {
            case "Build":
                fraudCRIBuild.click();
                break;

            case "Staging":
                fraudCRIStaging.click();
                break;

            case "Integration":
                fraudCRIIntegration.click();
                break;

            default:
                break;
        }
    }

    public void searchForUATUser(String number) {
        assertURLContains("credential-issuer?cri=fraud-cri");
        selectRow.sendKeys(number);
        searchButton.click();
    }

    public void navigateToResponse(String validOrInvalid) {
        waitForTextToAppear(CHECKING_YOUR_DETAILS);
        checkYourDetailsContinue.click();
        assertURLContains("callback");
        if ("Invalid".equalsIgnoreCase(validOrInvalid)) {
            errorResponse.click();
        } else {
            viewResponse.click();
        }
    }

    public void whoWeCheckDetailsWith(String page) {
        waitForTextToAppear(CHECKING_YOUR_DETAILS);
        whoWeCheckLink.click();

        if ("Experian".equalsIgnoreCase(page)) {
            experianLink.click();
            ArrayList<String> newTb = new ArrayList<String>(Driver.get().getWindowHandles());
            Driver.get().switchTo().window(newTb.get(1));
            assertURLContains("https://www.experian.co.uk/");
            Driver.get().close();
            Driver.get().switchTo().window(newTb.get(0));

        } else {
            privacyPolicyLink.click();
            ArrayList<String> newTb = new ArrayList<String>(Driver.get().getWindowHandles());
            Driver.get().switchTo().window(newTb.get(1));
            assertURLContains("privacy-notice");
            Driver.get().close();
            Driver.get().switchTo().window(newTb.get(0));
        }
    }

    public void userSearchByName(String username) {
        assertURLContains("credential-issuer?cri=fraud-cri");
        usersearchField.sendKeys(username);
        usersearchButton.click();
    }

    public void goTofraudCRILink() {
        fraudCRILink.click();
    }

    public void userNameInJsonResponse() throws JsonProcessingException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode vcNode = jsonNode.get("vc");
        JsonNode nameNode = vcNode.get("credentialSubject");
        JsonNode insideName = nameNode.get("name");
        JsonNode nameContent = insideName.get(0);
        LOGGER.info("nameContent = " + nameContent);
    }

    public void jsonErrorResponse(String testStatusCode) throws JsonProcessingException {
        String testErrorDescription = "general error";
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode insideError = jsonNode.get("errorObject");
        LOGGER.info("insideError = " + insideError);
        JsonNode errorDescription = insideError.get("description");
        JsonNode statusCode = insideError.get("httpstatusCode");
        String ActualErrorDescription = insideError.get("description").asText();
        String ActualStatusCode = insideError.get("httpstatusCode").asText();
        LOGGER.info("errorDescription = " + errorDescription);
        LOGGER.info("statusCode = " + statusCode);
        LOGGER.info("testErrorDescription = " + testErrorDescription);
        LOGGER.info("testStatusCode = " + testStatusCode);
        Assert.assertEquals(testErrorDescription, ActualErrorDescription);
        Assert.assertEquals(testStatusCode, ActualStatusCode);
    }

    public void goToPage(String page) {
        waitForTextToAppear(page);
    }

    public void clickContinue() {
        continueButton.isEnabled();
        continueButton.click();
    }

    public void goToResponse(String validOrInvalid) {
        assertURLContains("callback");
        if ("Invalid".equalsIgnoreCase(validOrInvalid)) {
            errorResponse.click();
        } else {
            viewResponse.click();
        }
    }

    public void goToVerifiableCredentialsPage() {
        title.getText();
    }

    public void goToEditUserLink() {
        editUserLink.click();
    }

    public void addHouseName(String housename) {
        housenameField.sendKeys(housename);
    }

    public void clearHouseNumber() {
        housenumberField.clear();
    }

    public void clearFirstname() {
        firstName.clear();
    }

    public void clearSurname() {
        surname.clear();
    }

    public void enterName(String name) {
        String[] names = name.split(" ");
        firstName.sendKeys(names[0]);
        surname.sendKeys(names[1]);
    }

    public void clearDoB() {
        dobDay.clear();
        dobMonth.clear();
        dobYear.clear();
    }

    public void addHouseNumber(String housenumber) {
        housenumberField.sendKeys(housenumber);
        fraudCRIButton.click();
    }

    public void clickSubmit() {
        fraudCRIButton.click();
    }

    public void userHouseNameAndNumber(String testHouseName, String testHouseNumber)
            throws JsonProcessingException {
        JsonNode addressContent = userAddressInJsonResponse();
        LOGGER.info("addressContent = " + addressContent);
        JsonNode houseName = addressContent.get("buildingName");
        JsonNode houseNumber = addressContent.get("buildingNumber");
        String ActualHouseName = addressContent.get("buildingName").asText();
        String ActualHouseNumber = addressContent.get("buildingNumber").asText();
        LOGGER.info("houseName = " + houseName);
        LOGGER.info("houseNumber = " + houseNumber);
        LOGGER.info("testHouseName = " + testHouseName);
        LOGGER.info("testHouseNumber = " + testHouseNumber);
        Assert.assertEquals(testHouseName, ActualHouseName);
        Assert.assertEquals(testHouseNumber, ActualHouseNumber);
    }

    public void ciInVC(String ci) throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode vcNode = jsonNode.get("vc");
        JsonNode evidenceNode = vcNode.get("evidence");

        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        List<JsonNode> evidence = objectReader.readValue(evidenceNode);

        JsonNode cisNode = evidence.get(0).get("ci");

        ObjectReader listReader =
                new ObjectMapper().readerFor(new TypeReference<List<String>>() {});
        List<String> cis = listReader.readValue(cisNode);

        if (StringUtils.isNotEmpty(ci)) {
            if (cis.size() > 0) {
                assertTrue(cis.contains(ci));
            } else {
                fail("No CIs found");
            }
        }
    }

    public void identityScoreIs(String score) throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode vcNode = jsonNode.get("vc");
        JsonNode evidenceNode = vcNode.get("evidence");

        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        List<JsonNode> evidence = objectReader.readValue(evidenceNode);

        String fraudScore = evidence.get(0).get("identityFraudScore").asText();
        assertEquals(fraudScore, score);
    }

    public void goToPageWithTitle(String title) {
        pagetTitle.getText();
    }

    public void removePostcode() {
        clearPostcode.clear();
    }

    public void goTofraudCRILinkAfterEdit() {
        fraudCRIButton.click();
    }

    private JsonNode userAddressInJsonResponse() throws JsonProcessingException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode vcNode = jsonNode.get("vc");
        JsonNode addressNode = vcNode.get("credentialSubject");
        JsonNode insideAddress = addressNode.get("address");
        JsonNode addressContent = insideAddress.get(0);
        return addressContent;
    }

    public void documentNumberInVC(String documentNumber) throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode vcNode = jsonNode.get("vc");
        JsonNode evidenceNode = vcNode.get("drivingPermit");

        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        List<JsonNode> evidence = objectReader.readValue(evidenceNode);

        String licenceNumber = evidence.get(0).get("documentNumber").asText();
        assertEquals(documentNumber, licenceNumber);
    }

    public void selectSecondAddressLink() {
        secondAddressLink.click();
    }

    public void addSecondAddressDetails(List<Map<String, String>> secondAddressDetails) {
        secondAddressHousenumberField.sendKeys(secondAddressDetails.get(0).get("housenumber"));
        secondAddressStreetnameField.sendKeys(secondAddressDetails.get(0).get("streetname"));
        secondAddresssTownOrCityField.sendKeys(secondAddressDetails.get(0).get("townorcity"));
        secondAddresssPostcodeField.sendKeys(secondAddressDetails.get(0).get("postcode"));
    }

    public void addValidFromDate(String day, String month, String year) {
        secondAddresssvalidToDayField.sendKeys(day);
        secondAddresssvalidToMonthField.sendKeys(month);
        secondAddresssvalidToYearField.sendKeys(year);
    }
}