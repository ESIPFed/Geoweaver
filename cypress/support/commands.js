// ***********************************************
 // Custom Cypress commands for Geoweaver E2E tests
 // ***********************************************

/**
 * Login to Geoweaver with localhost password when login_required is enabled.
 */
Cypress.Commands.add('loginIfRequired', () => {
  cy.request({
    url: 'http://localhost:8070/Geoweaver/web/key',
    method: 'GET',
    failOnStatusCode: false,
  }).then((keyResponse) => {
    if (keyResponse.status === 200 && keyResponse.body && keyResponse.body.publicKey) {
      const publicKey = keyResponse.body.publicKey;
      const password = Cypress.env('localhost_password') || '123456';

      cy.window().then((win) => {
        if (!win.JSEncrypt) {
          cy.log('JSEncrypt not found in window; UI login path will be used if needed');
          return;
        }

        const encrypt = new win.JSEncrypt();
        encrypt.setPublicKey(publicKey);
        const encryptedPassword = encrypt.encrypt(password);

        cy.request({
          url: 'http://localhost:8070/Geoweaver/web/authenticateLocalhost',
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: `encryptedPassword=${encodeURIComponent(encryptedPassword)}`,
          followRedirect: false,
          failOnStatusCode: false,
        }).then(() => {
          cy.log('API login completed');
        });
      });
    } else {
      cy.log('Login not required, skipping authentication');
    }
  });
});

/**
 * Dismiss IntroJS if it is present. Safe when intro never appears
 * (e.g. after login redirect races, or intro disabled).
 */
Cypress.Commands.add('skipIntroIfPresent', () => {
  cy.get('body', { timeout: 15000 }).should('be.visible');
  // IntroJS starts asynchronously after GW.main.init
  cy.wait(1500);
  cy.get('body').then(($body) => {
    if ($body.find('.introjs-overlay').length === 0) {
      return;
    }
    if ($body.find('.introjs-skipbutton').length > 0) {
      cy.get('.introjs-skipbutton').click({ force: true });
    } else if ($body.find('.introjs-donebutton').length > 0) {
      cy.get('.introjs-donebutton').click({ force: true });
    } else if ($body.find('.introjs-nextbutton').length > 0) {
      for (let i = 0; i < 8; i++) {
        cy.get('body').then(($b) => {
          if ($b.find('.introjs-nextbutton').length > 0) {
            cy.get('.introjs-nextbutton').click({ force: true });
          } else if ($b.find('.introjs-donebutton').length > 0) {
            cy.get('.introjs-donebutton').click({ force: true });
          }
        });
      }
    }
  });
  cy.get('.introjs-overlay').should('not.exist');
});

/**
 * Visit Geoweaver, handle localhost login when required, then dismiss intro.
 */
Cypress.Commands.add('visitGeoweaver', (url = 'http://localhost:8070/Geoweaver/web/geoweaver') => {
  cy.visit(url);

  cy.url({ timeout: 15000 }).then((currentUrl) => {
    if (currentUrl.includes('/localhost-login')) {
      const password = Cypress.env('localhost_password') || '123456';
      cy.get('input[type="password"]', { timeout: 10000 }).should('be.visible').clear().type(password);
      cy.get('button').contains(/login/i, { timeout: 10000 }).click();
      cy.url({ timeout: 15000 }).should('not.include', '/localhost-login');
    }
  });

  cy.get('body', { timeout: 15000 }).should('be.visible');
  // Side menu is a reliable signal that the main app shell loaded
  cy.get('.nav-side-menu', { timeout: 20000 }).should('exist');
  cy.skipIntroIfPresent();
});
