@proveYourIdentity_fullJourney
Feature: Prove Your Identity Full Journey

  Background:
    Given I navigate to the Orchestrator Stub
    And The user chooses the environment STAGING from dropdown
    And I click on Full journey route and Continue
    And I click on `Continue to prove your identity this way` radio button
    And clicks continue on the signed into your GOV.UK One Login page
    When I select the radio option UK Passport and click on Continue
    And I enter Passport Details
      | Passport number | Surname | First name |
      | 321654987       | DECERQUEIRA | KENNETH |
    And I enter Date of birth as 08/07/1965
    And I enter Passport Expiry date as 01/01/2030 and Continue
    And I enter BA2 5AA in the Postcode field and find address
    And the user chooses their address 8 HADLEY ROAD, BATH, BA2 5AA from dropdown and click `Choose address`

  @proveYourIdentity_happyPath
  Scenario: Prove Your Identity Full Journey Route (STUB)
    And the user enters the date 2014 they moved into their current address
    And the user clicks `I confirm my details are correct`
    And I confirm the current page is the fraud check page
    And I set the crosscore version
    When I check Continue button is enabled and click on the Continue button
    And the user clicks `Answer security questions`
    And kenneth answers the first question correctly
    And kenneth answers the second question correctly
    And kenneth answers the third question correctly
    And the user clicks `I confirm my details are correct`
    Then verify the users address credentials. current address 8 HADLEY ROAD, BATH, BA2 5AA
    And verify the users fraud credentials
    And The test is complete and I close the driver

  @proveYourIdentity_happyPath_multiple_addresses
  Scenario: Prove Your Identity Full Journey Route multiple addresses (STUB)
    And the user enters the current year as the date they moved into their current address
    And they select `NO` for `Have you lived here for more than 3 months?` and click on `Continue`
    Then they should be on `What is your previous home address?`
    And I enter LS7 4JZ in the Postcode field and find address
    And the user chooses their address 16 NEWTON GARTH, LEEDS, LS7 4JZ from dropdown and click `Choose address`
    And the user clicks `I confirm my details are correct`
    And the user clicks `I confirm my details are correct`
    And I confirm the current page is the fraud check page
    And I set the crosscore version
    When I check Continue button is enabled and click on the Continue button
    And the user clicks `Answer security questions`
    And kenneth answers the first question correctly
    And kenneth answers the second question correctly
    And kenneth answers the third question correctly
#    And kenneth answers the fourth question correctly
    And the user clicks `I confirm my details are correct`
    Then verify the users address credentials. current address 8 HADLEY ROAD, BATH, BA2 5AA
    And verify the users fraud credentials
    And The test is complete and I close the driver
