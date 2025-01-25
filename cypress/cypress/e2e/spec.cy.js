
/* * Test suite to verify the operational status of the application at 'http://localhost:8070/Geoweaver'. */
Cypress.on('uncaught:exception', (err, runnable) => {
  // Returning false prevents Cypress from failing the test
  console.error('Uncaught exception occurred:', err);
  return false;
});

describe('Application Build Check', () => {
  it('Application is up and running', () => {
    cy.request('http://localhost:8070/Geoweaver')
      .then((response) => {
        // Assert the HTTP status code is 200
        expect(response.status).to.eq(200);
      });
  });
  it('Navigating through intro', () => {
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    cy.get('.introjs-nextbutton').click();
    cy.get('.introjs-nextbutton').click();
    cy.get('.introjs-nextbutton').click();
    cy.get('.introjs-nextbutton').click();
    cy.get('.introjs-nextbutton').click();
    cy.get('#main-general-content').click();
    cy.get('.lead > b').should('be.visible');
  })
});

// describe('User Signup Test', () => {
//   it('Test unsuccessful signup without email address', () => {

//     cy.visit('http://localhost:8070/Geoweaver');
//     cy.get('.introjs-skipbutton').click();

//     cy.get('#toolbar-loginout-a > .fa').click()

//     cy.get('[onclick="GW.user.signupdialog()"]').click()

//     cy.get('#username').type('newuser'); 
//     cy.get('input[name="username"]') 
   
//     cy.get('#password').type('Geoweaver@123'); 

//     cy.get('#rpassword').type('Geoweaver@123');  
 
//     cy.get('#agree_yes').click();
  
//     cy.get('[onclick="GW.user.register()"]').click();
//     cy.contains("Email is missing!");
//   });


//   it('Test unsuccessful signup if user didnot agree for the terms and conditions', () => {

//     cy.visit('http://localhost:8070/Geoweaver');
//     cy.get('.introjs-skipbutton').click();

//     cy.get('#toolbar-loginout-a > .fa').click()

//     cy.get('[onclick="GW.user.signupdialog()"]').click()

//     cy.get('#username').type('newuser'); 
//     cy.get('input[name="username"]') 
   
//     cy.get('#password').type('Geoweaver@123'); 

//     cy.get('#rpassword').type('Geoweaver');  
//     cy.get('input[name="email"]').type('newuser@example.com');  
//     cy.get('#agree_no').click()
  
//     cy.get('[onclick="GW.user.register()"]').click()
//     cy.contains("The reentered password doesn't match!")
//   });
// });

// describe('User Login Test', () => {
//   it('allows an existing user to log in', () => {
//     cy.visit('http://localhost:8070/Geoweaver');
//     cy.get('.introjs-skipbutton').click();

//     cy.get('#toolbar-loginout-a > .fa').click();

//     cy.get('[onclick="GW.user.logindialog()"]', { timeout: 10000 }).should('be.visible');
  
//     cy.get('#username').type('newuser'); 
//     cy.get('#password').type('Geoweaver@123'); 

//     cy.get('[onclick="GW.user.login()"]').click();
//   });

//   it('Test unsuccessful Login with incorrect password', () => {
//     cy.visit('http://localhost:8070/Geoweaver'); 
//     cy.get('.introjs-skipbutton').click();
//     cy.get('#toolbar-loginout-a > .fa').click();
    
//     cy.get('#username').type('testuser'); 
//     cy.get('#password').type('wrongPassword123'); 

//     cy.get('[onclick="GW.user.login()"]').click();

//     cy.contains("Failed to log in.")
//   });

//   it('Test unsuccessful Login without password', () => {
//     cy.visit('http://localhost:8070/Geoweaver'); 
//     cy.get('.introjs-skipbutton').click();
//     cy.get('#toolbar-loginout-a > .fa').click();
    
//     cy.get('#username').type('testuser'); 


//     cy.get('[onclick="GW.user.login()"]').click();

//     cy.contains("Either usename or password is missing.")
//   });
// });

