const { defineConfig } = require('cypress')
const os = require('os');
module.exports = defineConfig({
  projectId: 'u864mu',
  env: {
    codeCoverage: {
      url: 'http://localhost:3000/__coverage__'
    },
    home:  os.homedir(),
  },
  e2e: {
    experimentalStudio: true,
    setupNodeEvents(on, config) {
      on('task', {
        log(message) {
          // Then to see the log messages in the terminal
          //   cy.task("log", "my message");
          console.log(message +'\n\n');
          return null;
        },
      });}
    }
  
})