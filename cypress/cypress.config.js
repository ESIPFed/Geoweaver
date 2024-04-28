const { defineConfig } = require('cypress')

module.exports = defineConfig({
  projectId: 'u864mu',
  env: {
    codeCoverage: {
      url: 'http://localhost:3000/__coverage__'
    }
  },
  e2e: {
    experimentalStudio: true,
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  }
  
})