// describe('Forgot Password Test', () => {
//   it('forgot password test', () => {
//     cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
//     cy.get('.introjs-skipbutton').click();
//     cy.get('#toolbar-loginout-a > .fa').click();
//     cy.get(':nth-child(4) > .col-md-12 > .btn').click();
//     cy.get('#email').type('newuser@example.com');
//     cy.get('#reset-password-form > :nth-child(1) > .col-md-12').click();
//     cy.get('[onclick="GW.user.resetPassword()"]').click();
//     cy.get('#reset-password-form > h3').click();
//     cy.get('#reset-password-form').click();
//     cy.get('[style="border-bottom-right-radius: 5px; border-bottom-left-radius: 5px; visibility: visible; position: absolute; left: 0px; box-sizing: content-box; top: 0px; width: 100%; height: 100%; background-color: rgb(255, 255, 255); overflow: auto;"]').click();
//     cy.get('#reset-password-form > h3').click();
//     cy.get('#reset-password-form > h3').should('be.visible');
//     cy.get('#reset-password-form > :nth-child(3) > a').should('be.visible');
//   }) 
// })

describe('Navigation', () => {
  it('should navigate to different pages', () => {
    cy.visit('http://localhost:8070/Geoweaver'); 
    cy.get('.introjs-skipbutton').click();
    
    cy.contains('Log' , { timeout: 10000 }).click();
    cy.contains('Logging').should('be.visible');

    cy.contains('Status' , { timeout: 10000 }).click();
    cy.contains('Real Time Status').should('be.visible');
    
    cy.contains('Guide' , { timeout: 10000 }).click();
    cy.contains('Geoweaver User Guide').should('be.visible');

    cy.contains('Tools' , { timeout: 10000 }).click();
    cy.get('[data-intro="All the other tools"]').click();
    cy.get('[onclick="GW.feedback.showDialog()"]').click();

    cy.get(':nth-child(5) > .btn').should('be.visible');

    cy.contains('History' , { timeout: 10000 }).click();
    cy.contains('Recent History').should('be.visible');

    cy.contains('Contact' , { timeout: 10000 }).click();
    cy.contains('Have feedback?').should('be.visible');

    cy.contains('About' , { timeout: 10000 }).click();
    cy.contains('Geoweaver is a web system').should('be.visible');
  });   
});


describe('Host Testing', () => {
  it('should submit a create new host form successfully', () => {
        cy.visit('http://localhost:8070/Geoweaver'); 
        cy.get('.introjs-skipbutton').click();
        cy.get('#newhost').click();
    
        cy.get('#hostip').type('1.1.1.1');
        cy.get('#hostport').type('8000');
        cy.get('#username').type('newuser');
    
        cy.get('#host-add-btn').click();
        cy.get('.nav-side-menu').contains('New Host')
      });
  it('Search result should be successful after creating the host', () => {
    /* ==== Generated with Cypress Studio ==== */
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('[data-intro="All the other tools"]').click();
    cy.get('#toolbar-search').click();
    cy.get('#keywords').clear('New Host');
    cy.get('#keywords').type('New Host');
    cy.get('#search').click();
    cy.get('tbody > :nth-child(1) > :nth-child(1) > a').click();
    cy.get('tbody > :nth-child(1) > :nth-child(1) > a').click();
    cy.get('tbody > :nth-child(1) > :nth-child(1) > a').should('be.visible');

    cy.get(
      '[style="position: absolute; top: -18px; left: 8px; width: 8px; height: 8px; cursor: pointer; margin: 0px; padding: 0px; box-sizing: content-box; font-family: sans-serif; text-align: center; font-size: 8px; line-height: 8px; border-width: 1px; border-radius: 5px; border-color: rgb(252, 97, 92); border-style: solid; background-color: rgb(252, 97, 92); color: white; z-index: 50; user-select: none;"]'
    ).click();

    cy.get(
      '[style="position: absolute; top: -18px; left: 8px; width: 8px; height: 8px; cursor: pointer; margin: 0px; padding: 0px; box-sizing: content-box; font-family: sans-serif; text-align: center; font-size: 8px; line-height: 8px; border-width: 1px; border-radius: 5px; border-color: rgb(252, 97, 92); border-style: solid; background-color: rgb(252, 97, 92); color: white; z-index: 50; user-select: none;"]'
    ).click();
  })


//   it('Global Search for Host', () => {
//   cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
//   cy.get('.introjs-skipbutton').click();
//   cy.get('#instant_search_bar').clear('N');
//   cy.get('#instant_search_bar').type('New ');
//   cy.get('#host-b4ijvz').should('be.visible');
//   })
});

