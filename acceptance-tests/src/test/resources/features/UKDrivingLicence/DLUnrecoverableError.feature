Feature: Driving License Test

 Background:
   Given I navigate to the IPV Core Stub
   And I click the Driving Licence CRI for the Build environment
   Then I search for Driving Licence user number 5 in the Experian table

  @DVLADrivingLicence_test @build
  Scenario: Check the Unrecoverable error/ Unknown error in Driving Licence CRI
    And I click Go to Driving Licence CRI dev button
    Given I delete the cookie to get the unexpected error
    Then I can see the relevant error page with correct title
    And I can see the heading  Sorry, there is a error
    And The test is complete and I close the driver
