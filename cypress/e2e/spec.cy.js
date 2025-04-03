
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
  // it('Test unsuccessful signup with already registered email address', () => {

  //   cy.visit('http://localhost:8070/Geoweaver');
  //   cy.get('.introjs-skipbutton').click();

  //   cy.get('#toolbar-loginout-a > .fa').click()

  //   cy.get('[onclick="GW.user.signupdialog()"]', { timeout: 10000 }).should('be.visible');
  //   cy.get('[onclick="GW.user.signupdialog()"]').click()

  //   cy.get('#username').type('newuser'); 
  //   cy.get('input[name="username"]');
   
  //   cy.get('#password').type('Geoweaver@123'); 

  //   cy.get('#rpassword').type('Geoweaver@123');  
  //   cy.get('input[name="email"]').type('newuser@example.com'); 
  //   cy.get('#agree_yes').click()

  //   // Submit the form
  
  //   cy.get('[onclick="GW.user.register()"]').click()
  //   cy.contains("the email address has already been registered")
  // });

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
});

describe('Process Testing', () => {
  it('Create Shell Process', () => {
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('#newprocess').click().then(() => {
      // Ensure the window or form is fully loaded and active
      cy.get('form', { timeout: 10000 }).should('be.visible'); // Wait until the form is visible
    });
    cy.get('form > :nth-child(1) > :nth-child(4)').clear('t');
    cy.get('form > :nth-child(1) > :nth-child(4)').type('shell_test').then(
      () => {
        cy.get('.new-process-code-area', { timeout: 10000 }).should('be.visible');
      }
    );
    cy.get('.modal-footer').contains('Add').click().then(
      () => {
        cy.get('ul#process_folder_shell_target', { timeout: 10000 }).should('be.visible');
      }
    );
    cy.get('ul#process_folder_shell_target').should('contain', 'shell_test');
  })

  it('Create Python Process', () => {
    // Visit the Geoweaver web page
    cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
    
    // Skip intro and go to process creation
    cy.get('.introjs-skipbutton').click();
    cy.get('#newprocess').click();
  
    // Ensure that the new process code area is visible before proceeding
    cy.get(".new-process-code-area", { timeout: 10000 }).should("be.visible");
  
    // Select Python from the form dropdown
    cy.get('form select.form-control.form-control-sm').select('Python');
  
    // Clear the input field and type the new process name
    cy.get('form > :nth-child(1) > :nth-child(4)').clear().type('python_test_2');
  
    // Click the "Add" button and ensure the process folder is visible
    cy.get('.modal-footer').contains('Add').click();
  
    // Use cy.wait() to ensure any delays are accounted for before checking the process folder
    cy.wait(3000);  // You can increase this value if necessary to ensure the process list is visible
  
    // Ensure the process folder is visible and contains the new process
    cy.get('ul#process_folder_python_target', { timeout: 10000 }).should('be.visible');
    cy.get('ul#process_folder_python_target').should('contain', 'python_test_2');
  });
  

});


describe('Add Process to Weaver', () => {
  it('Add to weaver', () => {
    cy.visit('http://localhost:8070/Geoweaver');
    cy.get('.introjs-skipbutton').click().then(
      () => {
        cy.get('#process_folder_shell', { timeout: 10000 }).should('be.visible');
      }
    );
    cy.get('#process_folder_shell').click().then(
      () => {
        cy.get('ul#process_folder_shell_target', { timeout: 10000 }).should('be.visible');
      }
    );
    cy.get('ul#process_folder_shell_target').contains('button', 'Add to Weaver').click();
    cy.get('circle').should('be.visible');
  })
});