describe('Process Testing', () => {

    it('Create Python Process', () => {
      cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
      cy.get('.introjs-skipbutton').click();
      cy.get('#newprocess').click();
      cy.get('form select.form-control.form-control-sm').select('Python');
      cy.get('form > :nth-child(1) > :nth-child(4)').clear('t');
      cy.get('form > :nth-child(1) > :nth-child(4)').type('python_test');
      cy.get('.modal-footer').contains('Add').click();
      cy.get('ul#process_folder_python_target').should('contain', 'python_test');
    })

});


describe('Add Process to Weaver', () => {
  it('Add to weaver - python', () => {
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('#process_folder_python').click();
    cy.get('ul#process_folder_python_target').contains('button', 'Add to Weaver').click();
    cy.get('circle').should('be.visible');
  })
});



describe('Delete Process', () => {
    it('Delete Python Process', () => {
      cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
      cy.get('.introjs-skipbutton').click();
      cy.get('#process_folder_python').click();
      cy.get('ul#process_folder_python_target').contains('python_test').click();
      cy.contains('button', 'Delete').click();
      cy.get('#del-confirm-btn').click();
      cy.get('#main-general-content').click();
      cy.get('[style="color:rgb(38, 90, 139);text-align:center;font-family:\'lato\', sans-serif;font-size:80px"]').should('be.visible');
    })

});

describe('Create Python process and run it', () => {
  it('creates python process and runs test', function() {
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('#process_folder_python').click();
    cy.get('#newprocess').click();
    cy.get('[id^="processcategory-"]').select('python');
    cy.get('[id^="processname-"]').clear().type('check_this'); 
    cy.get('.view-lines').click();
    cy.get('[id^="add-process-"]').last().click(); 
    cy.get('[id^="process-"] > .row > .col-md-8 > span').last().click(); 
    cy.get('[onclick*="runProcess"]').last().click();
    cy.get('#host-execute-btn').click();
    cy.get('#process-confirm-btn').click();
    cy.get('#inputpswd').clear().type('1234'); 
    cy.get('#pswd-confirm-btn').click();
    cy.get('#process-log-window > :nth-child(3)').click();
    cy.get('#process-log-window > :nth-child(3) > span').should('be.visible');
  });
});



describe('Hosts Testing', () => {
  it('Create New Host', () => {
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('#newhost').click();
    cy.get('#hostip').clear('1');
    cy.get('#hostip').type('1.1.1.1');
    cy.get('#hostport').clear('2');
    cy.get('#hostport').type('22');
    cy.get('#username').clear('n');
    cy.get('#username').type('newuser');
    cy.get('#host-add-btn').click();
    cy.get('ul#host_folder_ssh_target').should('contain', 'New Host');
  }) 

  it('Delete New Host', () => {
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('#host_folder_ssh > a').click();
    cy.get('ul#host_folder_ssh_target').contains('New Host').click();
    cy.get('.fa-minus').click();
    cy.get('#del-confirm-btn').click();
  })

  it('LocalHost testing', () => {
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('#host_folder_ssh > a').click();
    cy.get('#host-100001').click();
    cy.get('#_host_name').should('have.value', 'Localhost');
    cy.get('#_host_ip').should('have.value', '127.0.0.1');
    cy.get('#_host_port').should('have.value', '22');
    cy.get('#_host_username').should('have.value', 'publicuser');
    cy.get('#_host_url').should('have.value', 'http://localhost/');
    cy.get('#_host_type').click();
    cy.get('#_host_type').should('have.text', 'ssh');
  })

  it('LocalHost Read Python Env', () => {
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('#host_folder_ssh > a').click();
    cy.get('#host-100001').click();
    cy.get('.fab').click();
    cy.get('#inputpswd').clear('1');
    cy.get('#inputpswd').type('1234');
    cy.get('#pswd-confirm-btn').click();
    cy.intercept('POST', '/Geoweaver/web/readEnvironment').as('readEnvironment');
    cy.wait('@readEnvironment').then((interception) => {
      expect(interception.response.statusCode).to.equal(200);
    });
  })

  it('LocalHost File Upload', () => {
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('#host_folder_ssh > a').click();
    cy.get('#host-100001').click();
    cy.get('p > .fa-upload').click();
    cy.get('#inputpswd').clear('1');
    cy.get('#inputpswd').type('1234');
    cy.get('#pswd-confirm-btn').click();
    cy.get('#host-file-uploader').click();
    cy.intercept('POST', 'http://localhost:8070/Geoweaver/web/authenticateUser').as('authenticateUser');
    cy.wait('@authenticateUser').its('response.statusCode').should('eq', 200);
  })

});
