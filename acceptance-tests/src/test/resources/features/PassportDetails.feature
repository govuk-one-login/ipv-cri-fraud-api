Feature: Passport Test


  @passport_test
  Scenario Outline: Passport details page
    Given A user is on Prove Your Identity page
    And A user enters "<Passport number>", "<Surname>", "<Given names>", "<birthDay>", "<birthMonth>", "<birthYear>", "<expiryDay>","<expiryMonth>" and "<expiryYear>"
    Then user sees DCS check is complete message
    Examples:
      | Passport number | Surname | Given names  | birthDay | birthMonth | birthYear | expiryDay | expiryMonth | expiryYear |
      | 824159121       | Watson  | Mary         | 01       | 03         | 2021      | 01        | 01          | 2030       |
      | 824159122       | Gok     | Hakan Thomas | 03       | 12         | 1980      | 01        | 01          | 2030       |


  Scenario Outline: Add Passport to stub for testing
    Given I navigate to the IPV Core Stub
    And I navigate to User for Passport CRI dev Page
    When I enter number 5 and click Go to Passport CRI Dev button
    And A user enters the passport details "<Passport number>", "<Surname>", "<Given names>", "<birthDay>", "<birthMonth>", "<birthYear>", "<expiryDay>","<expiryMonth>" and "<expiryYear>"
    Then I validate the link Response from Passport CRI dev
    And  I navigate to the verifiable issuer to check for valid response from Passport CRI dev
    Examples:
      | Passport number | Surname     | Given names  | birthDay | birthMonth | birthYear | expiryDay | expiryMonth | expiryYear |
      | 321654987       | DECERQUEIRA | KENNETH      | 23       | 8         | 1959      | 27        | 5           | 2029        |



