@android @login
Feature: Login functionality

  @smoke @MDA-1234
  Scenario: Valid login navigates to home screen
    Given the app is launched on login screen
    When I enter username "bod@example.com"
    And I enter password "10203040"
    And I tap the login button
    Then the home screen should be displayed