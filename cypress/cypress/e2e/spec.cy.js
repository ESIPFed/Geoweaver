
/* * Test suite to verify the operational status of the application at 'http://localhost:8070/Geoweaver'. */

describe('Application Build Check', () => {
  it('Application is up and running', () => {
    cy.request('http://localhost:8070/Geoweaver')
      .then((response) => {
        // Assert the HTTP status code is 200
        expect(response.status).to.eq(200);
      });
  });
});


describe('User Signup Test', () => {
  it('Test unsuccessful signup with already registered email address', () => {

    cy.visit('http://localhost:8070/Geoweaver');
    cy.get('.introjs-skipbutton').click();

    cy.get('#toolbar-loginout-a > .fa').click()

    cy.get('[onclick="GW.user.signupdialog()"]', { timeout: 10000 }).should('be.visible');
    cy.get('[onclick="GW.user.signupdialog()"]').click()

    cy.get('#username').type('newuser'); 
    cy.get('input[name="username"]');
   
    cy.get('#password').type('Geoweaver@123'); 

    cy.get('#rpassword').type('Geoweaver@123');  
    cy.get('input[name="email"]').type('newuser@example.com'); 
    cy.get('#agree_yes').click()

    // Submit the form
  
    cy.get('[onclick="GW.user.register()"]').click()
    cy.contains("the email address has already been registered")
  });

  it('Test unsuccessful signup without email address', () => {

    cy.visit('http://localhost:8070/Geoweaver');
    cy.get('.introjs-skipbutton').click();

    cy.get('#toolbar-loginout-a > .fa').click()

    cy.get('[onclick="GW.user.signupdialog()"]').click()

    cy.get('#username').type('newuser'); 
    cy.get('input[name="username"]') 
   
    cy.get('#password').type('Geoweaver@123'); 

    cy.get('#rpassword').type('Geoweaver@123');  
 
    cy.get('#agree_yes').click();
  
    cy.get('[onclick="GW.user.register()"]').click();
    cy.contains("Email is missing!");
  });


  it('Test unsuccessful signup if user didnot agree for the terms and conditions', () => {

    cy.visit('http://localhost:8070/Geoweaver');
    cy.get('.introjs-skipbutton').click();

    cy.get('#toolbar-loginout-a > .fa').click()

    cy.get('[onclick="GW.user.signupdialog()"]').click()

    cy.get('#username').type('newuser'); 
    cy.get('input[name="username"]') 
   
    cy.get('#password').type('Geoweaver@123'); 

    cy.get('#rpassword').type('Geoweaver');  
    cy.get('input[name="email"]').type('newuser@example.com');  
    cy.get('#agree_no').click()
  
    cy.get('[onclick="GW.user.register()"]').click()
    cy.contains("The reentered password doesn't match!")
  });



});

describe('User Login Test', () => {
  it('allows an existing user to log in', () => {
    cy.visit('http://localhost:8070/Geoweaver');
    cy.get('.introjs-skipbutton').click();

    cy.get('#toolbar-loginout-a > .fa').click();

    cy.get('[onclick="GW.user.logindialog()"]', { timeout: 10000 }).should('be.visible');
  
    cy.get('#username').type('newuser'); 
    cy.get('#password').type('Geoweaver@123'); 

    cy.get('[onclick="GW.user.login()"]').click();
  });

  it('Test unsuccessful Login with incorrect password', () => {
    cy.visit('http://localhost:8070/Geoweaver'); 
    cy.get('.introjs-skipbutton').click();
    cy.get('#toolbar-loginout-a > .fa').click();
    
    cy.get('#username').type('testuser'); 
    cy.get('#password').type('wrongPassword123'); 

    cy.get('[onclick="GW.user.login()"]').click();

    cy.contains("Failed to log in.")
  });

  it('Test unsuccessful Login without password', () => {
    cy.visit('http://localhost:8070/Geoweaver'); 
    cy.get('.introjs-skipbutton').click();
    cy.get('#toolbar-loginout-a > .fa').click();
    
    cy.get('#username').type('testuser'); 


    cy.get('[onclick="GW.user.login()"]').click();

    cy.contains("Either usename or password is missing.")
  });
});

