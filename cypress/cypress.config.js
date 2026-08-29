const { defineConfig } = require('cypress')
const os = require('os');
module.exports = defineConfig({
  projectId: 'u864mu',
  env: {
    codeCoverage: {
      url: 'http://localhost:3000/__coverage__'
    },
    home: os.homedir(),
    localhost_password: '123456',
  },
  e2e: {
    experimentalStudio: true,
    supportFile: 'support/e2e.js',
    specPattern: 'e2e/**/*.cy.{js,jsx,ts,tsx}',
    setupNodeEvents(on, config) {
      on('task', {
        log(message) {
          // Then to see the log messages in the terminal
          //   cy.task("log", "my message");
          console.log(message +'\n\n');
          return null;
        },
      });
      on('after:screenshot', (details) => {
        console.log('Screenshot taken:', details);
        return null;
      });
    },
  },
  screenshotsFolder: 'screenshots',
  videosFolder: 'videos',
})
