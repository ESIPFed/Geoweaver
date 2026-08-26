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
    // Project root is ./cypress (CI working-directory). Prefer top-level e2e/support
    // over the legacy nested cypress/cypress/ tree.
    specPattern: 'e2e/**/*.cy.{js,jsx,ts,tsx}',
    supportFile: 'support/e2e.js',
    setupNodeEvents(on, config) {
      on('task', {
        log(message) {
          console.log(message + '\n\n');
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
