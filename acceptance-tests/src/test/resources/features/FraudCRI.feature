@fraud_CRI
Feature: Fraud CRI

  @happy_path @build-fraud
  Scenario: User Journey Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Build environment
    Then I search for user number 12 in the Experian table
    And I navigate to the verifiable issuer to check for a Valid response from experian
    And The test is complete and I close the driver

  @happy_path @staging-fraud
  Scenario: User Journey Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Staging environment
    Then I search for user number 12 in the Experian table
    And I navigate to the verifiable issuer to check for a Valid response from experian
    And The test is complete and I close the driver

  @happy_path @integration-fraud
  Scenario: User Journey Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Integration environment
    Then I search for user number 12 in the Experian table
    And I navigate to the verifiable issuer to check for a Valid response from experian
    And The test is complete and I close the driver

  @unhappy_path @build-fraud
  Scenario: User Journey Unhappy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Build environment
    Then I search for user number 14 in the Experian table
    And I navigate to the verifiable issuer to check for a Invalid response from experian
    And The test is complete and I close the driver

  @unhappy_path @staging-fraud
  Scenario: User Journey Unhappy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Staging environment
    Then I search for user number 14 in the Experian table
    And I navigate to the verifiable issuer to check for a Invalid response from experian
    And The test is complete and I close the driver

  @unhappy_path @integration-fraud
  Scenario: User Journey Unhappy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Integration environment
    Then I search for user number 14 in the Experian table
    And I navigate to the verifiable issuer to check for a Invalid response from experian
    And The test is complete and I close the driver

  @external_links @build-fraud
  Scenario Outline: User Navigates To Experian/Privacy Policy
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Build environment
    Then I search for user number 12 in the Experian table
    Then I navigate to <page> and assert I have been directed correctly
    And The test is complete and I close the driver

    Examples:
      | page           |
      | Experian       |
      | Privacy Policy |

  @external_links @staging-fraud
  Scenario Outline: User Navigates To Experian/Privacy Policy
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Staging environment
    Then I search for user number 12 in the Experian table
    Then I navigate to <page> and assert I have been directed correctly
    And The test is complete and I close the driver

    Examples:
      | page           |
      | Experian       |
      | Privacy Policy |

  @external_links @integration-fraud
  Scenario Outline: User Navigates To Experian/Privacy Policy
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Integration environment
    Then I search for user number 12 in the Experian table
    Then I navigate to <page> and assert I have been directed correctly
    And The test is complete and I close the driver

    Examples:
      | page           |
      | Experian       |
      | Privacy Policy |

  @userSearch_by_userName_happyPath @build-fraud
  Scenario: User Search By UserName User Journey Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Build environment
    When I search for user name Linda Duff in the Experian table
    And I click on Go to Fraud CRI link
    Then I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain user's name
    And The test is complete and I close the driver

  @userSearch_by_userName_happyPath @staging-fraud
  Scenario: User Search By UserName User Journey Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Staging environment
    When I search for user name Linda Duff in the Experian table
    And I click on Go to Fraud CRI link
    Then I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain user's name
    And The test is complete and I close the driver

  @userSearch_by_userName_happyPath @integration-fraud
  Scenario: User Search By UserName User Journey Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Integration environment
    When I search for user name Linda Duff in the Experian table
    And I click on Go to Fraud CRI link
    Then I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain user's name
    And The test is complete and I close the driver

  @Spinner_icon_within_Fraud_CRI_screen @build-fraud
  Scenario: User is presented with a spinner when clicking on the Continue button in the Fraud CRI screen (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Build environment
    And I search for user number 12 in the Experian table
    And I navigate to the page We need to check your details
    When I check Continue button is enabled and click on the Continue button
    Then I navigate to Verifiable Credentials page
    And I check for a Valid response from experian
    And The test is complete and I close the driver

  @Spinner_icon_within_Fraud_CRI_screen @staging-fraud
  Scenario: User is presented with a spinner when clicking on the Continue button in the Fraud CRI screen (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Staging environment
    And I search for user number 12 in the Experian table
    And I navigate to the page We need to check your details
    When I check Continue button is enabled and click on the Continue button
    Then I navigate to Verifiable Credentials page
    And I check for a Valid response from experian
    And The test is complete and I close the driver

  @Spinner_icon_within_Fraud_CRI_screen @integration-fraud
  Scenario: User is presented with a spinner when clicking on the Continue button in the Fraud CRI screen (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Integration environment
    And I search for user number 12 in the Experian table
    And I navigate to the page We need to check your details
    When I check Continue button is enabled and click on the Continue button
    Then I navigate to Verifiable Credentials page
    And I check for a Valid response from experian
    And The test is complete and I close the driver

  @userSearch_by_invalid_userName @staging-fraud
  Scenario: User Search By Invalid UserName(STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Staging environment
    When I search for user name Debra Kiritharnathan in the Experian table
    And I click on Go to Fraud CRI link
    Then I navigate to the verifiable issuer to check for a Invalid response from experian
    And JSON response should contain error details and status code as 302
    And The test is complete and I close the driver

  @userSearch_by_invalid_userName @integration-fraud
  Scenario: User Search By Invalid UserName(STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Integration environment
    When I search for user name Debra Kiritharnathan in the Experian table
    And I click on Go to Fraud CRI link
    Then I navigate to the verifiable issuer to check for a Invalid response from experian
    And JSON response should contain error details and status code as 302
    And The test is complete and I close the driver

  @edituser_happyPath @build-fraud
  Scenario: Edit User Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Build environment
    And I search for user name Linda Duff in the Experian table
    When I click on Edit User link
    And I am on Edit User page
    And I enter Test 45 in the House name field
    And I clear existing House number
    And I enter 455 in the House number field
    Then I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain user's House name as Test 45 and House number as 455
    And The test is complete and I close the driver

  @edituser_happyPath @staging-fraud
  Scenario: Edit User Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Staging environment
    And I search for user name Linda Duff in the Experian table
    When I click on Edit User link
    And I am on Edit User page
    And I enter Test 45 in the House name field
    And I clear existing House number
    And I enter 455 in the House number field
    Then I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain user's House name as Test 45 and House number as 455
    And The test is complete and I close the driver

  @edituser_happyPath @integration-fraud
  Scenario: Edit User Happy Path (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Integration environment
    And I search for user name Linda Duff in the Experian table
    When I click on Edit User link
    And I am on Edit User page
    And I enter Test 45 in the House name field
    And I clear existing House number
    And I enter 455 in the House number field
    Then I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain user's House name as Test 45 and House number as 455
    And The test is complete and I close the driver

  @happy_path_with_ci_fraud @staging-fraud
  Scenario: User Journey Happy Path with A01 CI (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Staging environment
    Then I search for user number 34 in the Experian table
    And I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain ci A01 and score 2
    And The test is complete and I close the driver

  @happy_path_with_ci_fraud @integration-fraud
  Scenario: User Journey Happy Path with A01 CI (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Integration environment
    Then I search for user number 34 in the Experian table
    And I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain ci A01 and score 2
    And The test is complete and I close the driver

  # User with surname CI6 will return the U015 code and will return CI as P01 in the VC
  @pep_test_all_users @build-fraud @test
  Scenario Outline: Edit User Happy Path with pep CI (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Build environment
    And I search for user name LINDA DUFF in the Experian table
    When I click on Edit User link
    And I am on Edit User page
    And I clear existing Date of Birth
    And I enter Date of birth as <dob>
    And I clear existing first name
    And I clear existing surname
    And I enter name <name>
    And I submit user updates
    And I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver

    Examples:
      | name                  | dob            | ci   | score |
      | ANTHONY CI6           | 17/02/1963     | P01  |    2  |
      | ANTHONY CI4           | 17/02/1963     | T03  |    0  |
      | ANTHONY NO_FILE_35    | 17/02/1963     |      |    1  |


  @pep_test_all_users @staging-fraud
  Scenario Outline: Edit User Happy Path with pep CI (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Staging environment
    And I search for user name LINDA DUFF in the Experian table
    When I click on Edit User link
    And I am on Edit User page
    And I clear existing Date of Birth
    And I enter Date of birth as <dob>
    And I clear existing first name
    And I clear existing surname
    And I enter name <name>
    And I submit user updates
    And I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver

    Examples:
      | name                    | dob            | ci  | score |
      | ANTHONY ROBERTS         | 17/02/1963     | P01 |   2   |
      | ALAVEEN MCOLIVER        | 12/07/1962     | P02 |   2   |
      | SPIROS ALLANIS          | 23/02/1985     |  |      2   |
      | ALBERT ARKIL            | 30/05/1947     |  |      2   |
      | KATHERINE MILES         | 04/07/1963     | P02 |   2   |
      | AMANDA HUSSEIN          | 03/05/1981     |  |      2   |
      | LISA WHALEY             | 28/11/1974     |  |      2   |
      | CHRISTOPHER LUKYAMUZI   | 03/10/1968     |  |      2   |
      | INDUMATHY OSHEA         | 19/12/1961     |  |      2   |
      | MICHELLE VORAPRAWAT     | 19/08/1978     |  |      2   |
      | DOUGLAS BEASLEY         | 25/08/1980     |  |      2   |
      | PHILLIP CRIS            | 16/12/1988     | P01 |   2   |
      | JOYCE BASU              | 23/02/1943     |  |      2   |
      | MARY MURTAGH            | 22/02/1960     | P01 |   2   |
      | JOHN SAGGAN             | 23/07/1936     | P01 |   2   |
      | JEAN DUPHIE             | 30/10/1950     | P02 |   2   |
      | IAN PADFIELD            | 24/05/1976     | P02 |   2   |
      | BARRY WYLIE             | 08/06/1958     | P02 |   2   |
      | LYNNE BROWNLIE          | 26/04/1968     | P02 |   2   |
      | RENEE JULIE             | 03/04/1973     |  |      2   |
      | CHRISTINE BRUTON        | 07/09/1961     |  |      2   |
      | DAVID ATTWATER          | 03/11/1959     |  |      2   |
      | VICTORIA WOOD           | 27/02/1985     | P01 |   2   |
      | CASSIE MORRIS           | 13/10/2000     | P02 |   2   |
      | SIMON HAMMOND           | 19/08/1980     |  |      2   |
      | DIPTI STUPPART          | 26/01/1989     | P02 |   2   |
      | JAMALA BROWER           | 27/10/1963     | P02 |   2   |

  @Search_user_with_MissingDetails_and_EditUser_Unhappypath
  Scenario Outline: Search for user with missing details and edit user UnHappy Path (STUB)'
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the <environment> environment
    And I search for user name Richard Gillis in the Experian table
    When I click on Edit User link
    And I clear the postcode
    And I clear existing House number
    And I click on Go to Fraud CRI link after Edit
    Then I navigate to the verifiable issuer to check for a Invalid response from experian
    And JSON response should contain error details and status code as 302
    And Validate User navigation back to core for invalid users
    And The test is complete and I close the driver

    Examples:
      | environment |
      | Build       |
      | Staging     |
      | Integration |

  @test_PEP_user_with_multiple_addresses @staging-fraud
  Scenario Outline: Edit PEP User with multiple addresses (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Staging environment
    And I search for user name LINDA DUFF in the Experian table
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
    Then I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver

    #Pep is skipped due to zero decision score
    Examples:
      | name                    | dob            | ci  | score |
      | ANTHONY ROBERTS         | 25/06/1959     |     |   1   |

  @test_PEP_user_with_multiple_addresses @build-fraud
  Scenario Outline: Edit PEP User with multiple addresses (STUB)
    Given I navigate to the IPV Core Stub
    And I click the Fraud CRI for the Build environment
    And I search for user name LINDA DUFF in the Experian table
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
    Then I navigate to the verifiable issuer to check for a Valid response from experian
    And JSON payload should contain ci <ci> and score <score>
    And The test is complete and I close the driver

    Examples:
      | name                    | dob            | ci  | score |
      | ANTHONY ROBERTS         | 25/06/1959     |     |   2   |