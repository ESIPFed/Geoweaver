// const { defineConfig } = require("cypress");

// module.exports = defineConfig({
//   e2e: {
//     setupNodeEvents(on, config) {
//       // implement node event listeners here
//     },
//   }
// });

const { defineConfig } = require('cypress')

module.exports = defineConfig({
  env: {
    codeCoverage: {
      url: 'http://localhost:3000/__coverage__'
    }
  },
  e2e: {
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  }
  
})
