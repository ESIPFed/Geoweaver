/**
 * Test file for gw.general.js
 */
import { describe, it, expect, beforeEach } from 'vitest';

describe('GW.general', () => {
    
    beforeEach(() => {
        // Setup DOM elements for testing
        document.body.innerHTML = '<div id="snackbar"></div>';
    });

    describe('showToasts', () => {
        it('should display toast message', () => {
            GW.general.showToasts('Test message');
            const snackbar = document.getElementById('snackbar');
            expect(snackbar.textContent).toBe('Test message');
        });
    });

    describe('showElement', () => {
        it('should show element with display block', () => {
            const element = document.createElement('div');
            element.style.display = 'none';
            document.body.appendChild(element);
            
            GW.general.showElement(element);
            expect(element.style.display).toBe('block');
        });
    });

    describe('hideElement', () => {
        it('should hide element with display none', () => {
            const element = document.createElement('div');
            element.style.display = 'block';
            document.body.appendChild(element);
            
            GW.general.hideElement(element);
            expect(element.style.display).toBe('none');
        });
    });
});
