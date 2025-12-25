// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************

/**
 * Login to Geoweaver with localhost password
 * This command handles the login flow if login_required is enabled
 */
Cypress.Commands.add('loginIfRequired', () => {
  // First, try to get the RSA public key
  cy.request({
    url: 'http://localhost:8070/Geoweaver/web/key',
    method: 'GET'
  }).then((keyResponse) => {
    if (keyResponse.status === 200 && keyResponse.body && keyResponse.body.publicKey) {
      // Login is required, perform login
      const publicKey = keyResponse.body.publicKey;
      const password = Cypress.env('localhost_password') || '123456'; // Default password for testing
      
      // Encrypt password using JSEncrypt (we'll need to do this in the browser context)
      cy.window().then((win) => {
        // Load JSEncrypt if not already loaded
        if (!win.JSEncrypt) {
          cy.log('JSEncrypt not found, attempting to load it');
          return cy.wrap(null);
        }
        
        const encrypt = new win.JSEncrypt();
        encrypt.setPublicKey(publicKey);
        const encryptedPassword = encrypt.encrypt(password);
        
        // Perform login
        cy.request({
          url: 'http://localhost:8070/Geoweaver/web/authenticateLocalhost',
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: `encryptedPassword=${encodeURIComponent(encryptedPassword)}`,
          followRedirect: false
        }).then((loginResponse) => {
          // Login should succeed
          cy.log('Login completed');
        });
      });
    } else {
      // Login not required, skip
      cy.log('Login not required, skipping authentication');
    }
  });
});

/**
 * Visit Geoweaver page and handle login if required
 */
Cypress.Commands.add('visitGeoweaver', (url = 'http://localhost:8070/Geoweaver/web/geoweaver') => {
  cy.visit(url);
  
  // Check if we're on the login page
  cy.url({ timeout: 5000 }).then((currentUrl) => {
    if (currentUrl.includes('/localhost-login')) {
      // We're on login page, need to login
      cy.get('input[type="password"]', { timeout: 10000 }).should('be.visible');
      
      // Get the password from env or use default
      const password = Cypress.env('localhost_password') || '123456';
      
      // Fill in password and submit
      cy.get('input[type="password"]').type(password);
      cy.get('button').contains('Login', { timeout: 10000 }).click();
      
      // Wait for redirect to main page
      cy.url({ timeout: 10000 }).should('not.include', '/localhost-login');
    }
  });
  
  // Wait for page to fully load
  cy.get('body', { timeout: 10000 }).should('be.visible');
  
  // Wait a bit more for IntroJS to potentially start
  cy.wait(1500);
});