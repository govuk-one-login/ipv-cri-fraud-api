@fraud_CRI_API
Feature: Fraud CRI API

  @intialJWT_happy_path @pre-merge @dev
  Scenario: Acquire initial JWT (STUB)
    Given user has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    When user sends a POST request to session endpoint
    Then user gets a session-id
    And user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    And user requests Fraud CRI VC
    And VC should contain identityFraudScore 1

  @intialJWT_happy_path @pre-merge @dev
  Scenario: Fraud Check and PEP check complete(STUB)
#    First test step has to be changed to use a PEP user
    Given user MICHELLE NO_FILE_99 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    When user sends a POST request to session endpoint
    Then user gets a session-id
    And user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    And user requests Fraud CRI VC
    And VC should contain identityFraudScore 1

  @intialJWT_happy_path @pre-merge @dev
  Scenario: Fraud Happy path for user with decision score below 35(STUB)
    Given user ALBERT NO_FILE_35 has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    When user sends a POST request to session endpoint
    Then user gets a session-id
    And user sends a POST request to Fraud endpoint
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    And user requests Fraud CRI VC
#    And VC should contain identityFraudScore 1
#    AND the PEP check is not completed

  @intialJWT_happy_path @pre-merge @dev
  Scenario: User found on mortality record(STUB)
    Given user on a mortality record has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    When user sends a POST request to session endpoint
    Then user gets a session-id
    And user sends a POST request to Fraud endpoint
#    AND one of the following U-codes is returned
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    And user requests Fraud CRI VC
    And VC should contain identityFraudScore 0
#  AND the PEP check is not completed

  @intialJWT_happy_path @pre-merge @dev
#    not clear
  Scenario: Fraud check and PEP check complete but Name and DoB of user matches the record of a PEP
#    Given PEP user has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    When user sends a POST request to session endpoint
    Then user gets a session-id
    And user sends a POST request to Fraud endpoint
#    AND the PEP check is completed
#    AND the U-code: U134 is returned
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    And user requests Fraud CRI VC
    And VC should contain identityFraudScore 2

  @intialJWT_happy_path @pre-merge @dev
  Scenario: Users address possibly belongs to someone else / Home telephone supplied does not match database / Potential developed identity
#    (Users address possibly belongs to someone else / Home telephone supplied does not match database / Potential developed identity)
#    Given user has the user identity in the form of a signed JWT string for CRI Id fraud-cri-dev
    When user sends a POST request to session endpoint
    Then user gets a session-id
    And user sends a POST request to Fraud endpoint
#    AND one of the following U-codes is returned
#    AND the PEP check is completed
    And user gets authorisation code
    And user sends a POST request to Access Token endpoint fraud-cri-dev
    And user requests Fraud CRI VC
    And VC should contain identityFraudScore 2


#  @intialJWT_happy_path
#  Scenario: Acquire initial JWT (STUB)
#    Given user has the user identity in the form of a signed JWT string for CRI Id fraud-cri-build
#    When user sends a POST request to session end point
#    Then user gets a session-id