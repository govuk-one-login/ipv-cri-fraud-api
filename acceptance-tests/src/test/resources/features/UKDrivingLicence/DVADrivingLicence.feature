Feature: DVA Driving Licence Test

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the Build environment
    And I search for Driving Licence user number 5 in the Experian table
    And I should be on `Who was your UK driving licence issued by` page
    And I click on DVA radio button and Continue
    And I should be on DVA `Enter your details exactly as they appear on your UK driving licence` page

  @DVADrivingLicence_test @build
  Scenario Outline:  DVA Driving Licence details page happy path
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And JSON response should contain documentNumber 55667788 same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject             |
      |DVADrivingLicenceSubjectHappyBilly   |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence details page unhappy path with InvalidDVADrivingLicenceDetails
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |DVADrivingLicenceSubjectUnhappySelina |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVADrivingLicenceNumber
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And JSON response should contain documentNumber 88776655 same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |IncorrectDVADrivingLicenceNumber |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVADateOfBirth
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDVADateOfBirth |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAFirstName
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDVAFirstName|

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVALastName
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDVALastName|

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAIssueDate
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDVAIssueDate|

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAValidToDate
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDVAValidToDate|

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAPostcode
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDVAPostcode|

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Retry Test Happy Path
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVADrivingLicenceSubjectHappyBilly |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence User failed second attempt
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDVADrivingLicenceNumber |

  @DVADrivingLicence_test @build
  Scenario: DVA Driving Licence User cancels after failed first attempt
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Driving Licence User cancels before first attempt via prove your identity another way route
    Given User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Driving Licence User cancels before first attempt via I do not have a UK driving licence route
    Given User click on ‘Back' Link
    When User click on I do not have a UK driving licence radio button
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

###########  DVA Field Validations ##########
  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Last name with numbers error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Last name should be displayed in the Error summary
    And Field error message for invalid Last name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |InvalidDVALastNameWithNumbers |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Last name with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Last name should be displayed in the Error summary
    And Field error message for invalid Last name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |InvalidDVALastNameWithSpecialChar |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence No Last name in the Last name field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Last name should be displayed in the Error summary
    And Field error message for invalid Last name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoDVALastName |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence First name with numbers error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid First name should be displayed in the Error summary
    And Field error message for invalid First name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |InvalidDVAFirstNameWithNumbers |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence First name with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid First name should be displayed in the Error summary
    And Field error message for invalid First name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |InvalidDVAFirstNameWithSpecialChar |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence No First name in the First name field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid First name should be displayed in the Error summary
    And Field error message for invalid First name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoDVAFirstName |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Date of birth that are not real error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper DVA error message for no or invalid Date of Birth should be displayed
    And Field DVA error message for no or invalid Date of Birth should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |InvalidDVADateOfBirth |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Date of birth with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper DVA error message for no or invalid Date of Birth should be displayed
    And Field DVA error message for no or invalid Date of Birth should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVADOBWithSpecialCharacters |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Date of birth in the future error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Your date of birth must be in the past DVA error should be displayed in the Error summary
    And Your date of birth must be in the past DVA Field error message should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVADateOfBirthInFuture |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence - No Date in the Date of birth field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper DVA error message for no or invalid Date of Birth should be displayed
    And Field DVA error message for no or invalid Date of Birth should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoDVADateOfBirth |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Issue date that are not real error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper DVA error message for invalid Issue date should be displayed in the Error summary
    And Field DVA error message for invalid Issue date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVAInvalidIssueDate |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Issue date with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper DVA error message for invalid Issue date should be displayed in the Error summary
    And Field DVA error message for invalid Issue date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVAIssueDateWithSpecialChar |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Issue date in the future error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then The issue date must be in the past DVA error message should be displayed in the Error summary
    And The issue date must be in the past DVA Field error message should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVAIssueDateInFuture |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence - No date in the Issue date field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper DVA error message for invalid Issue date should be displayed in the Error summary
    And Field DVA error message for invalid Issue date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoDVAIssueDate |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Valid to date that are not real error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Valid to date should be displayed in the Error summary
    And Field error message for invalid Valid to date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVAInvalidValidToDate |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Valid to date with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Valid to date should be displayed in the Error summary
    And Field error message for invalid Valid to date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVAValidToDateWithSpecialChar |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Valid to date in the past error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for expired driving licence should be displayed in the Error summary
    And Field error message for expired driving licence should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVAValidToDateInPast |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence - No date in the Valid to date field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Valid to date should be displayed in the Error summary
    And Field error message for invalid Valid to date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoDVAValidToDate |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence number less than 8 characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Your licence number should be 8 characters long error message should be displayed in the Error summary
    And Your licence number should be 8 characters long Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVADrivingLicenceNumLessThan8Char |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence number with special characters and spaces error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Your licence number should not include any symbols or spaces DVA error should be displayed in the Error summary
    And Your licence number should not include any symbols or spaces DVA Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVADrivingLicenceNumWithSpecialChar |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence number with alpha numeric characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper DVA error message for invalid Licence number should be displayed in the Error summary
    And Field DVA error message for invalid Licence number should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVADrivingLicenceNumWithAlphanumericChar |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence number with alpha characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper DVA error message for invalid Licence number should be displayed in the Error summary
    And Field DVA error message for invalid Licence number should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVADrivingLicenceNumberWithAlphaChar |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence - No Licence number in the Licence number field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper DVA error message for invalid Licence number should be displayed in the Error summary
    And Field DVA error message for invalid Licence number should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoDVADrivingLicenceNumber |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Postcode less than 5 characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Your postcode should be between 5 and 7 characters error should be displayed in the Error summary
    And Your postcode should be between 5 and 7 characters Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVAPostcodeLessThan5Char |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Postcode with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Your postcode should only include numbers and letters error message should be displayed in the Error summary
    And Your postcode should only include numbers and letters Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVAPostcodeWithSpecialChar |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Postcode with numeric characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Your postcode should include numbers and letters error message should be displayed in the Error summary
    And Your postcode should include numbers and letters Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVAPostcodeWithNumericChar |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence Postcode with alpha characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Your postcode should include numbers and letters error message should be displayed in the Error summary
    And Your postcode should include numbers and letters Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVAPostcodeWithAlphaChar |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence - No Postcode in the Postcode field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Postcode should be displayed in the Error summary
    And Field error message for invalid Postcode should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoDVAPostcode |

  @DVADrivingLicence_test @build
  Scenario Outline: DVA Driving Licence International Postcode error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Enter a UK postcode should be displayed in the Error summary
    And Enter a UK postcode Field error message should be displayed
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |DVAInternationalPostcode |

  @DVADrivingLicence_test @build
  Scenario Outline:  DVA Driving Licence Generate VC with invalid DL number and prove in another way unhappy path
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON response should contain documentNumber 88776655 same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject           |
      | IncorrectDVADrivingLicenceNumber     |