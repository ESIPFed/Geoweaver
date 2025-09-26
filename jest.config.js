module.exports = {
  testEnvironment: 'jsdom',
  testMatch: [
    '**/src/test/javascript/**/*.test.js'
  ],
  setupFilesAfterEnv: ['<rootDir>/src/test/javascript/setup.js'],
  collectCoverageFrom: [
    'src/main/resources/static/js/**/*.js',
    '!src/main/resources/static/js/**/*.min.js'
  ],
  coverageDirectory: 'target/coverage/javascript',
  coverageReporters: ['text', 'lcov', 'html']
};
