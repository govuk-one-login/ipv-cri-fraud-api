@fraud_CRI_API
Feature: Fraud CRI API

  @intialJWT_happy_path @fraudCRI_API @pre-merge @dev
  Scenario: Acquire initial JWT and Fraud Check with PEP error response failure(STUB)
    Given user ALBERT PEP_ERROR_RESPONSE row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-shared-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-shared-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 1

  @fraudCRI_API @pre-merge @dev
  Scenario: Acquire initial JWT and Fraud Check with PEP tech failure(STUB)
    Given user ALBERT PEP_TECH_FAIL row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-shared-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-shared-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 1

  @fraudCRI_API @pre-merge @dev
  Scenario: Fraud Check Succeeds and PEP Succeeds but is not PEP (HOLLINGDALU)
    Given user ALBERT HOLLINGDALU row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-shared-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-shared-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 2

  @fraudCRI_API @pre-merge @dev
  Scenario: Fraud Check and PEP check complete(STUB)
#    VC for Fraud Succeeds and PEP Succeeds but is PEP (PEPS)
    Given user ALBERT PEPS row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-shared-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-shared-dev
    Then user requests Fraud CRI VC
    And VC should contain ci P01 and identityFraudScore 2

  @fraudCRI_API @pre-merge @dev
  Scenario: Fraud Happy path for user with decision score below 35(STUB)
    Given user ALBERT NO_FILE_35 row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-shared-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-shared-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 1

  @fraudCRI_API @pre-merge @dev
  Scenario: Fraud Check for user found on mortality record(STUB)
    Given user ALBERT GILT row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-shared-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-shared-dev
    Then user requests Fraud CRI VC
    And VC should contain ci T02 and identityFraudScore 0

  @fraudCRI_API @pre-merge @dev
  Scenario Outline: Fraud Check for user with various activity history records(STUB)
    Given user LINDA DUFF row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-shared-dev
    Given user changes <field> in session request to <fieldValue> for fraud-cri-shared-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-shared-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore <identityFraudScore>
    And VC should contain activityHistory score of <activityHistoryScore>
    And VC evidence checks should contain <checks>
    Examples:
      | field    | fieldValue | identityFraudScore | activityHistoryScore | checks |
      | lastName | DUFF       | 2                  | 0                    | mortality_check,identity_theft_check,synthetic_identity_check,impersonation_risk_check |
      | lastName | AHS        | 2                  | 1                    | mortality_check,identity_theft_check,synthetic_identity_check,impersonation_risk_check,activity_history_check |

  @fraudCRI_API @staging
  Scenario Outline: Fraud Check for user with various activity history records(STUB)
    Given user PAUL BUTTIVANT row number 5 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-shared-dev
    Given user changes <field> in session request to <fieldValue> for fraud-cri-shared-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-shared-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore <identityFraudScore>
    And VC should contain activityHistory score of <activityHistoryScore>
    And VC evidence checks should contain <checks>
    And VC evidence activity`history check contains activity from <activityFrom>
    Examples:
      | field    | fieldValue | identityFraudScore | activityHistoryScore | activityFrom | checks |
      | lastName | BUTTIVANT  | 1                  | 1                    | 2013-12-01   | mortality_check,identity_theft_check,synthetic_identity_check,impersonation_risk_check,activity_history_check |

  @fraudCRI_API @pre-merge @dev @LIME-415
  Scenario Outline: Fraud Check for users with potentially fraudulent CIs
    Given user <givenName> <familyName> row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-shared-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-shared-dev
    Then user requests Fraud CRI VC
    And VC should contain ci <ci> and identityFraudScore 2
    Examples:
      | givenName| familyName| ci  |
      | Albert   | CI1       | A01 |
      | Anthony  | CI2       | N01 |
      | Albert   | CI5       | T05 |