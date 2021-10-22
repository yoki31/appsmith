const jsEditorLocators = require("../locators/JSEditor.json");

describe("Admin settings page", function() {
  // it("should test that settings page is accessible to super user", () => {
  //   cy.LogOut();
  //   cy.LoginFromAPI(Cypress.env("USERNAME"), Cypress.env("PASSWORD"));
  //   cy.visit("/applications");
  //   cy.get(".t-profile-menu-trigger").should("be.visible");
  //   cy.get(".t-profile-menu-trigger").click();
  //   cy.get(".t--admin-settings-menu").should("be.visible");
  //   cy.get(".t--admin-settings-menu").click();
  //   cy.url().should("contain", "/settings/general");
  // });

  // it("should test that settings page is not accessible to normal users", () => {
  //   cy.LogOut();
  //   cy.LoginFromAPI(Cypress.env("TESTUSERNAME1"), Cypress.env("TESTPASSWORD1"));
  //   cy.visit("/applications");
  //   cy.get(".t-profile-menu-trigger").should("be.visible");
  //   cy.get(".t-profile-menu-trigger").click();
  //   cy.get(".t--admin-settings-menu").should("not.exist");
  //   cy.visit("/settings/general");
  //   // non super users are redirected to home page
  //   cy.url().should("contain", "/applications");
  // });

  describe("", () => {
    this.beforeAll(() => {
      cy.LogOut();
      cy.LoginFromAPI(Cypress.env("USERNAME"), Cypress.env("PASSWORD"));
    });

    // it("should test that settings page is redirected to default tab", () => {
    //   cy.visit("/settings");
    //   cy.url().should("contain", "/settings/general");
    // });

    it("should test that settings page tab redirects", () => {
      cy.visit("/settings/general");
    });
  });
});
