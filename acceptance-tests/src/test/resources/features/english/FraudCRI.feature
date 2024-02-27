@fraud_CRI
Feature: Fraud CRI

  @happy_path @build-fraud @staging-fraud @integration-fraud
  Scenario: User Journey Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    Then I search for user number 12 in the ThirdParty table
    And I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain JTI field
    And The test is complete and I close the driver

#  To be removed after the test is moved to the front repo
  @happy_path @build-fraud @staging-fraud @integration-fraud
  Scenario: Beta Banner Reject Analysis
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    Then I search for user number 12 in the ThirdParty table
    When I view the Beta banner
    When the beta banner reads This is a new service – your feedback (opens in new tab) will help us to improve it.
    And I select Reject analytics cookies button
    Then I see the Reject Analysis sentence You've rejected additional cookies. You can change your cookie settings at any time.
    Then  I select the link change your cookie settings
    Then I check the page to change cookie preferences opens
    And The test is complete and I close the driver

  @unhappy_path @build-fraud @staging-fraud @integration-fraud
  Scenario: User Journey Unhappy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    Then I search for user number 14 in the ThirdParty table
    And I navigate to the verifiable issuer to check for a Invalid response from thirdParty
    And The test is complete and I close the driver

  @userSearch_by_userName_happyPath @build-fraud @staging-fraud @integration-fraud
  Scenario: User Search By UserName User Journey Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    When I search for user name Linda Duff in the ThirdParty table
    And I click on Go to Fraud CRI link
    Then I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain user's name
    And The test is complete and I close the driver

  @Spinner_icon_within_Fraud_CRI_screen @build-fraud @staging-fraud @integration-fraud
  Scenario: User is presented with a spinner when clicking on the Continue button in the Fraud CRI screen (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user number 12 in the ThirdParty table
    And I confirm the current page is the fraud check page
    When I check Continue button is enabled and click on the Continue button
    Then I navigate to Verifiable Credentials page
    And I check for a Valid response from thirdParty
    And The test is complete and I close the driver


  @userSearch_by_invalid_userName @staging-fraud
  Scenario: User Search By Invalid UserName(STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    When I search for user name Debra Kiritharnathan in the ThirdParty table
    And I click on Go to Fraud CRI link
    Then I navigate to the verifiable issuer to check for a Invalid response from thirdParty
    And JSON response should contain error details and status code as 302
    And The test is complete and I close the driver

  @userSearch_by_invalid_userName @integration-fraud
  Scenario: User Search By Invalid UserName(STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Integration environment
    When I search for user name Debra Kiritharnathan in the ThirdParty table
    And I click on Go to Fraud CRI link
    Then I navigate to the verifiable issuer to check for a Invalid response from thirdParty
    And JSON response should contain error details and status code as 302
    And The test is complete and I close the driver

  @edituser_happyPath @build-fraud @staging-fraud @integration-fraud
  Scenario: Edit User Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name Linda Duff in the ThirdParty table
    When I click on Edit User link
    And I am on Edit User page
    And I enter Test 45 in the House name field
    And I clear existing House number
    And I enter 455 in the House number field
    Then I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain user's House name as Test 45 and House number as 455
    And The test is complete and I close the driver

  @happy_path_with_ci_fraud @staging-fraud
  Scenario: User Journey Happy Path with A01 CI
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name MICHELLE KABIR in the ThirdParty table
    When I click on Edit User link
    And I am on Edit User page
    And I clear existing Date of Birth
    And I enter Date of birth as 13/06/1987
    And I submit user updates
    And I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain ci A01 and score 2
    And The test is complete and I close the driver

  @pep_test_all_users @build-fraud
  Scenario Outline: Edit User Happy Path with pep CI (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name LINDA DUFF in the ThirdParty table
    When I click on Edit User link
    And I am on Edit User page
    And I clear existing Date of Birth
    And I enter Date of birth as <dob>
    And I clear existing first name
    And I clear existing surname
    And I enter name <name>
    And I submit user updates
    And I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver

    Examples:
      | name               | dob        | ci  | score |
      | ANTHONY CI6        | 17/02/1963 | P01 | 2     |
      | ANTHONY CI4        | 17/02/1963 | T03 | 0     |
      | ANTHONY NO_FILE_35 | 17/02/1963 |     | 1     |

  @pep_test_all_users @staging-fraud
  Scenario Outline: Edit User Happy Path with pep CI (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name LINDA DUFF in the ThirdParty table
    When I click on Edit User link
    And I am on Edit User page
    And I clear existing Date of Birth
    And I enter Date of birth as <dob>
    And I clear existing first name
    And I clear existing surname
    And I enter name <name>
    And I submit user updates
    And I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver

    Examples:
      | name                  | dob        | ci  | score |
      | ANTHONY ROBERTS       | 17/02/1963 | P01 | 2     |
      | ALAVEEN MCOLIVER      | 12/07/1962 | P02 | 2     |
      | SPIROS ALLANIS        | 23/02/1985 |     | 2     |
      | ALBERT ARKIL          | 30/05/1947 |     | 2     |
      | KATHERINE MILES       | 04/07/1963 | P02 | 2     |
      | AMANDA HUSSEIN        | 03/05/1981 |     | 2     |
      | LISA WHALEY           | 28/11/1974 |     | 2     |
      | CHRISTOPHER LUKYAMUZI | 03/10/1968 |     | 2     |
      | INDUMATHY OSHEA       | 19/12/1961 |     | 2     |
      | MICHELLE VORAPRAWAT   | 19/08/1978 |     | 2     |
      | DOUGLAS BEASLEY       | 25/08/1980 |     | 2     |
      | PHILLIP CRIS          | 16/12/1988 | P01 | 2     |
      | JOYCE BASU            | 23/02/1943 |     | 2     |
      | MARY MURTAGH          | 22/02/1960 | P01 | 2     |
      | JOHN SAGGAN           | 23/07/1936 | P01 | 2     |
      | JEAN DUPHIE           | 30/10/1950 | P02 | 2     |
      | IAN PADFIELD          | 24/05/1976 | P02 | 2     |
      | BARRY WYLIE           | 08/06/1958 | P02 | 2     |
      | LYNNE BROWNLIE        | 26/04/1968 | P02 | 2     |
      | RENEE JULIE           | 03/04/1973 |     | 2     |
      | CHRISTINE BRUTON      | 07/09/1961 |     | 2     |
      | DAVID ATTWATER        | 03/11/1959 |     | 2     |
      | VICTORIA WOOD         | 27/02/1985 | P01 | 2     |
      | CASSIE MORRIS         | 13/10/2000 | P02 | 2     |
      | SIMON HAMMOND         | 19/08/1980 |     | 2     |
      | DIPTI STUPPART        | 26/01/1989 | P02 | 2     |
      | JAMALA BROWER         | 27/10/1963 | P02 | 2     |

  @test_PEP_user_with_multiple_addresses @staging-fraud
  Scenario Outline: Edit PEP User with multiple addresses (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name LINDA DUFF in the ThirdParty table
    When I click on Edit User link
    Then I am on Edit User page
    And I clear existing Date of Birth
    And I enter Date of birth as <dob>
    And I clear existing first name
    And I clear existing surname
    And I enter name <name>
    When I click on Second address
    And I enter Second address details
      | housenumber | streetname | townorcity | postcode |
      | 285         | HIGH STREET| WESTBURY   | BA13 3BN  |
    And I enter valid to date as 01/01/2021
    And I submit user updates
    Then I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver

    #Pep is skipped due to zero decision score
    Examples:
      | name                    | dob            | ci  | score |
      | ANTHONY ROBERTS         | 25/06/1959     |     |   1   |

  @test_PEP_user_with_multiple_addresses @build-fraud
  Scenario Outline: Edit PEP User with multiple addresses (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name LINDA DUFF in the ThirdParty table
    When I click on Edit User link
    Then I am on Edit User page
    And I clear existing Date of Birth
    And I enter Date of birth as <dob>
    And I clear existing first name
    And I clear existing surname
    And I enter name <name>
    When I click on Second address
    And I enter Second address details
      | housenumber | streetname | townorcity | postcode |
      | 285         | HIGH STREET| WESTBURY   | BA13 3BN  |
    And I enter valid to date as 01/01/2021
    And I submit user updates
    Then I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver

    Examples:
      | name                    | dob            | ci  | score |
      | ANTHONY ROBERTS         | 25/06/1959     |     |   2   |

  @build-fraud
  Scenario Outline:Crosscore Authenticate and PEP completed and user is a PEP
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name LINDA DUFF in the ThirdParty table
    When I click on Edit User link
    Then I am on Edit User page
    And I clear existing Date of Birth
    Then I enter Date of birth as <dob>
    And I clear existing first name
    Then I clear existing surname
    Then I enter name <name>
    And I submit user updates
    Then I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain checkDetails impersonation_risk_check
    And JSON payload should contain checkDetails mortality_check
    And JSON payload should contain checkDetails identity_theft_check
    Then JSON payload should contain checkDetails synthetic_identity_check
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver
    Examples:
      | name                    | dob            |ci  |score|
      | JAMALA BROWER           | 27/10/1963     |    |2    |


  @build-fraud
  Scenario Outline:Crosscore Authenticate and PEP completed and user not PEP
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name LINDA DUFF in the ThirdParty table
    When I click on Edit User link
    Then I am on Edit User page
    And I clear existing Date of Birth
    Then I enter Date of birth as <dob>
    And I clear existing first name
    Then I clear existing surname
    And I enter name <name>
    And I submit user updates
    Then I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain checkDetails impersonation_risk_check
    And JSON payload should contain checkDetails mortality_check
    And JSON payload should contain checkDetails identity_theft_check
    Then JSON payload should contain checkDetails synthetic_identity_check
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver
    Examples:
      | name                    | dob            | ci   | score|
      | ALBERT PEPS             | 05/10/1943     |P01   | 2     |


  @build-fraud
  Scenario Outline: Mortality u-code returned
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name LINDA DUFF in the ThirdParty table
    When I click on Edit User link
    Then I am on Edit User page
    And I clear existing Date of Birth
    Then I enter Date of birth as <dob>
    And I clear existing first name
    And I clear existing surname
    Then I enter name <name>
    And I submit user updates
    Then I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain failedCheckDetails mortality_check
    And JSON payload should contain failedCheckDetails identity_theft_check
    Then JSON payload should contain failedCheckDetails synthetic_identity_check
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver
    Examples:
      | name           | dob              | ci   | score  |
      | ALBERT GILT    | 05/10/1943       | T02  | 0     |


  @build-fraud
  Scenario Outline: Crosscore Authenticate completed and PEP not completed due to error from ThirdParty
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name LINDA DUFF in the ThirdParty table
    When I click on Edit User link
    Then I am on Edit User page
    And I clear existing Date of Birth
    Then I enter Date of birth as <dob>
    And I clear existing first name
    And I clear existing surname
    Then I enter name <name>
    And I submit user updates
    Then I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain failedCheckDetails impersonation_risk_check
    And JSON payload should contain checkDetails mortality_check
    And JSON payload should contain checkDetails identity_theft_check
    Then JSON payload should contain checkDetails synthetic_identity_check
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver
    Examples:
      | name                         | dob              | ci   | score  |
      | ALBERT PEP_ERROR_RESPONSE    | 05/10/1943       |      | 1    |

  @build-fraud
  Scenario Outline: Crosscore Authenticate completed and PEP not completed due to technical failure
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name LINDA DUFF in the ThirdParty table
    When I click on Edit User link
    Then I am on Edit User page
    And I clear existing Date of Birth
    Then I enter Date of birth as <dob>
    And I clear existing first name
    And I clear existing surname
    Then I enter name <name>
    And I submit user updates
    Then I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain failedCheckDetails impersonation_risk_check
    And JSON payload should contain checkDetails mortality_check
    And JSON payload should contain checkDetails identity_theft_check
    Then JSON payload should contain checkDetails synthetic_identity_check
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver
    Examples:
      | name                    | dob              | ci   | score  |
      | ALBERT PEP_TECH_FAIL    | 05/10/1943       |      | 1    |

  @build-fraud
  Scenario Outline: Decision score below 35
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the testEnvironment
    And I search for user name LINDA DUFF in the ThirdParty table
    When I click on Edit User link
    Then I am on Edit User page
    And I clear existing Date of Birth
    Then I enter Date of birth as <dob>
    And I clear existing first name
    And I clear existing surname
    Then I enter name <name>
    And I submit user updates
    Then I navigate to the verifiable issuer to check for a Valid response from thirdParty
    And JSON payload should contain failedCheckDetails mortality_check
    And JSON payload should contain failedCheckDetails identity_theft_check
    Then JSON payload should contain failedCheckDetails synthetic_identity_check
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver
    Examples:
      | name                    | dob              | ci   | score  |
      | ALBERT NO_FILE_35       | 05/10/1943       |      | 1      |
