Feature: Driving Licence Test

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the Build environment
    And I search for Driving Licence user number 5 in the Experian table
    And I should be on `Who was your UK driving licence issued by` page
    And I click on DVLA radio button and Continue
    And I should be on `Enter your details exactly as they appear on your UK driving licence` page

  @DVLADrivingLicence_test @build @tmsLink=LIME-165
  Scenario Outline:  DVLA Driving Licence details page happy path
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And JSON response should contain documentNumber PARKE610112PBFGH same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyPeter   |

  @DVLADrivingLicence_test @build @tmsLink=LIME-165
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectDrivingLicenceNumber
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And JSON response should contain documentNumber PARKE610112PBFGI same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |IncorrectDrivingLicenceNumber |

  @DVLADrivingLicence_test @build @tmsLink=LIME-165
  Scenario Outline: DVLA Driving Licence details page unhappy path when licence number date format does not match with User's Date Of Birth
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Licence number should be displayed in the Error summary
    And Check you have entered your date of birth correctly error should be displayed in the Error summary
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |InvalidDrivingLicenceNumber |

  @DVLADrivingLicence_test @build @tmsLink=LIME-165
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectDateOfBirth
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Check you have entered your date of birth correctly error should be displayed in the Error summary
    And Proper error message for invalid Licence number should be displayed in the Error summary
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectDateOfBirth |

  @DVLADrivingLicence_test @build @tmsLink=LIME-165
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectFirstName
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectFirstName|

  @DVLADrivingLicence_test @build @tmsLink=LIME-165
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectLastName
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |IncorrectLastName|

  @DVLADrivingLicence_test @build @tmsLink=LIME-165
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectIssueDate
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectIssueDate|

  @DVLADrivingLicence_test @build @tmsLink=LIME-165
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectValidToDate
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectValidToDate|

  @DVLADrivingLicence_test @build @tmsLink=LIME-165
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectIssueNumber
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectIssueNumber|

  @DVLADrivingLicence_test @build @tmsLink=LIME-165
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectPostcode
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectPostcode|

  @DVLADrivingLicence_test @build @tmsLink=LIME-167
  Scenario Outline: DVLA Driving Licence Retry Test Happy Path
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters data as a <DrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyPeter |

  @DVLADrivingLicence_test @build @tmsLink=LIME-167
  Scenario Outline: DVLA Driving Licence User failed second attempt
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters data as a <DrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectDrivingLicenceNumber |

  @DVLADrivingLicence_test @build @tmsLink=LIME-167
  Scenario: DVLA Driving Licence User cancels after failed first attempt
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci DO2, validity score 0 and strength score 3
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build @tmsLink=LIME-167
  Scenario: DVLA Driving Licence User cancels before first attempt via prove your identity another way route
    Given User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build @tmsLink=LIME-167
  Scenario: DVLA Driving Licence User cancels before first attempt via I do not have a UK driving licence route
    Given User click on ‘Back' Link
    When User click on I do not have a UK driving licence radio button
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

    ###########  DVLA Field Validations ##########
  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Last name with numbers error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Last name should be displayed in the Error summary
    And Field error message for invalid Last name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |InvalidLastNameWithNumbers |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Last name with special characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Last name should be displayed in the Error summary
    And Field error message for invalid Last name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InvalidLastNameWithSpecialCharacters |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence - No Last name in the Last name field error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Last name should be displayed in the Error summary
    And Field error message for invalid Last name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoLastName |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence First name with numbers error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid First name should be displayed in the Error summary
    And Field error message for invalid First name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |InvalidFirstNameWithNumbers |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence First name with special characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid First name should be displayed in the Error summary
    And Field error message for invalid First name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InvalidFirstNameWithSpecialCharacters |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence - No First name in the First name field error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid First name should be displayed in the Error summary
    And Field error message for invalid First name should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoFirstName |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Middle names with numbers error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Middle names should be displayed in the Error summary
    And Field error message for invalid Middle names should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |InvalidMiddleNamesWithNumbers |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Middle names with special characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Middle names should be displayed in the Error summary
    And Field error message for invalid Middle names should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |InvalidMiddleNamesWithSpecialCharacters |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Date of birth that are not real error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for no or invalid Date of Birth should be displayed
    And Field error message for no or invalid Date of Birth should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InvalidDateOfBirth |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Date of birth with special characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for no or invalid Date of Birth should be displayed
    And Field error message for no or invalid Date of Birth should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |DateOfBirthWithSpecialCharacters |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Date of birth in the future error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Your date of birth must be in the past error should be displayed in the Error summary
    And Your date of birth must be in the past Field error message should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |DateOfBirthInFuture |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence - No Date in the Date of birth field error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for no or invalid Date of Birth should be displayed
    And Field error message for no or invalid Date of Birth should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoDateOfBirth |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Issue date that are not real error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Issue date should be displayed in the Error summary
    And Field error message for invalid Issue date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InvalidIssueDate |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Issue date with special characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Issue date should be displayed in the Error summary
    And Field error message for invalid Issue date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueDateWithSpecialCharacters |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Issue date in the future error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then The issue date must be in the past error message should be displayed in the Error summary
    And The issue date must be in the past Field error message should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueDateInFuture |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence - No date in the Issue date field error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Issue date should be displayed in the Error summary
    And Field error message for invalid Issue date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoIssueDate |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Valid to date that are not real error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Valid to date should be displayed in the Error summary
    And Field error message for invalid Valid to date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InvalidValidToDate |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Valid to date with special characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Valid to date should be displayed in the Error summary
    And Field error message for invalid Valid to date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |ValidToDateWithSpecialCharacters |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Valid to date in the past error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for expired driving licence should be displayed in the Error summary
    And Field error message for expired driving licence should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |ValidToDateInPast |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence - No date in the Valid to date field error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Valid to date should be displayed in the Error summary
    And Field error message for invalid Valid to date should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoValidToDate |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence number less than 16 characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Your licence number should be 16 characters long error message should be displayed in the Error summary
    And Your licence number should be 16 characters long Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |DrivingLicenceNumLessThan16Char |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence number with special characters and spaces error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Your licence number should not include any symbols or spaces error message should be displayed in the Error summary
    And Your licence number should not include any symbols or spaces Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |DrivingLicenceNumberWithSpecialChar |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence number with numeric characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Licence number should be displayed in the Error summary
    And Field error message for invalid Licence number should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |DrivingLicenceNumberWithNumericChar |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence number with alpha characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Licence number should be displayed in the Error summary
    And Field error message for invalid Licence number should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |DrivingLicenceNumberWithAlphaChar |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence - No Licence number in the Licence number field error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Licence number should be displayed in the Error summary
    And Field error message for invalid Licence number should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoDrivingLicenceNumber |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Issue number less than 2 characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Your issue number should be 2 numbers long error message should be displayed in the Error summary
    And Your issue number should be 2 numbers long Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueNumberLessThan2Char |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Issue number with special characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Your issue number should not include any symbols or spaces error message should be displayed in the Error summary
    And Your issue number should not include any symbols or spaces Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueNumberWithSpecialChar |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Issue number with alphanumeric characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Issue number should be displayed in the Error summary
    And Field error message for invalid Issue number should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueNumberWithAlphanumericChar |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Issue number with alpha characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Issue number should be displayed in the Error summary
    And Field error message for invalid Issue number should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueNumberWithAlphaChar |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence - No Issue number in the Issue number field error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Issue number should be displayed in the Error summary
    And Field error message for invalid Issue number should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoIssueNumber |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Postcode less than 5 characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Your postcode should be between 5 and 7 characters error should be displayed in the Error summary
    And Your postcode should be between 5 and 7 characters Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |PostcodeLessThan5Char |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Postcode with special characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Your postcode should only include numbers and letters error message should be displayed in the Error summary
    And Your postcode should only include numbers and letters Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |PostcodeWithSpecialChar |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Postcode with numeric characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Your postcode should include numbers and letters error message should be displayed in the Error summary
    And Your postcode should include numbers and letters Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |PostcodeWithNumericChar |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence Postcode with alpha characters error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Your postcode should include numbers and letters error message should be displayed in the Error summary
    And Your postcode should include numbers and letters Field error should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |PostcodeWithAlphaChar |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence - No Postcode in the Postcode field error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for invalid Postcode should be displayed in the Error summary
    And Field error message for invalid Postcode should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoPostcode |

  @DVLADrivingLicence_test @build
  Scenario Outline: DVLA Driving Licence International Postcode error validation
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Enter a UK postcode should be displayed in the Error summary
    And Enter a UK postcode Field error message should be displayed
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InternationalPostcode |

  @DVLADrivingLicence_test @build
  Scenario Outline:  DVLA Driving Licence Generate VC with invalid DL number and prove in another way unhappy path
    Given User enters data as a <DrivingLicenceSubject>
    When User clicks on continue
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON response should contain documentNumber PARKE610112PBFGI same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      | IncorrectDrivingLicenceNumber    |