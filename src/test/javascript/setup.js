/**
 * Jest setup file for frontend tests
 */

// Mock jQuery
global.$ = global.jQuery = require('jquery');

// Mock D3
global.d3 = {
  select: function(selector) {
    return {
      node: function() {
        return document.querySelector(selector);
      },
      on: function(event, handler) {
        return this;
      },
      click: function() {
        return this;
      }
    };
  }
};

// Mock Monaco Editor
global.monaco = {
  editor: {
    setTheme: function(theme) {
      this.currentTheme = theme;
    }
  }
};

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};
global.localStorage = localStorageMock;

// Mock fetch
global.fetch = jest.fn();

// Mock console to reduce noise in tests
global.console = {
  ...console,
  log: jest.fn(),
  debug: jest.fn(),
  info: jest.fn(),
  warn: jest.fn(),
  error: jest.fn(),
};
