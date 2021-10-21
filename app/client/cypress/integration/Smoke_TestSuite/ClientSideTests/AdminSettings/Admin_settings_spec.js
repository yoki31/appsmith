describe("Admin settings page", function() {
  it("should test settings page is accessible to super", () => {
    cy.visit("/applications");
    cy.get(".t-profile-menu-trigger").should("be.visible");
    cy.get(".t-profile-menu-trigger").click();
    cy.get(".t--admin-settings-menu").should("be.visible");
    cy.get(".t--admin-settings-menu").click();
  });
});
