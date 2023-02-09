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

#  @intialJWT_happy_path
#  Scenario: Acquire initial JWT (STUB)
#    Given user has the user identity in the form of a signed JWT string for CRI Id fraud-cri-build
#    When user sends a POST request to session end point
#    Then user gets a session-id