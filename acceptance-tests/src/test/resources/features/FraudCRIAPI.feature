@fraud_CRI_API @V2
Feature: Fraud CRI API

  @pre-merge @dev
  Scenario: Happy Path with KENNETH DECERQUEIRA
    Given user KENNETH DECERQUEIRA row number 197 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 2
    And VC is for person KENNETH DECERQUEIRA

  @pre-merge @dev
  Scenario: Acquire initial JWT and Fraud Check with PEP error response failure(STUB)
    Given user ALBERT PEP_ERROR_RESPONSE row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 1

  @pre-merge @dev
  Scenario: Acquire initial JWT and Fraud Check with PEP tech failure(STUB)
    Given user ALBERT PEP_TECH_FAIL row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 1

  @pre-merge @dev
  Scenario: Fraud Check Succeeds and PEP Succeeds but is not PEP (HOLLINGDALU)
    Given user ALBERT HOLLINGDALU row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 2

  @pre-merge @dev
  Scenario: Fraud Check and PEP check complete(STUB)
#    VC for Fraud Succeeds and PEP Succeeds but is PEP (PEPS)
    Given user ALBERT PEPS row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci P01 and identityFraudScore 2

  @pre-merge @dev
  Scenario: Fraud Happy path for user with decision score below 35(STUB)
    Given user ALBERT NO_FILE_35 row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 1

  @pre-merge @dev
  Scenario: Fraud Check for user found on mortality record(STUB)
    Given user ALBERT GILT row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci T02 and identityFraudScore 0

  @pre-merge @dev
  Scenario Outline: Fraud Check for user with various activity history records(STUB)
    Given user LINDA DUFF row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    Given user changes <field> in session request to <fieldValue> for fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore <identityFraudScore>
    And VC should contain activityHistory score of <activityHistoryScore>
    And VC evidence checks should contain <checks>
    Examples:
      | field    | fieldValue | identityFraudScore | activityHistoryScore | checks |
      | lastName | DUFF       | 2                  | 0                    | mortality_check,identity_theft_check,synthetic_identity_check,impersonation_risk_check |
      | lastName | AHS        | 2                  | 1                    | mortality_check,identity_theft_check,synthetic_identity_check,impersonation_risk_check,activity_history_check |

  @staging
  Scenario Outline: Fraud Check for user with various activity history records(STUB)
    Given user PAUL BUTTIVANT row number 5 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    Given user changes <field> in session request to <fieldValue> for fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore <identityFraudScore>
    And VC should contain activityHistory score of <activityHistoryScore>
    And VC evidence checks should contain <checks>
    And VC evidence activity`history check contains activity from <activityFrom>
    Examples:
      | field    | fieldValue | identityFraudScore | activityHistoryScore | activityFrom | checks |
      | lastName | BUTTIVANT  | 1                  | 1                    | 2013-12-01   | mortality_check,identity_theft_check,synthetic_identity_check,impersonation_risk_check,activity_history_check |

  @pre-merge @dev @LIME-415
  Scenario Outline: Fraud Check for users with potentially fraudulent CIs
    Given user <givenName> <familyName> row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci <ci> and identityFraudScore 2
    Examples:
      | givenName| familyName| ci  |
      | Albert   | CI1       | A01 |
      | Anthony  | CI2       | N01 |
      | Albert   | CI5       | T05 |

  @pre-merge @dev
  Scenario: User initiates a duplicate check when one is already in progress
    Given user ALBERT PEP_ERROR_RESPONSE row number 6 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user sends a second POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 1

#  @fraudCRI_API @pre-merge @dev
  Scenario: HappyPath Authenticate has delays enabled (FWAIT_INTIME)
    Given user KENNETH FWAIT_INTIME row number 197 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 2
    And VC is for person KENNETH FWAIT_INTIME

#  @fraudCRI_API @pre-merge @dev
  Scenario: FailPath Authenticate Times-out (FWAIT_OUTOFTIME)
    Given user KENNETH FWAIT_OUTOFTIME row number 197 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint and the API returns the error {"oauth_error":{"error_description":"Unexpected server error","error":"server_error"}

#  @fraudCRI_API @pre-merge @dev
  Scenario: HappyPath PEP has delays enabled (PWAIT_INTIME)
    Given user KENNETH PWAIT_INTIME row number 197 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 2
    And VC is for person KENNETH PWAIT_INTIME

#  @fraudCRI_API @pre-merge @dev
  Scenario: FailPath PEP Times-out (PWAIT_OUTOFTIME)
    Given user KENNETH PWAIT_OUTOFTIME row number 197 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 1
    And VC is for person KENNETH PWAIT_OUTOFTIME

#  @fraudCRI_API @pre-merge @dev
  Scenario: HappyPath both API's have delays enabled (ALL_WAIT_INTIME)
    Given user KENNETH ALL_WAIT_INTIME row number 197 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC should contain ci  and identityFraudScore 2
    And VC is for person KENNETH ALL_WAIT_INTIME

#  @fraudCRI_API @pre-merge @dev
  Scenario Outline: FailPath PEP Times-out user has activity history (AHS_PEP_TECH_FAIL)
    Given user KENNETH AHS_PEP_TECH_FAIL row number 197 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    And user sends a POST request to session endpoint
    And user gets a session-id
    When user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    Then user requests Fraud CRI VC
    And VC is for person KENNETH AHS_PEP_TECH_FAIL
    And VC should contain ci  and identityFraudScore <identityFraudScore>
    And VC should contain activityHistory score of <activityHistoryScore>
    And VC evidence checks should contain <checks>
    Examples:
      | identityFraudScore | activityHistoryScore | checks |
      | 1                  | 1                    | mortality_check,identity_theft_check,synthetic_identity_check,activity_history_check |