describe('Edit Process Name', () => {
  it('Add to weaver', () => {
    cy.visit('http://localhost:8070/Geoweaver');
    cy.get('.introjs-skipbutton').click().then(
      () => {
        cy.get('#process_folder_shell', { timeout: 10000 }).should('be.visible');
      }
    );
    cy.get('#process_folder_shell').click().then(
      () => {
        cy.get('#process_folder_shell', { timeout: 10000 }).should('be.visible');
      }
    );
    cy.get('ul#process_folder_shell_target').contains('shell_test').click().then(
      () => {
        cy.get('#processname', { timeout: 10000 }).should('be.visible');
      }
    );
    cy.get('#processname').should('be.visible').and('not.be.disabled');
    cy.get('#processname').clear();
    cy.get('#processname', { timeout: 10000 }).should('be.visible'); // Waits for up to 10 seconds
    cy.get('#processname').type('updated_shell_test', { force: true }) // Type the text
      .should('have.value', 'updated_shell_test'); // Check the value
    cy.get('.process-edit-right-icon').click();
    cy.get('ul#process_folder_shell_target').should('contain', 'updated_shell_test');

  })
  it('process category and id should be disabled', () => {
    cy.visit('http://localhost:8070/Geoweaver');
    cy.get('.introjs-skipbutton').click().then(
      () => {
        cy.get('#process_folder_shell', { timeout: 10000 }).should('be.visible');
      }
    );
    cy.get('#process_folder_shell').click();
    cy.get('ul#process_folder_shell_target').contains('updated_shell_test').click();
    cy.get('#processcategory').should('be.disabled');
    cy.get('#processid').should('be.disabled')
  })

  it('Create and Edit Python Process', function() {
    cy.visit('http://localhost:8070/Geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('#process_folder_python').click();
    cy.get('#newprocess').click();
    cy.get('form select.form-control.form-control-sm').select('python');
    cy.get('form > :nth-child(1) > :nth-child(4)').clear('te');
    cy.get('form > :nth-child(1) > :nth-child(4)').type('test_edit');

    // Wait for the Monaco editor to load
    cy.window().then((win) => {
      return new Cypress.Promise((resolve) => {
        const checkMonaco = () => {
          const editors = win.monaco?.editor?.getEditors(); // Get all Monaco editors
          cy.log(`Total Monaco Editors found: ${editors?.length || 0}`); // Log editor count
    
          if (editors && editors.length > 0) {
            const visibleEditor = editors.find((editor) => {
              const isVisible = Cypress.$(editor.getDomNode()).is(':visible'); // Check if visible
              cy.log(`Editor visible: ${isVisible}`);
              return isVisible;
            });
    
            if (visibleEditor) {
              cy.log("Using visible Monaco editor");
              resolve(visibleEditor);
            } else {
              cy.log("No visible Monaco editor found, retrying...");
              setTimeout(checkMonaco, 500); // Retry if no visible editor is found
            }
          } else {
            setTimeout(checkMonaco, 500); // Retry if no editors are found
          }
        };
        checkMonaco();
      });
    }).then((editor) => {
      if (editor) {
        cy.log("Found visible Monaco Editor:", editor);
        const model = editor.getModel();
        if (model) {
          cy.wrap(null).should(() => {
            // Use Monaco's API to simulate actual typing
            editor.focus(); // Ensure the editor is focused
            editor.executeEdits("", [{ range: model.getFullModelRange(), text: "print('hello world!')" }]);
            editor.trigger("keyboard", "type", { text: "" }); // Simulate a keystroke to trigger Monaco's change detection
          });
    
          // Wait for the Monaco editor to reflect the change
          cy.wrap(null).should(() => {
            expect(model.getValue()).to.contains("hello world");
          });
        }
      }
    });
    
    cy.get('.view-lines').click();
    cy.get('.modal-footer').contains('Add').click();

    // Wait for the Monaco editor to load
    cy.window().then((win) => {
      return new Cypress.Promise((resolve) => {
        const checkMonaco = () => {
          const editors = win.monaco?.editor?.getEditors(); // Get all Monaco editors
          cy.log(`Total Monaco Editors found: ${editors?.length || 0}`); // Log editor count
    
          if (editors && editors.length > 0) {
            const visibleEditor = editors.find((editor) => {
              const isVisible = Cypress.$(editor.getDomNode()).is(':visible'); // Check if visible
              cy.log(`Editor visible: ${isVisible}`);
              return isVisible;
            });
    
            if (visibleEditor) {
              cy.log("Using visible Monaco editor");
              resolve(visibleEditor);
            } else {
              cy.log("No visible Monaco editor found, retrying...");
              setTimeout(checkMonaco, 500); // Retry if no visible editor is found
            }
          } else {
            setTimeout(checkMonaco, 500); // Retry if no editors are found
          }
        };
        checkMonaco();
      });
    }).then((editor) => {
      if (editor) {
        cy.log("Found visible Monaco Editor:", editor);
        const model = editor.getModel();
        if (model) {
          cy.wrap(null).should(() => {
            // Use Monaco's API to simulate actual typing
            editor.focus(); // Ensure the editor is focused
            editor.executeEdits("", [{ range: model.getFullModelRange(), text: "\ntest edit" }]);
            editor.trigger("keyboard", "type", { text: "" }); // Simulate a keystroke to trigger Monaco's change detection
          });
    
          // Wait for the Monaco editor to reflect the change
          cy.wrap(null).should(() => {
            expect(model.getValue()).to.contains("test edit");
          });
        }
      }
    });
  });
});


describe('Delete Process', () => {
    it('Delete Shell Process', () => {
      cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
      cy.get('.introjs-skipbutton').click().then(
        () => {
          cy.get('#processes', { timeout: 5000 }).should('be.visible');
        }
      );
      cy.get('#process_folder_shell').click().then(
        () => {
          cy.get('#process_folder_shell_target', { timeout: 10000 }).should('be.visible');
        }
      );
      cy.get('ul#process_folder_shell_target').contains('updated_shell_test').click();
      cy.contains('button', 'Delete').click();
      cy.get('#del-confirm-btn').click();
      cy.get('#main-general-content').click();
      cy.get('[style="color:rgb(38, 90, 139);text-align:center;font-family:\'lato\', sans-serif;font-size:80px"]').should('be.visible');
    })

    it('Delete Python Process', () => {
      cy.visit('http://localhost:8070/Geoweaver/web/geoweaver');
      cy.get('.introjs-skipbutton').click().then(
        () => {
          cy.get('#process_folder_python',  { timeout: 10000 }).should("be.visible")
        }
      );
      cy.get('#process_folder_python').click();
      cy.get('ul#process_folder_python_target').contains('python_test_2').click();
      cy.contains('button', 'Delete').click();
      cy.get('#del-confirm-btn').click();
      cy.get('#main-general-content').click();
      cy.get('[style="color:rgb(38, 90, 139);text-align:center;font-family:\'lato\', sans-serif;font-size:80px"]').should('be.visible');
    })

});

describe('Write Password into .secret', () => {
  it('Should write secret to a file', () => {
    // Hash value to be written to the file
    const dataToWrite = '4205c81c1aaafae4406dc56bd6c8b26edeb816c6d18294cf0aeee4a948146e0fa3e7cf0ea3e3a6de0b7fe990d7de28ec3060f953b88e4cef5ade04c12ff917ee';
    const homeDirectory = Cypress.env('home');
   
    cy.log('Home Directory:', homeDirectory); // Debug statement
    const filePath = `${homeDirectory}/gw-workspace/.secret`;
    console.log(filePath)
    cy.debug('Detailed debugging information home dire is ',homeDirectory);
    cy.task('log', 'Logging home dire' + homeDirectory)
    cy.task('log', 'Logging filepath' + filePath)
    cy.log('File Path:', filePath); // Debug statement
    cy.writeFile(filePath, dataToWrite, 'binary')
      .then(() => {
        cy.readFile(filePath).should('contain', dataToWrite);
        cy.readFile(filePath).then((fileContents) => {
          cy.task('log', 'File Contents: ' + fileContents);
          cy.log('File Contents:', fileContents);
        });
      })
  });
});

describe('Create Python process and run it', () => {
  it('creates python process and runs', () => {
    cy.visit('http://localhost:8070/Geoweaver');
    cy.get('.introjs-skipbutton').click();
    cy.get('#newprocess').click();

    cy.get('form select.form-control.form-control-sm').select('Python');
    cy.get('form > :nth-child(1) > :nth-child(4)').type('hello_world.py');

    cy.get('.modal-footer').contains('Add').click();

    cy.get('ul#process_folder_python_target').contains('hello_world.py').click();

    cy.window().then((win) => {
      return new Cypress.Promise((resolve) => {
        const checkMonaco = () => {
          const editors = win.monaco?.editor?.getEditors(); // Get all Monaco editors
          cy.log(`Total Monaco Editors found: ${editors?.length || 0}`); // Log editor count
    
          if (editors && editors.length > 0) {
            const visibleEditor = editors.find((editor) => {
              const isVisible = Cypress.$(editor.getDomNode()).is(':visible'); // Check if visible
              cy.log(`Editor visible: ${isVisible}`);
              return isVisible;
            });
    
            if (visibleEditor) {
              cy.log("Using visible Monaco editor");
              resolve(visibleEditor);
            } else {
              cy.log("No visible Monaco editor found, retrying...");
              setTimeout(checkMonaco, 500); // Retry if no visible editor is found
            }
          } else {
            setTimeout(checkMonaco, 500); // Retry if no editors are found
          }
        };
        checkMonaco();
      });
    }).then((editor) => {
      if (editor) {
        cy.log("Found visible Monaco Editor:", editor);
        const model = editor.getModel();
        if (model) {
          cy.wrap(null).should(() => {
            editor.focus(); // Ensure the editor is focused
            model.setValue("print('hello world!')"); // Set value
            editor.trigger("keyboard", "type", { text: "" }); // Simulate keystroke to force UI update
          });
    
          // **Wait until Monaco editor actually reflects the new value**
          cy.wrap(null).should(() => {
            expect(model.getValue().trim()).to.equal("print('hello world!')");
          });
        }
      }
    });    

    cy.get('#main-process-info', { timeout: 10000 }).should("be.visible")
    
    cy.log("now should change the content")

    cy.get('#processid').then(($input) => {
      const processId = $input.val(); // Get the value of the input field
      console.log('process id ',processId)
      const selector = `[onclick="GW.process.runProcess('${processId}', 'hello_world.py', 'python')"]`;
      cy.get(selector).click(); // Perform actions using the dynamically constructed selector
    });
    cy.intercept('POST', '/Geoweaver/web/executeProcess').as('executeProcess'); // Intercept the POST request
    cy.get('#host-execute-btn').click();

    cy.get('#process-confirm-btn').click();
    cy.get('#inputpswd').clear('1');
    cy.get('#inputpswd').type('123456');
    cy.get('#pswd-confirm-btn').click();

    cy.wait(5000);

    cy.get('#single-console-content').should('contain', 'hello world!');
    cy.get('#single-console-content').should('contain', 'Exit Code: 0');
    cy.get('#process-log-window').click();
    cy.get('#process-log-window').should('be.visible');
    cy.wait('@executeProcess').its('response.statusCode').should('eq', 200);
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
    cy.get('#inputpswd').type('123456');
    cy.get('#pswd-confirm-btn').click();
    cy.intercept('POST', '/Geoweaver/web/readEnvironment').as('readEnvironment');
    
    cy.wait('@readEnvironment').then((interception) => {
      cy.log(interception)
      expect(interception.response.statusCode).to.equal(200);
    });
  })

});
